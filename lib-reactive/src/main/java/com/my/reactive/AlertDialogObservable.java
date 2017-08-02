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

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.MainThreadDisposable;

/**
 * An observable encapsulate {@link AlertDialog} and emit a boolean to downstream.
 */
public class AlertDialogObservable extends Observable<Boolean> {

    // Given.
    private final AlertDialog.Builder mSource;
    private final String mPositiveString;
    private final String mNegativeString;

    public AlertDialogObservable(AlertDialog.Builder source,
                                 String positiveString,
                                 String negativeString) {
        mSource = source;
        mPositiveString = positiveString;
        mNegativeString = negativeString;
    }

    @Override
    protected void subscribeActual(final Observer<? super Boolean> observer) {
        final Disposable disposable = new Disposable(observer);

        mSource.setPositiveButton(mPositiveString, disposable);
        mSource.setNegativeButton(mNegativeString, disposable);

        disposable.actual = mSource.create();
        disposable.actual.show();

        observer.onSubscribe(disposable);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    private static final class Disposable
        extends MainThreadDisposable
        implements AlertDialog.OnClickListener {

        // Given...
        final Observer<? super Boolean> observer;

        // Given later on...
        AlertDialog actual;

        Disposable(Observer<? super Boolean> observer) {
            this.observer = observer;
        }

        @Override
        protected void onDispose() {
            this.actual.dismiss();
            this.actual = null;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                observer.onNext(true);
            } else {
                observer.onNext(false);
            }
            observer.onComplete();
        }
    }
}
