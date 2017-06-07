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

import android.content.Context;
import android.util.SparseArray;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.my.demo.bigbite.game.event.uiEvent.FrameUiEvent;
import com.my.demo.bigbite.game.view.CameraSourcePreview;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.MainThreadDisposable;
import io.reactivex.disposables.Disposable;

/**
 * An observable taking the {@link CameraSourcePreview} and {@link Detector} as
 * input and emit the detection result frame by frame.
 * <br/>
 * Usage:
 * <pre>
 * CameraObservable
 *     .create(getContext(), view, 320, 240, getFaceDetector())
 *     .subscribe((UiEvent event) -> {
 *         // DO SOMETHING.
 *     })
 * </pre>
 */
public final class CameraObservable<T>
    extends Observable<FrameUiEvent<T>> {

    private final Context mContext;

    private final CameraSourcePreview mCameraView;
    private final int mPreviewWidth;
    private final int mPreviewHeight;
    private final Detector<T> mDelegateDetector;

    public static <T> CameraObservable<T> create(final Context context,
                                                 final CameraSourcePreview view,
                                                 final int previewWidth,
                                                 final int previewHeight,
                                                 final Detector<T> detector) {
        return new CameraObservable<>(context,
                                      view, previewWidth, previewHeight,
                                      detector);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    private CameraObservable(final Context context,
                             final CameraSourcePreview view,
                             final int previewWidth,
                             final int previewHeight,
                             final Detector<T> detector) {
        mContext = context;
        mCameraView = view;
        mPreviewWidth = previewWidth;
        mPreviewHeight = previewHeight;
        mDelegateDetector = detector;
    }

    @Override
    protected void subscribeActual(Observer<? super FrameUiEvent<T>> observer) {
        try {
            // Give the observer a disposable.
            final Disposable disposable = new CameraPreviewDisposable(mCameraView);
            observer.onSubscribe(disposable);

            // Start camera.
            Detector<T> detector = new ObserverDetector<>(
                disposable, observer, mDelegateDetector);
            final CameraSource source = new CameraSource.Builder(mContext, detector)
                .setRequestedPreviewSize(mPreviewWidth, mPreviewHeight)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setAutoFocusEnabled(true)
                .setRequestedFps(24f)
                .build();

            mCameraView.start(source);
        } catch (Throwable err) {
            observer.onError(err);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    private static final class CameraPreviewDisposable extends MainThreadDisposable {

        final CameraSourcePreview mCameraView;

        CameraPreviewDisposable(CameraSourcePreview view) {
            mCameraView = view;
        }

        @Override
        protected void onDispose() {
            mCameraView.release();
        }
    }

    private static final class ObserverDetector<T> extends Detector<T> {

        boolean mIsFirstFrame = true;

        final Disposable mDisposable;
        final Observer<? super FrameUiEvent<T>> mObserver;
        final Detector<T> mDelegateDetector;

        ObserverDetector(final Disposable disposable,
                         final Observer<? super FrameUiEvent<T>> observer,
                         final Detector<T> other) {
            mDisposable = disposable;
            mObserver = observer;
            mDelegateDetector = other;

            // Init a DUMMY processor.
            setProcessor(new Processor<T>() {
                @Override
                public void release() {
                    // DO NOTHING.
                }

                @Override
                public void receiveDetections(Detections<T> ignored) {
                    // DO NOTHING.
                }
            });
        }

        @Override
        public SparseArray<T> detect(Frame frame) {
            if (mDisposable.isDisposed()) return null;

            try {
                final SparseArray<T> res = mDelegateDetector.detect(frame);

                if (mIsFirstFrame) {
                    mIsFirstFrame = false;
                    mObserver.onNext(FrameUiEvent.firstFrame(res));
                } else {
                    mObserver.onNext(FrameUiEvent.repeatedFrame(res));
                }

                return res;
            } catch (Throwable err) {
                mObserver.onError(err);

                return null;
            }
        }
    }
}
