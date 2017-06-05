package com.my.demo.bigbite;

import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.TestScheduler;

public class RxJavaTest {

    @Test
    public void disposableTest() throws Exception {
        TestScheduler scheduler = new TestScheduler();
        TestObserver<Long> o = Observable
            .interval(1, TimeUnit.SECONDS, scheduler)
            .test();
        o.assertNoValues();
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        o.assertValues(0L);
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        o.assertValues(0L, 1L);

        // Dispose the connection.
        o.dispose();

        scheduler.advanceTimeBy(100, TimeUnit.SECONDS);
        o.assertValues(0L, 1L);
    }

    /**
     * If a completed observable is also disposed.
     */
    @Test
    public void completeTest() throws Exception {
        System.out.println("Before.");
        Disposable disposable = Observable
            .fromCallable(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    System.out.println("Run callable.");
                    return new Object();
                }
            })
            .subscribe(
                new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        System.out.println("onNext.");
                    }
                },
                new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        System.out.println("onError.");
                    }
                },
                new Action() {
                    @Override
                    public void run() throws Exception {
                        System.out.println("onComplete.");
                    }
                });
        System.out.println("After.");
        System.out.println(String.format("disposable %s disposed.",
                                         (disposable.isDisposed() ?
                                             "is" : "isn't")));
    }
}
