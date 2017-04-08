// Copyright (c) 2016 boyw165
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

package com.my.boilerplate.data;

import android.app.Activity;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;

import com.my.core.protocol.IProgressBarView;

import java.lang.ref.WeakReference;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

/**
 * A store is a data layer which observes the {@link ContentProvider} and
 * provides APIs of observable to the view-model.
 *
 *    .-----------.   .-----------.
 *    | ViewModel |   | ViewModel |   ...
 *    '-----------'   '-----------'
 *           ^             ^
 *            \            |             observes
 *           .----------------------.
 *           |  Store (Data Layer)  |
 *           '----------------------'
 *                      ^
 *                      |                observes
 *            .-------------------.
 *            |  ContentProvider  |
 *            '-------------------'
 */
public abstract class AbstractContentProviderStore<T> {

    @NonNull
    protected final ContentResolver mResolver;

    @NonNull
    protected final ContentObserver mContentObserver;

    /**
     * The behavior that show/hide the progress-bar.
     */
    protected ObservableTransformer<Object, Object> mDefaultRxBehavior;
    protected WeakReference<IProgressBarView> mProgressView;

    public AbstractContentProviderStore(ContentResolver contentResolver) {
        mResolver = contentResolver;
        mContentObserver = createContentObserver();
    }

    @SuppressWarnings("unused")
    final public ContentResolver getContentResolver() {
        return mResolver;
    }

    @SuppressWarnings("unused")
    final public ContentObserver getContentObserver() {
        return mContentObserver;
    }

    @SuppressWarnings("unused")
    final public void subscribeToContentResolver() {
        mResolver.registerContentObserver(
            getRegistrationUri(), true, mContentObserver);
    }

    @SuppressWarnings("unused")
    final public void unsubscribeToContentResolver() {
        mResolver.unregisterContentObserver(mContentObserver);
    }

    /**
     * Show the progress bar when the query is process; hide the progress bar
     * when the process is done.
     */
    @SuppressWarnings("unused")
    public AbstractContentProviderStore<T> showProgressBar(IProgressBarView view) {
        if (view instanceof Activity &&
            ((Activity) view).isFinishing()) return this;

        mProgressView = new WeakReference<>(view);

        return this;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    /**
     * Get the URI for registering {@link ContentObserver}.
     */
    @SuppressWarnings("unused")
    @NonNull
    protected abstract Uri getRegistrationUri();

    /**
     * Observer is responsible for receiving the update from the database and
     * handle it in a worker thread.
     * <br/><br/>
     * Example:
     * <pre>
     * ContentObserver createContentObserver() {
     *     return new ContentObserver(createHandler(TAG)) {
     *         public void onChange(boolean selfChange, Uri uri) {
     *             super.onChange(selfChange, uri);
     *             // Dispatch the callbacks or notify the observers.
     *         }
     *     };
     * }
     * </pre>
     */
    @SuppressWarnings("unused")
    @NonNull
    protected abstract ContentObserver createContentObserver();

    @NonNull
    protected static Handler createHandler(String name) {
        HandlerThread handlerThread = new HandlerThread(name);
        handlerThread.start();
        return new Handler(handlerThread.getLooper());
    }

    protected void showProgressBar() {
        if (mProgressView == null || mProgressView.get() == null) return;

        mProgressView.get().showProgressBar();
    }

    protected void hideProgressBar() {
        if (mProgressView == null || mProgressView.get() == null) return;

        mProgressView.get().hideProgressBar();
    }

    @SuppressWarnings("unchecked,unused")
    protected  <T> ObservableTransformer<T, T> applyDefaultBehavior() {
        // The reusable behavior.
        if (mDefaultRxBehavior == null) {
            mDefaultRxBehavior = new ObservableTransformer<Object, Object>() {
                @Override
                public ObservableSource<Object> apply(Observable<Object> upstream) {
                    return upstream
                        // When start.
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe(new Consumer<Disposable>() {
                            @Override
                            public void accept(Disposable disposable)
                                throws Exception {
                                showProgressBar();
                            }
                        })
                        // When error happens.
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnError(new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable)
                                throws Exception {
                                hideProgressBar();
                            }
                        })
                        // When next.
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext(new Consumer<Object>() {
                            @Override
                            public void accept(Object o) throws Exception {
                                showProgressBar();
                            }
                        })
                        // When end.
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnComplete(new Action() {
                            @Override
                            public void run() throws Exception {
                                hideProgressBar();
                            }
                        });
                }
            };
        }

        return (ObservableTransformer<T, T>) mDefaultRxBehavior;
    }
}
