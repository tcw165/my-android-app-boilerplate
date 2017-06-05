package com.my.reactive;

import io.reactivex.Observable;

public interface IOnClickObservable<T> {

    Observable<T> getOnClickObservable();
}
