// Copyright (c) 2017-present Cardinalblue
//
// Author: boy@cardinalblue.com
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

package com.my.reactive;

import android.os.Looper;
import android.widget.SeekBar;

import com.my.reactive.uiEvent.UiEvent;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.MainThreadDisposable;

/**
 * An observable wrapper for {@link SeekBar}. It emits the {@link UiEvent} to
 * downstream to indicate the progress of the seek-bar, also the start-doing-stop
 * touch information.
 */
public class SeekBarChangeObservable extends Observable<UiEvent<Integer>> {

    private final SeekBar view;

    public SeekBarChangeObservable(SeekBar view) {
        this.view = view;
    }

    @Override
    protected void subscribeActual(Observer<? super UiEvent<Integer>> observer) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            observer.onError(new IllegalStateException(
                "Expected to be called on the main thread but was " +
                Thread.currentThread().getName()));
        }

        final Listener listener = new Listener(view, observer);
        view.setOnSeekBarChangeListener(listener);
        observer.onSubscribe(listener);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    static final class Listener
        extends MainThreadDisposable
        implements SeekBar.OnSeekBarChangeListener {

        private final SeekBar view;
        private final Observer<? super UiEvent<Integer>> observer;

        Listener(SeekBar view,
                 Observer<? super UiEvent<Integer>> observer) {
            this.view = view;
            this.observer = observer;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar,
                                      int progress,
                                      boolean fromUser) {
            if (!isDisposed()) {
                observer.onNext(UiEvent.doing(fromUser,
                                              progress));
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            if (!isDisposed()) {
                observer.onNext(UiEvent.start(view.getProgress()));
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (!isDisposed()) {
                observer.onNext(UiEvent.stop(view.getProgress()));
            }
        }

        @Override
        protected void onDispose() {
            view.setOnSeekBarChangeListener(null);
        }
    }
}
