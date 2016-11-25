// Copyright (c) 2016 boyw165
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
//    The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
//    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

package com.my.boilerplate.util;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.my.boilerplate.json.JsonWhatever;
import com.my.boilerplate.net.IWhateverApiService;
import com.my.boilerplate.view.IProgressBarView;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.internal.util.ConnectConsumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WebApiUtil {

    protected static final String TAG = WebApiUtil.class.getSimpleName();

    public static final String BASE_URL = "http://cook-buddy.herokuapp.com/";

    protected static WebApiUtil sSingleton = null;

    public static WebApiUtil with(final Context context) {
        if (sSingleton == null) {
            sSingleton = new WebApiUtil(context.getApplicationContext());
        }

        return sSingleton;
    }

    ///////////////////////////////////////////////////////////////////////////

    /**
     * The application context.
     */
    protected final Context mContext;

    /**
     * The weak reference pointed to the activity that supports
     * {@code IProgressBarView} interface.
     */
    protected WeakReference<IProgressBarView> mProgressView;

    protected final OkHttpClient mHttpClient;
    protected final Retrofit mServiceFactory;
    protected final IWhateverApiService mWhateverApiServ;

//    protected Transformer<Object, Object> mDefaultRxBehavior;

    WebApiUtil(final Context context) {
        mHttpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

        // TODO: Interceptor?
//        httpClient.networkInterceptors().add(new Interceptor() {
//            public com.squareup.okhttp.Response intercept(Chain chain) throws IOException {
//                Request.Builder builder = chain.request().newBuilder();
//
//                System.out.println("Adding headers:" + headers);
//                for (Map.Entry<String, String> entry : headers.entrySet()) {
//                    builder.addHeader(entry.getKey(), entry.getValue());
//                }
//
//                return chain.proceed(builder.build());
//            }
//        });

        mServiceFactory = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(mHttpClient)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build();

        mContext = context;
        mWhateverApiServ = mServiceFactory.create(IWhateverApiService.class);
    }

    /**
     * Show the progress bar when processing.
     */
    public WebApiUtil showProgressBar(final IProgressBarView view) {
        if (view instanceof Activity &&
            ((Activity) view).isFinishing()) return this;

        mProgressView = new WeakReference<>(view);

        return this;
    }

    /**
     * Get certain JSON from the server and show the progress bar for you.
     */
    public Observable<JsonWhatever> getJsonWhatever() {
        return mWhateverApiServ
            .getJsonWhatever();
            // Apply default behavior.
//            .compose(this.<JsonWhatever>applyDefaultBehavior());
//            .compose(new rx.Observable.Transformer<JsonWhatever, JsonWhatever>() {
//                @Override
//                public rx.Observable<JsonWhatever> call(rx.Observable<JsonWhatever> ob) {
//                    return ob
//                        .subscribeOn(rx.schedulers.Schedulers.io())
//                        // When start.
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .doOnSubscribe(new Consumer<Disposable>() {
//                            @Override
//                            public void accept(Disposable disposable) throws Exception {
//                                Log.i(TAG, "API call starts.");
//                                showProgressBar();
//                            }
//                        })
//                        // When error happens.
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .doOnError(new Consumer<Throwable>() {
//                            @Override
//                            public void accept(Throwable t) throws Exception {
//                                Log.e(TAG, String.format("API call error: %s", t));
//                                hideProgressBar();
//                            }
//                        })
//                        // When end.
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .doOnComplete(new Action() {
//                            @Override
//                            public void run() throws Exception {
//                                Log.i(TAG, "API call ends.");
//                                hideProgressBar();
//                            }
//                        })
//                        .observeOn(AndroidSchedulers.mainThread());
//                    ;
//                }
//            })
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / protected Methods //////////////////////////////////////////

    protected void showProgressBar() {
        if (mProgressView == null || mProgressView.get() == null) return;

        mProgressView.get().showProgressBar();
    }

    protected void hideProgressBar() {
        if (mProgressView == null || mProgressView.get() == null) return;

        mProgressView.get().hideProgressBar();
    }

//    @SuppressWarnings("unchecked")
//    protected <T> rx.Observable.Transformer<T, T> applyDefaultBehavior() {
//        // The reusable behavior.
//        if (mDefaultRxBehavior == null) {
//            mDefaultRxBehavior = new rx.Observable.Transformer<Object, Object>() {
//                @Override
//                public Observable<Object> call(Observable<Object> ob) {
//                    return ob.subscribeOn(Schedulers.io())
//                             // When start.
//                             .observeOn(AndroidSchedulers.mainThread())
//                             .doOnSubscribe(new Consumer<Disposable>() {
//                                 @Override
//                                 public void accept(Disposable disposable) throws Exception {
//                                     Log.i(TAG, "API call starts.");
//                                     showProgressBar();
//                                 }
//                             })
//                             // When error happens.
//                             .observeOn(AndroidSchedulers.mainThread())
//                             .doOnError(new Consumer<Throwable>() {
//                                 @Override
//                                 public void accept(Throwable t) throws Exception {
//                                     Log.e(TAG, String.format("API call error: %s", t));
//                                     hideProgressBar();
//                                 }
//                             })
//                             // When end.
//                             .observeOn(AndroidSchedulers.mainThread())
//                             .doOnComplete(new Action() {
//                                 @Override
//                                 public void run() throws Exception {
//                                     Log.i(TAG, "API call ends.");
//                                     hideProgressBar();
//                                 }
//                             })
//                             .observeOn(AndroidSchedulers.mainThread());
//                }
//            };
//        }
//
//        return (Transformer<T, T>) mDefaultRxBehavior;
//    }
}
