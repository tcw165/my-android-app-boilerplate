package com.my.boilerplate;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.my.widget.CameraTextureView;
import com.my.widget.ElasticDragDismissLayout;

public class ViewOfCameraSampleActivity extends AppCompatActivity {

    ElasticDragDismissLayout mLayout;
//    CameraSurfaceView mCameraView;
    CameraTextureView mCameraView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_of_camera_sample);
        // Disable the default window transition and let mLayout to handle it.
        overridePendingTransition(0, 0);

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

//        mCameraView = (CameraSurfaceView) findViewById(R.id.cameraPreview);
        mCameraView = (CameraTextureView) findViewById(R.id.cameraPreview);
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
        mCameraView.openCameraAsync();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Close the camera.
        mCameraView.closeCameraAsync();
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

}
