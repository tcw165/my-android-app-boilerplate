package com.my.boilerplate;

import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.my.widget.ElasticDragDismissLayout;

import java.io.IOException;
import java.util.List;

public class ViewOfCameraSampleActivity extends AppCompatActivity {

    Toolbar mToolbar;
    ElasticDragDismissLayout mLayout;

    Camera mCamera;
    SurfaceView mPreview;
    SurfaceHolder mHolder;
    List<Camera.Size> mSupportedPreviewSizes;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_of_camera_sample);
        // Disable the default window transition and let mLayout to handle it.
        overridePendingTransition(0, 0);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mLayout = (ElasticDragDismissLayout) findViewById(R.id.layout);
        mLayout.addOnDragDismissListener(new ElasticDragDismissLayout.SystemChromeFader(this) {
            @Override
            public void onDrag(float elasticOffset,
                               float elasticOffsetPixels,
                               float rawOffset,
                               float rawOffsetPixels) {
                super.onDrag(elasticOffset,
                             elasticOffsetPixels,
                             rawOffset,
                             rawOffsetPixels);
            }

            @Override
            public void onDragDismissed(float totalScroll) {
                Log.d("xyz", "onDragDismissed");
                finishWithResult();
            }

            @Override
            public void onBackPressedDismissed() {
                Log.d("xyz", "onBackPressedDismissed");
                finishWithResult();
            }

            @Override
            public void onCoverPressedDismissed() {
                Log.d("xyz", "onCoverPressedDismissed");
                finishWithResult();
            }
        });

        mPreview = (SurfaceView) findViewById(R.id.cameraPreview);

        mHolder = mPreview.getHolder();
        mHolder.addCallback(onSurfaceUpdate());
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Actively remove the listeners to prevent coupled reference.
        mLayout.removeAllOnDragDismissListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Open the layout with animation.
        mLayout.postOpen();

        // Open the camera.
        safeCameraOpen();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Close the camera.
        stopPreviewAndFreeCamera();
    }

    @Override
    public void onBackPressed() {
        // Close the layout with animation.
        mLayout.close();
    }

    public void finishWithResult() {
        // We cannot get the SupportFragmentManager to pop its stack when the
        // activity is paused by calling super.onBackPressed().
        ActivityCompat.finishAfterTransition(this);

        // Disable the window transition and let the launching activity handle
        // it.
        overridePendingTransition(0, 0);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    SurfaceHolder.Callback onSurfaceUpdate() {
        return new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (mCamera == null) return;

                try {
                    // It must be called after the surface is created and before
                    // the Camera#startPreview.
                    mCamera.setPreviewDisplay(mPreview.getHolder());
                } catch (IOException e) {
                    Log.e("xyz", "Failed to set preview.");
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder,
                                       int format,
                                       int width,
                                       int height) {
                // Now that the size is known, set up the camera parameters and
                // begin the preview.
                final Camera.Parameters params = mCamera.getParameters();
                if (mSupportedPreviewSizes != null &&
                    !mSupportedPreviewSizes.isEmpty()) {
                    for (Camera.Size size : mSupportedPreviewSizes) {
                        if (Math.min(mPreview.getWidth(), mPreview.getHeight()) >=
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
                if (mCamera != null) {
                    mCamera.stopPreview();
                }
            }
        };
    }

    boolean safeCameraOpen() {
        if (Camera.getNumberOfCameras() == 0) return false;

        try {
            stopPreviewAndFreeCamera();
            mCamera = Camera.open(0);
            if (mCamera != null) {
                mSupportedPreviewSizes = mCamera.getParameters()
                                                .getSupportedPreviewSizes();
            }
        } catch (Exception e) {
            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }

        return mCamera != null;
    }

    /**
     * When this function returns, mCamera will be null.
     */
    void stopPreviewAndFreeCamera() {
        if (mCamera != null) {
            // Call stopPreview() to stop updating the preview surface.
            mCamera.stopPreview();

            // Important: Call release() to release the camera for use by other
            // applications. Applications should release the camera immediately
            // during onPause() and re-open() it during onResume()).
            mCamera.release();

            mCamera = null;
        }
    }

    void setCameraDisplayOrientation() {
        final Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(0, info);

        int windowRotation = 0;
        switch (getWindowManager()
            .getDefaultDisplay()
            .getRotation()) {
            case Surface.ROTATION_0:windowRotation = 0; break;
            case Surface.ROTATION_90: windowRotation = 90; break;
            case Surface.ROTATION_180: windowRotation = 180; break;
            case Surface.ROTATION_270: windowRotation = 270; break;
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
}
