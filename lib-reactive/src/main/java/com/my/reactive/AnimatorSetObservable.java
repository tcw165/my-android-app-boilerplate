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

import android.animation.Animator;
import android.animation.AnimatorSet;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.MainThreadDisposable;

/**
 * An observable emitting the progress in integer from 0 to 100.
 */
public class AnimatorSetObservable extends Observable<Integer> {

    private final AnimatorSet mSource;

    public AnimatorSetObservable(AnimatorSet source) {
        mSource = source;
    }

    @Override
    protected void subscribeActual(Observer<? super Integer> observer) {
        final Disposable disposable = new Disposable(observer, mSource);

        observer.onSubscribe(disposable);

        mSource.addListener(disposable);
        mSource.start();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    private static final class Disposable
        extends MainThreadDisposable
        implements Animator.AnimatorListener {

        private final AnimatorSet source;
        private final Observer<? super Integer> observer;

        Disposable(Observer<? super Integer> observer,
                   AnimatorSet source) {
            this.observer = observer;
            this.source = source;
        }

        @Override
        protected void onDispose() {
            source.removeListener(this);
        }

        @Override
        public void onAnimationStart(Animator animation) {
            if (isDisposed()) return;

            observer.onNext(0);
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (isDisposed()) return;

            observer.onNext(100);
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            if (isDisposed()) return;

            observer.onNext(100);
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
            // DO NOTHING.
        }
    }
}
