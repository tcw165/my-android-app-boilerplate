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

package com.my.demo.dlib.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;
import android.widget.Toast;

import com.google.protobuf.InvalidProtocolBufferException;
import com.my.core.util.CameraUtil;
import com.my.demo.dlib.R;
import com.my.jni.dlib.FaceLandmarksDetector;
import com.my.jni.dlib.data.Face;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

// TODO: Need to change the TextureView size to match aspect-ratio of the
// TODO: camera preview size.
// TODO: Solve it in the View#onMeasure or SurfaceTexture#setDefaultBufferSize
public class FaceLandmarksCameraView
    extends TextureView
    implements TextureView.SurfaceTextureListener {

    // State.
    int mCameraId;
    float mCameraPreviewAspectRatio;
    int mCameraOrientation;
    Camera.Size mCameraPreviewSize;
    boolean mIsPreviewing;
    RectF mCaptureBound;
    Bitmap mBitmap;
    Paint mPaint;

    static final String TAG = FaceLandmarksCameraView.class.getSimpleName();
    // Because the Camera.open and Camera.setPreviewTexture take 100ms
    // individually, doing them in the other thread avoids
    Handler mHandler;
    Timer mPredictTimer;
    Camera mCamera;
    FaceLandmarksDetector mFaceDetector;

    // Callbacks.
    OnCameraPreviewListener mCallback;

    public FaceLandmarksCameraView(Context context) {
        this(context, null);
    }

    public FaceLandmarksCameraView(Context context,
                                   AttributeSet attrs) {
        super(context, attrs);

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(ContextCompat.getColor(context, R.color.pink));
    }

    @Override
    public synchronized void onSurfaceTextureAvailable(SurfaceTexture surface,
                                                       int width,
                                                       int height) {
        // Ensure the handler.
        ensureHandler();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                safePreviewCamera();
            }
        });
    }

    @Override
    public synchronized void onSurfaceTextureSizeChanged(SurfaceTexture surface,
                                                         final int width,
                                                         final int height) {
        // Ensure the handler.
        ensureHandler();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                setPreviewSize(width, height);
            }
        });
    }

    @Override
    public synchronized boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        // Ensure the handler.
        ensureHandler();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                closeCameraSync();
            }
        });
        return true;
    }

    @Override
    public synchronized void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // Invoked when the specified SurfaceTexture is updated through
        // SurfaceTexture.updateTexImage().
    }

    // FIXME: Doesn't work.
    @Override
    public void onDrawForeground(Canvas canvas) {
        super.onDrawForeground(canvas);

        canvas.drawRect(getLeft() + 100,
                        getTop() + 100,
                        getRight() - 100,
                        getBottom() - 100,
                        mPaint);
    }

    public void openCameraAsync(final RectF captureBound) {
        // TODO: Support face camera
        openCameraAsync(0, captureBound);
    }

    public void openCameraAsync(final int cameraId,
                                final RectF captureBound) {
        mCaptureBound = captureBound;

//        // Ensure the bitmap buffer.
//        mBitmap = Bitmap.createBitmap(mCaptureBound.width(),
//                                      mCaptureBound.height(),
//                                      Bitmap.Config.ARGB_8888);

        // Ensure the handler.
        ensureHandler();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                openCameraSync(cameraId);
            }
        });
    }

    public void closeCameraAsync() {
        // Ensure the handler.
        ensureHandler();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                closeCameraSync();
            }
        });
    }

    public synchronized void openCameraSync(final int cameraId) {
        if (cameraId < 0 || cameraId >= Camera.getNumberOfCameras()) return;

        try {
            closeCameraSync();

            mCamera = Camera.open(cameraId);
            mCameraId = cameraId;
        } catch (Exception e) {
            Log.e("xyz", "failed to open Camera " + cameraId);
            e.printStackTrace();
        }

        safePreviewCamera();
    }

    public synchronized void closeCameraSync() {
        if (mCamera == null) return;

        // Call stopPreview() to stop updating the preview surface.
        mCamera.stopPreview();
        mIsPreviewing = false;

        // Important: Call release() to release the camera for use by
        // other applications. Applications should release the camera
        // immediately during onPause() and re-open() it during
        // onResume()).
        mCamera.release();
        mCamera = null;

        stopTimer();

        // The handler.
        if (mHandler != null) {
            mHandler.getLooper().quit();
            mHandler = null;
        }
    }

    public void setDetector(FaceLandmarksDetector faceDetector) {
        mFaceDetector = faceDetector;
    }

    public void setOnCameraPreviewListener(OnCameraPreviewListener callback) {
        mCallback = callback;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (!isHardwareAccelerated()) {
            // TODO: Warn the user or throw an exception.
            Toast.makeText(getContext(),
                           R.string.warning_not_support_hardware_acceleration,
                           Toast.LENGTH_SHORT)
                 .show();
            return;
        }

        // Ensure the handler.
        ensureHandler();

        // The texture callback.
        setSurfaceTextureListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        // Force to close the camera.
        closeCameraAsync();

        // The texture callback.
        setSurfaceTextureListener(null);
    }

    @Override
    protected void onMeasure(int widthSpec,
                             int heightSpec) {
        if (mCameraPreviewAspectRatio == 0.f) {
            super.onMeasure(widthSpec, heightSpec);
        } else {
            final int height = MeasureSpec.getSize(heightSpec);
            final int width = (int) (height / mCameraPreviewAspectRatio);
            setMeasuredDimension(width, height);
        }
    }

    synchronized void ensureHandler() {
        if (mHandler == null) {
            final HandlerThread thread = new HandlerThread(TAG);
            thread.start();

            mHandler = new Handler(thread.getLooper());
        }
    }

    synchronized boolean isCameraOpenedAndSurfaceAvailable() {
        return mCamera != null && isAvailable();
    }

    synchronized void setPreviewSize(int width,
                                     int height) {
        if (!isCameraOpenedAndSurfaceAvailable()) return;

        // Pause the preview before updating the preview size.
        if (mIsPreviewing) mCamera.stopPreview();

        // Now that the size is known, set up the camera parameters and
        // begin the preview.
        final Camera.Parameters params = mCamera.getParameters();
        mCameraOrientation = CameraUtil.getDisplayOrientation(mCameraId);
        mCameraPreviewSize = CameraUtil.getOptimalPreviewSize(mCamera,
                                                              mCameraOrientation,
                                                              width,
                                                              height);
        params.setPreviewSize(mCameraPreviewSize.width,
                              mCameraPreviewSize.height);
        mCamera.setParameters(params);

        // Resume the preview after updating the preview size.
        if (mIsPreviewing) mCamera.startPreview();
    }

    synchronized void safePreviewCamera() {
        if (!isCameraOpenedAndSurfaceAvailable()) return;
        if (mIsPreviewing) return;

        try {
            // TODO: Detect the orientation change automatically.
            // It must be called after the surface is created and before
            // the Camera#startPreview.
            mCameraOrientation = CameraUtil.getOptimalDisplayOrientation(getContext(),
                                                                            mCameraId);
            mCamera.setPreviewTexture(getSurfaceTexture());
            mCamera.setDisplayOrientation(mCameraOrientation);
            // Find the most fit preview size.
            mCameraPreviewSize = CameraUtil.getOptimalPreviewSize(mCamera,
                                                                  mCameraOrientation,
                                                                getWidth(),
                                                                getHeight());
            mCameraPreviewAspectRatio = (float) mCameraPreviewSize.width /
                                        mCameraPreviewSize.height;
            // Request layout update in the UI thread.
            post(new Runnable() {
                @Override
                public void run() {
                    // Will trigger {@link #onMeasure}
                    requestLayout();
                }
            });
            // TODO: Start preview after the layout finished.
            mCamera.startPreview();

            // TODO: It doesn't work, fix it.
            // Enable auto-focus mode.
            CameraUtil.enableAutoFocusModel(mCamera);

            // TODO: Not sure using Timer or another HandlerThread, which is better.
            // Start a timer to predict the camera buffer.
            startTimer();

            mIsPreviewing = true;
        } catch (IOException e) {
            Log.e("xyz", "Failed to set preview.");
            e.printStackTrace();
        }
    }

    synchronized void startTimer() {
        try {
            if (mPredictTimer == null) {
                final int INTERVAL = 2000;
                mPredictTimer = new Timer();
                mPredictTimer.scheduleAtFixedRate(
                    new TimerTask() {
                        @Override
                        public void run() {
                            detectCameraPreviewAsync();
                        }
                    },
                    INTERVAL,
                    INTERVAL);
            }
        } catch (Exception e) {
            // DO NOTHING.
        }
    }

    synchronized void stopTimer() {
        if (mPredictTimer != null) {
            mPredictTimer.cancel();
            mPredictTimer = null;
        }

//        try {
//            if (mClassifier != null) {
//                mClassifier.close();
//                mClassifier = null;
//            }
//        } catch (Exception e) {
//            // DO NOTHING.
//        }

        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }
    }

    synchronized void detectCameraPreviewAsync() {
        if (mHandler == null) return;

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!isAvailable()) return;
//                if (mClassifier == null) return;
//                if (mBitmap == null) return;
                if (mCaptureBound.width() == 0 ||
                    mCaptureBound.height() == 0) return;

                // Get the current preview.
