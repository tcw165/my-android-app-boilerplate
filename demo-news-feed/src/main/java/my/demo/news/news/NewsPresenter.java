package my.demo.news.news;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import my.demo.news.event.StateEvent;
import my.demo.news.net.INewsServiceApi;

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

        //            { UI }
        //              |
        //              |  imperative call
        //              v
        //        { HTTP/HTTPS }
        //              |
        //              |  Observable<TranslateJsonEvent>
        //              v
        //       { Translate JSON }
        //              |
        //              |  Observable<UpdateViewEvent>
        //              v
        //            { UI }

        // TODO: Break into at least two parts.
        // Do HTTP/HTTPS request, triggered by UI.
        return mService
            .getNews()
            .subscribeOn(mWorkerScheduler)
            .observeOn(mWorkerScheduler)
            .map(new Function<String, TranslateJsonEvent>() {
                @Override
                public TranslateJsonEvent apply(String s)
                    throws Exception {
                    return TranslateJsonEvent.success(s);
                }
            })
            .onErrorReturn(new Function<Throwable, TranslateJsonEvent>() {
                @Override
                public TranslateJsonEvent apply(Throwable err)
                    throws Exception {
                    return TranslateJsonEvent.failure(err);
                }
            })
            .startWith(TranslateJsonEvent.inProgress())
            // Do JSON translation.
            .observeOn(mWorkerScheduler)
            .flatMap(new Function<TranslateJsonEvent, ObservableSource<UpdateViewEvent>>() {
                @Override
                public ObservableSource<UpdateViewEvent> apply(TranslateJsonEvent event)
                    throws Exception {

                    if (event.state == StateEvent.STATE_IN_PROGRESS) {
                        return Observable.just(UpdateViewEvent.inProgress());
                    } else if (event.state == StateEvent.STATE_FAILURE) {
                        return Observable.just(UpdateViewEvent.failure(event.err));
                    } else {
                        return mJsonTranslator
                            .translateEntity(event.json)
                            .subscribeOn(mWorkerScheduler)
                            .toObservable()
                            .map(new Function<List<INewsEntity>, UpdateViewEvent>() {
                                @Override
                                public UpdateViewEvent apply(List<INewsEntity> entities)
                                    throws Exception {
                                    return UpdateViewEvent.success(entities);
                                }
                            });
                    }
                }
            })
            // Back to UI.
            .observeOn(mUiScheduler)
            .subscribe(
                // onNext...
                new Consumer<UpdateViewEvent>() {
                    @Override
                    public void accept(UpdateViewEvent event)
                        throws Exception {
                        if (event.state == UpdateViewEvent.STATE_IN_PROGRESS) {
                            mView.showProgressIndicator(true);
                            mView.showNewsPlaceholder(false);
                        } else if (event.state == UpdateViewEvent.STATE_SUCCESS) {
                            mView.showProgressIndicator(false);
                            mView.showNewsPlaceholder(false);
                            mView.showNews(event.entities);
                        } else if (event.state == UpdateViewEvent.STATE_FAILURE) {
                            mView.showProgressIndicator(false);
                            mView.showNewsPlaceholder(true);

                            event.err.printStackTrace();
                        }
                    }
                },
                // onError...
                new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable)
                        throws Exception {
                        throwable.printStackTrace();
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
