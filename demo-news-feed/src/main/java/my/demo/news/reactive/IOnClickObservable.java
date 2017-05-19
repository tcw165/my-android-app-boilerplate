package my.demo.news.reactive;

import io.reactivex.Observable;

public interface IOnClickObservable<T> {

    Observable<T> getOnClickObservable();
}
