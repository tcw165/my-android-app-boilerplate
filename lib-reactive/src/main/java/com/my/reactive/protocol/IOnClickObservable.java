package com.my.reactive.protocol;

import io.reactivex.Observable;

public interface IOnClickObservable<T> {

    Observable<T> getOnClickObservable();
}
