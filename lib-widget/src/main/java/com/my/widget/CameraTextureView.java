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
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;

import com.my.widget.util.CameraUtil;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class CameraTextureView
    extends TextureView
    implements TextureView.SurfaceTextureListener {

    int mCameraId;

    Camera mCamera;
    WeakReference<SurfaceTexture> mSurface;

    public CameraTextureView(Context context) {
        this(context, null);
    }

    public CameraTextureView(Context context,
                             AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface,
                                          int width,
                                          int height) {
        mSurface = new WeakReference<>(surface);
        safePreviewCamera();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface,
                                            int width,
                                            int height) {
        if (!isCameraOpenedAndSurfaceCreated()) return;

        // Now that the size is known, set up the camera parameters and
        // begin the preview.
        final Camera.Parameters params = mCamera.getParameters();
        final Camera.Size size = CameraUtil.getClosetPreviewSize(mCamera, width, height);
        params.setPreviewSize(size.width, size.height);
        mCamera.setParameters(params);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mSurface != null) {
            mSurface.clear();
            mSurface = null;
        }

        closeCamera();

        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // Invoked when the specified SurfaceTexture is updated through
        // SurfaceTexture.updateTexImage().
    }

    public boolean openCamera() {
        // TODO: Support face camera
        return openCamera(0);
    }

    public boolean openCamera(final int cameraId) {
        if (Camera.getNumberOfCameras() <= cameraId) return false;

        try {
            closeCamera();
            mCamera = Camera.open(cameraId);
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

        if (isHardwareAccelerated()) {
            setSurfaceTextureListener(this);
        } else {
            // TODO: Warn the user or throw an exception.
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        setSurfaceTextureListener(null);
    }

    protected boolean isCameraOpenedAndSurfaceCreated() {
        return mCamera != null && mSurface != null;
    }

    protected void safePreviewCamera() {
        if (!isCameraOpenedAndSurfaceCreated()) return;

        try {
            // It must be called after the surface is created and before
            // the Camera#startPreview.
            mCamera.setPreviewTexture(mSurface.get());
            mCamera.setDisplayOrientation(
                CameraUtil.getDisplayOrientation(
                    getContext(),
                    mCameraId));
            mCamera.startPreview();
        } catch (IOException e) {
            Log.e("xyz", "Failed to set preview.");
            e.printStackTrace();
        }
    }
}
