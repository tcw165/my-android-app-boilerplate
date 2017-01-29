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

package com.my.widget;

import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.my.widget.util.CameraUtil;

import java.io.IOException;

public class CameraSurfaceView
    extends SurfaceView
    implements SurfaceHolder.Callback {

    int mCameraId;
    boolean mIsPreviewing;
    boolean mIsSurfaceAvailable;

    static final String TAG = CameraSurfaceView.class.getSimpleName();
    // Because the Camera.open and Camera.setPreviewTexture take 100ms
    // individually, doing them in the other thread avoids
    Handler mHandler;
    Camera mCamera;

    public CameraSurfaceView(Context context) {
        this(context, null);
    }

    public CameraSurfaceView(Context context,
                             AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public synchronized void surfaceCreated(SurfaceHolder holder) {
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
    public synchronized void surfaceChanged(SurfaceHolder holder,
                                            final int format,
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
    public synchronized void surfaceDestroyed(SurfaceHolder holder) {
        // Ensure the handler.
        ensureHandler();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                closeCameraSync();
            }
        });
    }

    public void openCameraAsync() {
        // TODO: Support face camera
        openCameraAsync(0);
    }

    public void openCameraAsync(final int cameraId) {
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

            // (Takes around 178 ms)
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

        // Important: Call release() to release the camera for use by other
        // applications. Applications should release the camera immediately
        // during onPause() and re-open() it during onResume()).
        mCamera.release();
        mCamera = null;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        // Ensure the handler.
        ensureHandler();

        getHolder().addCallback(this);
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        // The handler.
        if (mHandler != null) {
            mHandler.getLooper().quit();
        }

        // Force to close the camera.
        closeCameraSync();

        getHolder().removeCallback(this);
    }

    void ensureHandler() {
        synchronized (this) {
            if (mHandler == null) {
                final HandlerThread thread = new HandlerThread(TAG);
                thread.start();

                mHandler = new Handler(thread.getLooper());
            }
        }
    }

    synchronized boolean isCameraOpenedAndSurfaceAvailable() {
        return mCamera != null && mIsSurfaceAvailable;
    }

    synchronized void setPreviewSize(int width,
                                     int height) {
        if (!isCameraOpenedAndSurfaceAvailable()) return;

        // Pause the preview before updating the preview size.
        if (mIsPreviewing) mCamera.stopPreview();

        // Now that the size is known, set up the camera parameters and
        // begin the preview.
        final Camera.Parameters params = mCamera.getParameters();
        final Camera.Size size = CameraUtil.getClosetPreviewSize(mCamera, width, height);
        params.setPreviewSize(size.width, size.height);
        mCamera.setParameters(params);

        // Resume the preview after updating the preview size.
        if (mIsPreviewing) mCamera.startPreview();
    }

    synchronized void safePreviewCamera() {
        if (!isCameraOpenedAndSurfaceAvailable()) return;
        if (mIsPreviewing) return;

        try {
            // TODO: Detect the orientation change automatically.
            // (Takes around 1 ms)
            // It must be called after the surface is created and before
            // the Camera#startPreview.
            mCamera.setPreviewDisplay(getHolder());
            // (Takes around 2 ms)
            // Setup the camera orientation.
            mCamera.setDisplayOrientation(
                CameraUtil.getDisplayOrientation(
                    getContext(),
                    mCameraId));
            // Find the most fit preview size.
            setPreviewSize(getWidth(), getHeight());
            // (Takes around 213 ms)
            mCamera.startPreview();

            mIsSurfaceAvailable = true;
        } catch (IOException e) {
            Log.e("xyz", "Failed to set preview.");
            e.printStackTrace();
        }
    }
}
