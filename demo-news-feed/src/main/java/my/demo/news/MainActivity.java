package my.demo.news;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import my.demo.news.net.INewsServiceApi;
import my.demo.news.net.StringResponseFactory;
import my.demo.news.news.INewsContract;
import my.demo.news.news.INewsEntity;
import my.demo.news.news.NewsJsonTranslator;
import my.demo.news.news.NewsPresenter;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;

public class MainActivity
    extends AppCompatActivity
    implements INewsContract.View {

    private static final String TAG = MainActivity.class.getSimpleName();

    // View.
    @BindView(R.id.list)
    RecyclerView mListView;
    @BindView(R.id.placeholder)
    View mPlaceholder;

    ProgressDialog mProgressView;

    // Data.
    NewsListAdapter mListViewAdapter;

    private CompositeDisposable mDisposables;

    private Unbinder mUnbinder;

    private INewsContract.UserInteraction mPresenter;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showNews(final List<INewsEntity> entities) {
        mListViewAdapter.setItems(entities);

        // TODO: Because the following code is part of the business logic,
        // TODO: maybe move the following code into the presenter so that
        // TODO: it is testable?
        // onClick listener.
        mDisposables.add(
            mListViewAdapter
                .getOnClickObservable()
                .debounce(200, TimeUnit.MILLISECONDS)
                .subscribe(
                    new Consumer<INewsEntity>() {
                        @Override
                        public void accept(INewsEntity entity) throws Exception {
                            mPresenter.openNewsDetails(entity);
                        }
                    },
                    new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable err) throws Exception {
                            err.printStackTrace();
                        }
                    }));
    }

    @Override
    public void showNewsPlaceholder(boolean value) {
        mListView.setVisibility(!value ? View.VISIBLE : View.GONE);
        mPlaceholder.setVisibility(value ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showNewsDetailUi(INewsEntity entity) {
        // Serialize the entities and pass to next activity would save me from
        // explicitly saving-restoring data.
        startActivity(
            new Intent(MainActivity.this, DetailViewActivity.class)
                .putExtra(Intent.ACTION_ATTACH_DATA, entity));
    }

    @Override
    public void showProgressIndicator(boolean value) {
        if (value) {
            mProgressView.show();
        } else {
            mProgressView.dismiss();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mUnbinder = ButterKnife.bind(this);

        // Progress indicator.
        mProgressView = new ProgressDialog(this);
        mProgressView.setMessage(getString(R.string.warning_loading));
        mProgressView.setCancelable(false);

        // Init list adapter.
        mListView.setAdapter(mListViewAdapter = new NewsListAdapter(this));
        mListView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        // TODO: Use DI to manage parameter injection.
        // Init the presenter (business logic).
        mPresenter = new NewsPresenter(this,
                                       new NewsJsonTranslator(),
                                       getServiceApi(),
                                       Schedulers.io(),
                                       AndroidSchedulers.mainThread());
    }

    @Override
    protected void onResume() {
        super.onResume();

        mDisposables = new CompositeDisposable();

        // Load the news feed.
        try {
            // showNews would be called when news is loaded.
            mDisposables.add(
                mPresenter.getNews());
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Dispose everything.
        mDisposables.clear();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unbind the view.
        mUnbinder.unbind();
    }

    private INewsServiceApi getServiceApi() {
        final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .cache(new Cache(getExternalCacheDir(),
                             BuildConfig.DEFAULT_DOWNLOAD_CACHE_SIZE_MB * 1024 * 1024))
            // To return cached data if offline.
            .addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request originalRequest = chain.request();
                    String cacheHeaderValue = isNetworkAvailable(MainActivity.this)
                        ? "public, max-age=2419200"
                        : "public, only-if-cached, max-stale=2419200";
                    Request request = originalRequest.newBuilder().build();
                    Response response = chain.proceed(request);
                    return response.newBuilder()
                                   .removeHeader("Pragma")
                                   .removeHeader("Cache-Control")
                                   .header("Cache-Control", cacheHeaderValue)
                                   .build();
                }
            })
            // To return cached data if offline.
            .addNetworkInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request originalRequest = chain.request();
                    String cacheHeaderValue = isNetworkAvailable(MainActivity.this)
                        ? "public, max-age=2419200"
                        : "public, only-if-cached, max-stale=2419200";
                    Request request = originalRequest.newBuilder().build();
                    Response response = chain.proceed(request);
                    return response.newBuilder()
                                   .removeHeader("Pragma")
                                   .removeHeader("Cache-Control")
                                   .header("Cache-Control", cacheHeaderValue)
                                   .build();
                }
            })
            .build();
        final Retrofit factory = new Retrofit.Builder()
            .baseUrl("http://api.myjson.com/")
            .client(client)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(StringResponseFactory.create())
            .build();

        return factory.create(INewsServiceApi.class);
    }

    private boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager cm = (ConnectivityManager) context
            .getSystemService(Context.CONNECTIVITY_SERVICE);

        final NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
               activeNetwork.isConnectedOrConnecting();
    }
}
