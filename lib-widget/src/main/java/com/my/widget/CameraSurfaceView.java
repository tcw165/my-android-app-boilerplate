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
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.my.widget.util.CameraUtil;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class CameraSurfaceView extends SurfaceView {

    int mCameraId;
    // TODO: Use Handler and its MessageQueue instead?
    AtomicBoolean mIsSurfaceAvailable = new AtomicBoolean(false);

    Camera mCamera;

    public CameraSurfaceView(Context context) {
        this(context, null);
    }

    public CameraSurfaceView(Context context,
                             AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean openCamera() {
        // TODO: Support face camera
        return openCamera(0);
    }

    public boolean openCamera(final int cameraId) {
        if (cameraId < 0 && cameraId >= Camera.getNumberOfCameras()) return false;

        try {
            closeCamera();

            final long start = System.currentTimeMillis();
            // TODO: Takes around 178 ms, run it in the background?
            mCamera = Camera.open(cameraId);
            final long end = System.currentTimeMillis();
            Log.d("xyz", "Camera.open takes " + (end - start) + " ms.");
            mCameraId = cameraId;
        } catch (Exception e) {
            Log.e("xyz", "failed to open Camera " + cameraId);
            e.printStackTrace();
        }

        safePreviewCamera();

        return mCamera != null;
    }

    public void closeCamera() {
        if (mCamera == null) return;

        // Call stopPreview() to stop updating the preview surface.
        mCamera.stopPreview();

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

        getHolder().addCallback(mSurfaceCallback);
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mCamera != null) {
            mCamera.stopPreview();
        }

        getHolder().removeCallback(mSurfaceCallback);
    }

    SurfaceHolder.Callback mSurfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mIsSurfaceAvailable.set(true);

            if (mCamera == null) return;

            safePreviewCamera();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder,
                                   int format,
                                   int width,
                                   int height) {
            if (!isCameraOpenedAndSurfaceCreated()) return;

            // Stop the preview before updating the parameters.
            mCamera.stopPreview();

            // Now that the size is known, set up the camera parameters and
            // begin the preview.
            final Camera.Parameters params = mCamera.getParameters();
            final Camera.Size size = CameraUtil.getClosetPreviewSize(mCamera, width, height);
            params.setPreviewSize(size.width, size.height);
            mCamera.setParameters(params);

            // Important: Call startPreview() to start updating the preview surface.
            // Preview must be started before you can take a picture.
            // TODO: Takes around 213 ms, run it in the background?
            mCamera.startPreview();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            mIsSurfaceAvailable.set(false);
            closeCamera();
        }
    };

    protected boolean isCameraOpenedAndSurfaceCreated() {
        return mCamera != null && mIsSurfaceAvailable.get();
    }

    protected void safePreviewCamera() {
        if (!isCameraOpenedAndSurfaceCreated()) return;

        try {
            // TODO: Takes around 1 ms, run it in the background?
            // TODO: Detect the orientation change automatically.
            // It must be called after the surface is created and before
            // the Camera#startPreview.
            long start = System.currentTimeMillis();
            mCamera.setPreviewDisplay(getHolder());
            long end = System.currentTimeMillis();
            Log.d("xyz", "Camera.setPreviewDisplay takes " + (end - start) + " ms.");

            // TODO: Takes around 2 ms, run it in the background?
            // Setup the camera orientation.
            start = System.currentTimeMillis();
            mCamera.setDisplayOrientation(
                CameraUtil.getDisplayOrientation(
                    getContext(),
                    mCameraId));
            end = System.currentTimeMillis();
            Log.d("xyz", "Camera.setDisplayOrientation takes " + (end - start) + " ms.");

            // TODO: Takes around 213 ms, run it in the background?
            start = System.currentTimeMillis();
            mCamera.startPreview();
            end = System.currentTimeMillis();
            Log.d("xyz", "Camera.startPreview takes " + (end - start) + " ms.");
        } catch (IOException e) {
            Log.e("xyz", "Failed to set preview.");
            e.printStackTrace();
        }
    }
}
