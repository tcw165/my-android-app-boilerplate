package news.com.sample.news;

import java.util.List;

import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import news.com.sample.net.INewsServiceApi;

public class NewsPresenter implements INewsContract.UserInteraction {

    private final INewsContract.View mView;
    private final INewsContract.JsonTranslator mJsonTranslator;
    private final INewsServiceApi mService;
    private final Scheduler mWorkerScheduler;
    private final Scheduler mUiScheduler;

    public NewsPresenter(INewsContract.View view,
                         INewsContract.JsonTranslator translator,
                         INewsServiceApi serviceApi,
                         Scheduler workerScheduler,
                         Scheduler uiScheduler) {
        mView = view;
        mJsonTranslator = translator;
        mService = serviceApi;
        mWorkerScheduler = workerScheduler;
        mUiScheduler = uiScheduler;
    }

    @Override
    public Disposable getNews() {
        // TODO: Jake Wharton had a good youtube video of how to improve it.
        return Maybe
            .just(true)
            .observeOn(mUiScheduler)
            // Show progress indicator.
            .map(new Function<Boolean, Boolean>() {
                @Override
                public Boolean apply(Boolean ignored) throws Exception {
                    mView.showProgressIndicator(true);
                    return ignored;
                }
            })
            // Do HTTP/HTTPS request.
            .observeOn(mWorkerScheduler)
            .flatMap(new Function<Boolean, MaybeSource<String>>() {
                @Override
                public MaybeSource<String> apply(Boolean ignored) throws Exception {
                    return mService.getNews();
                }
            })
            // Do JSON translation.
            .subscribeOn(mWorkerScheduler)
            .flatMap(new Function<String, MaybeSource<List<INewsEntity>>>() {
                @Override
                public MaybeSource<List<INewsEntity>> apply(String s) throws Exception {
                    return mJsonTranslator.translateEntity(s).toMaybe();
                }
            })
            .observeOn(mUiScheduler)
            .subscribe(
                new Consumer<List<INewsEntity>>() {
                    @Override
                    public void accept(List<INewsEntity> entities) throws Exception {
                        mView.showNews(entities);
                        mView.showProgressIndicator(false);
                    }
                },
                new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        mView.showNewsPlaceholder(true);
                        mView.showProgressIndicator(false);

                        throwable.printStackTrace();
                    }
                },
                new Action() {
                    @Override
                    public void run() throws Exception {
                        mView.showProgressIndicator(false);
                    }
                });
    }

    @Override
    public void openNewsDetails(INewsEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("The news entity is null.");
        }

        mView.showNewsDetailUi(entity);
    }
}