//                final Bitmap fullBitmap = getBitmap(mCameraPreviewSize.width / 4,
//                                                    mCameraPreviewSize.height / 4);
                final Bitmap fullBitmap = getBitmap();
//                final Bitmap fixedBitmap = BitmapUtil.rotateBitmap(
//                    fullBitmap, mCameraOrientation);
                Log.d("xyz", "Ready to detect landmarks (w=" + fullBitmap.getWidth() +
                             ", h=" + fullBitmap.getHeight() + ")");
//                final Bitmap faceBitmap = Bitmap.createBitmap(
//                    fullBitmap,
//                    (int) (mCaptureBound.left * fullBitmap.getWidth()),
//                    (int) (mCaptureBound.top * fullBitmap.getHeight()),
//                    (int) (mCaptureBound.width() * fullBitmap.getWidth()),
//                    (int) (mCaptureBound.height() * fullBitmap.getHeight()));

//                try {
//                    // Feed the face bitmap to the detector.
//                    final List<Face.Landmark> landmarks = mFaceDetector
//                        .findLandmarksInFace(faceBitmap);
//                    final List<Face.Landmark> landmarks = mFaceDetector
//                        .findLandmarksInFace(fullBitmap);
//
//                    // Emit the result.
//                    dispatchOnFaceLandmarksDetected(landmarks);
//                } catch (InvalidProtocolBufferException error) {
//                    Log.w("xyz", error.getMessage());
//                }

                fullBitmap.recycle();
//                fixedBitmap.recycle();
//                faceBitmap.recycle();
            }
        });
    }

    void dispatchOnFaceLandmarksDetected(final List<Face.Landmark> landmarks) {
        // Update on the UI thread.
        post(new Runnable() {
            @Override
            public void run() {
                if (mCallback == null) return;

                mCallback.onFaceLandmarksDetected(landmarks);
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    public interface OnCameraPreviewListener {

        void onFaceLandmarksDetected(List<Face.Landmark> landmarks);
    }
}
