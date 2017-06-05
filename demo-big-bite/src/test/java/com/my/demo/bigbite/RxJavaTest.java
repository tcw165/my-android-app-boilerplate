package com.my.demo.bigbite;

import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.TestScheduler;

public class RxJavaTest {

    @Test
    public void testSchedulerTest() throws Exception {
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

    @Test
    public void connectableObservableTest() throws Exception {
        //
        //              +---p[0]-p[1]-p[2]-p[3]-p[4]-p[5]-...---+
        //              |                                       |
        //              |                                       |
        // [upstream]---+---f[0]---------------------f[1]-...---+---[downstream]
        //              |                                       |
        //              |                                       |
        //              +---l[0]------l[1]------l[2]------...---+
        //

        final CountDownLatch latch = new CountDownLatch(1);
        final long TOTAL_LIFE_SECONDS = 3;
        final CompositeDisposable disposables = new CompositeDisposable();
        disposables.add(
            Observable
                .just(0)
                // Make the upstream sharable for the downstream.
                .publish(new Function<Observable<Integer>, ObservableSource<Event>>() {
                    @Override
                    public ObservableSource<Event> apply(final Observable<Integer> upstream)
                        throws Exception {
                        return Observable.mergeArray(
                            // The life control source.
                            upstream.compose(toLifeEvent(TOTAL_LIFE_SECONDS)),
                            // The period source.
                            upstream.compose(toPeriodEvent()),
                            // The filtering source.
                            upstream.compose(toFilterEvent()));
                    }
                })
                // Don't react to the event by referring to the value of filter
                // event.
                .compose(filterByEvent())
                // React to the event.
                .subscribe(
                    new Consumer<Event>() {
                        @Override
                        public void accept(Event event) throws Exception {
                            // Self-terminate if the life is at the end.
                            if (event instanceof LifeEvent &&
                                event.value >= 1) {
                                disposables.clear();
                            }

                            System.out.format("onNext: %s\n", event);
                        }
                    },
                    new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            System.err.println(throwable.getMessage());
                        }
                    },
                    new Action() {
                        @Override
                        public void run() throws Exception {
                            System.out.println("Terminate the experiment.");
                            latch.countDown();
                        }
                    }));

        latch.await(TOTAL_LIFE_SECONDS + 1, TimeUnit.SECONDS);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    private ObservableTransformer<Integer, Event> toLifeEvent(final long totalLifeInSeconds) {
        return new ObservableTransformer<Integer, Event>() {
            @Override
            public ObservableSource<Event> apply(final Observable<Integer> upstream) {
                return upstream
                    .switchMap(new Function<Integer, ObservableSource<Event>>() {
                        @Override
                        public ObservableSource<Event> apply(Integer value) throws Exception {
                            return Observable
                                .interval(totalLifeInSeconds, TimeUnit.SECONDS)
                                .map(new Function<Long, Event>() {
                                    @Override
                                    public Event apply(Long value) throws Exception {
                                        return new LifeEvent(value);
                                    }
                                });
                        }
                    });
            }
        };
    }

    private ObservableTransformer<Integer, Event> toFilterEvent() {
        return new ObservableTransformer<Integer, Event>() {
            @Override
            public ObservableSource<Event> apply(final Observable<Integer> upstream) {
                return upstream
                    .switchMap(new Function<Integer, ObservableSource<Event>>() {
                        @Override
                        public ObservableSource<Event> apply(Integer value) throws Exception {
                            return Observable
                                .interval(800, TimeUnit.MILLISECONDS)
                                .map(new Function<Long, Event>() {
                                    @Override
                                    public Event apply(Long value) throws Exception {
                                        return new FilterEvent(value);
                                    }
                                });
                        }
                    });
            }
        };
    }

    private ObservableTransformer<Integer, Event> toPeriodEvent() {
        return new ObservableTransformer<Integer, Event>() {
            @Override
            public ObservableSource<Event> apply(final Observable<Integer> upstream) {
                return upstream
                    .switchMap(new Function<Integer, ObservableSource<Event>>() {
                        @Override
                        public ObservableSource<Event> apply(Integer value) throws Exception {
                            return Observable
                                .interval(100, TimeUnit.MILLISECONDS)
                                .map(new Function<Long, Event>() {
                                    @Override
                                    public Event apply(Long value) throws Exception {
                                        return new PeriodEvent(value);
                                    }
                                });
                        }
                    });
            }
        };
    }

    private ObservableTransformer<Event, Event> filterByEvent() {
        final AtomicBoolean ok = new AtomicBoolean(false);
        return new ObservableTransformer<Event, Event>() {
            @Override
            public ObservableSource<Event> apply(Observable<Event> upstream) {
                return upstream.filter(new Predicate<Event>() {
                    @Override
                    public boolean test(Event event) throws Exception {
                        if (event instanceof FilterEvent &&
                            event.value > 1 &&
                            !ok.get()) {
                            ok.set(true);
                        }
                        return ok.get();
                    }
                });
            }
        };
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    private static abstract class Event {

        final long value;

        Event(long value) {
            this.value = value;
        }
    }

    private static class LifeEvent extends Event {

        LifeEvent(long value) {
            super(value);
        }

        @Override
        public String toString() {
            return String.format("LifeEvent{%s}", value);
        }
    }

    private static class FilterEvent extends Event {

        FilterEvent(long value) {
            super(value);
        }

        @Override
        public String toString() {
            return String.format("FilterEvent{%s}", value);
        }
    }

    private static class PeriodEvent extends Event {

        PeriodEvent(long value) {
            super(value);
        }

        @Override
        public String toString() {
            return String.format("PeriodEvent{%s}", value);
        }
    }
}
