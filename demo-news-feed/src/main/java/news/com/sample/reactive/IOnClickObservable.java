package news.com.sample.reactive;

import io.reactivex.Observable;

public interface IOnClickObservable<T> {

    Observable<T> getOnClickObservable();
}
