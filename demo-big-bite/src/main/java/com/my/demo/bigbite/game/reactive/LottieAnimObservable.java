// Copyright (c) 2017-present boyw165
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

package com.my.demo.bigbite.game.reactive;

import android.animation.Animator;
import android.util.Log;
import android.view.View;

import com.airbnb.lottie.LottieAnimationView;
import com.my.demo.bigbite.event.UiEvent;

import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.MainThreadDisposable;

public class LottieAnimObservable extends Observable<UiEvent> {

    final AtomicBoolean mIsAnimating = new AtomicBoolean(false);
    final LottieAnimationView mView;

    public LottieAnimObservable(final LottieAnimationView view) {
        mView = view;
    }

    public boolean isAnimating() {
        return mIsAnimating.get();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    @Override
    protected void subscribeActual(Observer<? super UiEvent> observer) {
        final ListenerDisposable disposable = new ListenerDisposable(
            observer, mView, mIsAnimating);

        observer.onSubscribe(disposable);

        mView.addAnimatorListener(disposable);
        mView.playAnimation();

        mIsAnimating.set(true);
        Log.d("xyz", "fucking is animating=" + mView.isAnimating());
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    private static class ListenerDisposable extends MainThreadDisposable
        implements Animator.AnimatorListener {

        private final Observer<? super UiEvent> mObserver;
        private final LottieAnimationView mView;
        private final AtomicBoolean mIsAnimating;

        ListenerDisposable(final Observer<? super UiEvent> observer,
                           final LottieAnimationView view,
                           final AtomicBoolean isAnimating) {
            mObserver = observer;
            mView = view;
            mIsAnimating = isAnimating;
        }

        @Override
        protected void onDispose() {
            mView.removeAnimatorListener(this);
            mView.cancelAnimation();
        }

        @Override
        public void onAnimationStart(Animator animation) {
//            mObserver.onNext(AnimUiEvent.start());
            Log.d("anim", "onAnimationStart");
        }

        @Override
        public void onAnimationEnd(Animator animation) {
//            mObserver.onNext(AnimUiEvent.end());

            Log.d("anim", "onAnimationEnd");
            mIsAnimating.set(false);
            mObserver.onComplete();
            mView.setVisibility(View.GONE);
        }

        @Override
        public void onAnimationCancel(Animator animation) {
//            mObserver.onNext(AnimUiEvent.cancel());
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
            // DO NOTHING.
        }
    }
}
