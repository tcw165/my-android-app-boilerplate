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

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class CameraSurfaceView extends SurfaceView {

    AtomicBoolean mIsSurfaceCreated = new AtomicBoolean(false);

    Camera mCamera;
    List<Camera.Size> mSupportedPreviewSizes;

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

    public boolean openCamera(final int camera) {
        if (Camera.getNumberOfCameras() <= camera) return false;

        try {
            closeCamera();
            mCamera = Camera.open(camera);
            mSupportedPreviewSizes = mCamera.getParameters()
                                            .getSupportedPreviewSizes();
        } catch (Exception e) {
            Log.e("xyz", "failed to open Camera " + camera);
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
            mIsSurfaceCreated.set(true);

            if (mCamera == null) return;

            safePreviewCamera();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder,
                                   int format,
                                   int width,
                                   int height) {
            if (mCamera == null) return;

            // Stop the preview before updating the parameters.
            mCamera.stopPreview();

            // Now that the size is known, set up the camera parameters and
            // begin the preview.
            final Camera.Parameters params = mCamera.getParameters();
            if (mSupportedPreviewSizes != null &&
                !mSupportedPreviewSizes.isEmpty()) {
                for (Camera.Size size : mSupportedPreviewSizes) {
                    if (Math.min(width, height) >=
                        Math.max(size.width, size.height)) {
                        params.setPreviewSize(size.width, size.height);

                        mCamera.setParameters(params);
                        break;
                    }
                }
            }

            // Setup the camera orientation.
            setCameraDisplayOrientation();

            // Important: Call startPreview() to start updating the preview surface.
            // Preview must be started before you can take a picture.
            mCamera.startPreview();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            mIsSurfaceCreated.set(false);

            if (mCamera == null) return;

            mCamera.stopPreview();
        }
    };

    protected boolean isCameraOpenedAndSurfaceCreated() {
        return mCamera != null && mIsSurfaceCreated.get();
    }

    protected void safePreviewCamera() {
        if (!isCameraOpenedAndSurfaceCreated()) return;

        try {
            // It must be called after the surface is created and before
            // the Camera#startPreview.
            mCamera.setPreviewDisplay(getHolder());
            mCamera.startPreview();
        } catch (IOException e) {
            Log.e("xyz", "Failed to set preview.");
            e.printStackTrace();
        }
    }

    protected void setCameraDisplayOrientation() {
        // TODO: Detect the orientation change automatically.
        final Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(0, info);

        int windowRotation = 0;
        switch (getActivity()
            .getWindowManager()
            .getDefaultDisplay()
            .getRotation()) {
            case Surface.ROTATION_0:
                windowRotation = 0;
                break;
            case Surface.ROTATION_90:
                windowRotation = 90;
                break;
            case Surface.ROTATION_180:
                windowRotation = 180;
                break;
            case Surface.ROTATION_270:
                windowRotation = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + windowRotation) % 360;
            // compensate the mirror
            result = (360 - result) % 360;
        } else {
            // back-facing
            result = (info.orientation - windowRotation + 360) % 360;
        }

        mCamera.setDisplayOrientation(result);
    }

    Activity getActivity() {
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }
}
