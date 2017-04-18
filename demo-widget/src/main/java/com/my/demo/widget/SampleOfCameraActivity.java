package com.my.demo.widget;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.my.widget.CameraTextureView;
import com.my.widget.ElasticDragDismissLayout;
import com.my.widget.ElasticDragDismissLayout.OnDragDismissCallback;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class SampleOfCameraActivity
    extends AppCompatActivity
    implements CameraTextureView.OnImageClassifiedCallback {

    /**
     * Needed because it asks the permission in the onResume function and the
     * RxPermission launch a delegate activity for handling the permission
     * request. Otherwise the request would be fired for two times.
     */
    boolean mPermSettling;

    ElasticDragDismissLayout mLayout;
    TextView mDescripView;
    //    CameraSurfaceView mCameraView;
    CameraTextureView mCameraView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sample_of_camera);
        // Disable the default window transition and let mLayout to handle it.
        overridePendingTransition(0, 0);

        mLayout = (ElasticDragDismissLayout) findViewById(R.id.layout);
        mLayout.addOnDragDismissListener(onDragDismissListener());

        mDescripView = (TextView) findViewById(R.id.description);

        // Use SurfaceView.
//        mCameraView = (CameraSurfaceView) findViewById(R.id.cameraPreview);
        // Use TextureView.
        mCameraView = (CameraTextureView) findViewById(R.id.cameraPreview);
        mCameraView.setOnClassifyPreviewListener(this);
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

        // Open the camera.
        if (mLayout.isOpened()) {
            grantPermissionAndOpenCameraAsync();
        } else {
            // Open the layout with animation. Once the animation finishes,
            // open the camera.
            mLayout.postOpen(new Runnable() {
                @Override
                public void run() {
                    grantPermissionAndOpenCameraAsync();
                }
            });
        }
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
        mLayout.close(null);
    }

    @Override
    public void onImageClassified(final String description) {
        mDescripView.setText(description);
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

    void grantPermissionAndOpenCameraAsync() {
        // Check the permission and open the camera.
        if (!mPermSettling) {
            final int permCheck = ContextCompat.checkSelfPermission(
                SampleOfCameraActivity.this,
                Manifest.permission.CAMERA);
            if (permCheck != PackageManager.PERMISSION_GRANTED) {
                mPermSettling = true;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Observable
                        .just(true)
                        .delay(400, TimeUnit.MILLISECONDS)
                        .flatMap(new Function<Boolean, ObservableSource<Boolean>>() {
                            @Override
                            public ObservableSource<Boolean> apply(Boolean ignored)
                                throws Exception {
                                return RxPermissions
                                    .getInstance(SampleOfCameraActivity.this)
                                    .request(Manifest.permission.CAMERA);
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean granted)
                                throws Exception {
                                if (isFinishing()) return;
                                if (granted) {
                                    mCameraView.openCameraAsync();
                                    mPermSettling = false;
                                } else {
                                    onBackPressed();
                                }
                            }
                        });
                }
//                else {
//                    // TODO: Use a PermissionDelegateActivity instead?
//                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
//                                               Uri.fromParts("package", getPackageName(), null));
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent);
//                }
            } else {
                mCameraView.openCameraAsync();
                mPermSettling = false;
            }
        }
    }

    private OnDragDismissCallback onDragDismissListener() {
        return new ElasticDragDismissLayout.SystemChromeFader(this) {
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
            public void onDismissByDragOver(float totalScroll) {
                Log.d("xyz", "onDismissByDragOver");
                finishWithResult();
            }

            @Override
            public void onDismissByBackPressed() {
                Log.d("xyz", "onDismissByBackPressed");
                finishWithResult();
            }

            @Override
            public void onDismissByBgPressed() {
                Log.d("xyz", "onDismissByBgPressed");
                finishWithResult();
            }
        };
    }
}
