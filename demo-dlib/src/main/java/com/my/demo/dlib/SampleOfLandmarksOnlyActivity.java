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

package com.my.demo.dlib;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.cameraview.CameraView;
import com.google.protobuf.InvalidProtocolBufferException;
import com.my.core.protocol.IProgressBarView;
import com.my.core.util.FileUtil;
import com.my.core.util.ViewUtil;
import com.my.demo.dlib.view.FaceLandmarksImageView;
import com.my.jni.dlib.FaceLandmarksDetector;
import com.my.jni.dlib.data.Face;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class SampleOfLandmarksOnlyActivity
    extends AppCompatActivity
    implements IProgressBarView,
               Handler.Callback {

//    private static final String ASSET_TEST_PHOTO = "boyw165-i-am-tyson-chandler.jpg";
    private static final String ASSET_TEST_PHOTO = "5-ppl.jpg";
    private static final String ASSET_SHAPE_DETECTOR_DATA = "shape_predictor_68_face_landmarks.dat";

    private static final int MSG_TAKE_PHOTO = 1 << 1;
    private static final int MSG_DETECT_LANDMARKS = 1 << 2;

    // View.
    @BindView(R.id.btn_back)
    FloatingActionButton mBtnBack;
    @BindView(R.id.btn_take_photo)
    FloatingActionButton mBtnTakePhoto;
    @BindView(R.id.face_bound)
    View mFaceBoundView;
    @BindView(R.id.landmarks_preview)
    FaceLandmarksImageView mLandmarksPreview;
    @BindView(R.id.camera)
    CameraView mCameraView;

    // Butter Knife.
    Unbinder mUnbinder;

    // Face Detector.
    Handler mDetectorHandler;
    FaceLandmarksDetector mFaceDetector;

    // Data.
    Rect mFaceBound;
    byte[] mData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sample_of_landmarks_only);

        // Init view binding.
        mUnbinder = ButterKnife.bind(this);

        // Back button.
        mBtnBack.setOnClickListener(onClickToBack());

        // Camera view.
        mCameraView.addCallback(mCameraCallback);

        // Init the face detector.
        mFaceDetector = new FaceLandmarksDetector();
    }

    @Override
    protected void onResume() {
        super.onResume();

        grantPermission()
//            // Delay for waiting the layout process is finished.
//            .delay(1000, TimeUnit.MILLISECONDS)
            // Start face landmarks detection.
            .flatMap(new Function<Boolean, ObservableSource<?>>() {
                @Override
                public ObservableSource<?> apply(Boolean granted)
                    throws Exception {
                    if (granted) {
                        showProgressBar("Preparing the data...");
                        return processFaceLandmarksDetection()
                            .subscribeOn(Schedulers.io());
                    } else {
                        return Observable.just(0);
                    }
                }
            })
            .observeOn(AndroidSchedulers.mainThread())
            // Open the camera.
            .map(new Function<Object, Object>() {
                @Override
                public Object apply(Object value) throws Exception {
                    hideProgressBar();

                    // Prepare the capturing rect.
                    mFaceBound = new Rect(
                        mFaceBoundView.getLeft(),
                        mFaceBoundView.getTop(),
                        mFaceBoundView.getRight(),
                        mFaceBoundView.getBottom());

                    // Open the camera.
                    mCameraView.start();

                    return value;
                }
            })
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new DisposableObserver<Object>() {
                @Override
                public void onNext(Object value) {
                    // DO NOTHING.
                }

                @Override
                public void onError(Throwable e) {
                }

                @Override
                public void onComplete() {
                    hideProgressBar();
                }
            });
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Close camera.
        mCameraView.stop();
    }

    @Override
    public void showProgressBar() {
        ViewUtil
            .with(this)
            .setProgressBarCancelable(false)
            .showProgressBar(getString(R.string.loading));
    }

    @Override
    public void showProgressBar(String msg) {
        ViewUtil
            .with(this)
            .setProgressBarCancelable(false)
            .showProgressBar(msg);
    }

    @Override
    public void hideProgressBar() {
        ViewUtil.with(this)
                .hideProgressBar();
    }

    @Override
    public void updateProgress(int progress) {
        ViewUtil.with(this)
                .setProgressBarCancelable(false)
                .showProgressBar(null);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_TAKE_PHOTO:
                // Will lead to onPictureTaken callback.
                mCameraView.takePicture();
                return true;
            case MSG_DETECT_LANDMARKS:
                final Bitmap fullBitmap = BitmapFactory
                    .decodeByteArray(mData, 0, mData.length);

                try {
                    // Call detector JNI.
                    final List<Face.Landmark> landmarks =
                        mFaceDetector.findLandmarksInFace(fullBitmap, mFaceBound);

                    // Display the landmarks.
                    List<Face> faces = new ArrayList<>();
                    faces.add(new Face(landmarks));
                    mLandmarksPreview.setFaces(faces);
                } catch (InvalidProtocolBufferException err) {
                    Log.e("xyz", err.getMessage());
                }

                // Schedule next take-photo.
                mDetectorHandler.sendEmptyMessage(MSG_TAKE_PHOTO);
                return true;
        }

        return false;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Camera view.
        mCameraView.removeCallback(mCameraCallback);

        // View binding.
        mUnbinder.unbind();
    }

    private CameraView.Callback mCameraCallback =
        new CameraView.Callback() {
            @Override
            public void onCameraOpened(CameraView cameraView) {
                Log.d("xyz", "onCameraOpened");
                // Start a worker thread for detecting face landmarks.
                final HandlerThread worker = new HandlerThread(
                    SampleOfLandmarksOnlyActivity.class.getSimpleName());
                worker.start();
                mDetectorHandler = new Handler(worker.getLooper(),
                                               SampleOfLandmarksOnlyActivity.this);

                // Take photo after a short delay.
                mDetectorHandler.sendEmptyMessageDelayed(MSG_TAKE_PHOTO, 550);
            }

            @Override
            public void onCameraClosed(CameraView cameraView) {
                Log.d("xyz", "onCameraClosed");
                // Stop the worker thread for detecting face landmarks.
                mDetectorHandler.getLooper().quit();
            }

            @Override
            public void onPictureTaken(CameraView cameraView,
                                       final byte[] data) {
                Log.d("xyz", "onPictureTaken " + data.length + "(" + Looper.myLooper() + ")");

                mData = data;
                mDetectorHandler.sendEmptyMessage(MSG_DETECT_LANDMARKS);
//                getBackgroundHandler().post(new Runnable() {
//                    @Override
//                    public void run() {
//                        File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
//                                             "picture.jpg");
//                        OutputStream os = null;
//                        try {
//                            os = new FileOutputStream(file);
//                            os.write(data);
//                            os.close();
//                        } catch (IOException e) {
//                            Log.w("xyz", "Cannot write to " + file, e);
//                        } finally {
//                            if (os != null) {
//                                try {
//                                    os.close();
//                                } catch (IOException e) {
//                                    // Ignore
//                                }
//                            }
//                        }
//                    }
//                });
            }
        };

    private View.OnClickListener onClickToBack() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        };
    }

    private Observable<Boolean> grantPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return RxPermissions
                .getInstance(this)
                .request(Manifest.permission.READ_EXTERNAL_STORAGE,
                         Manifest.permission.WRITE_EXTERNAL_STORAGE,
                         Manifest.permission.CAMERA);
        } else {
            int check1 = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
            int check2 = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int check3 = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA);

            return Observable.just(check1 == PackageManager.PERMISSION_GRANTED &&
                                   check2 == PackageManager.PERMISSION_GRANTED &&
                                   check3 == PackageManager.PERMISSION_GRANTED);
        }
    }

    private Observable<?> processFaceLandmarksDetection() {
        final String dirName = getApplicationContext().getPackageName();
        final File dir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS + "/" + dirName);

        return Observable
            .zip(Observable
                     // Prepare the shape detector.
                     .fromCallable(new Callable<String>() {
                         @Override
                         public String call()
                             throws Exception {
                             if (dir.mkdirs() || dir.isDirectory()) {
                                 final File savedFile = new File(dir, ASSET_SHAPE_DETECTOR_DATA);

                                 // Copy asset to external disk.
                                 if (!savedFile.exists() && savedFile.createNewFile()) {
                                     FileUtil.copy(getAssets().open(ASSET_SHAPE_DETECTOR_DATA),
                                                   new FileOutputStream(savedFile));
                                 }

                                 return savedFile.getAbsolutePath();
                             } else {
                                 throw new IOException(
                                     String.format("Cannot copy %s to %s",
                                                   ASSET_SHAPE_DETECTOR_DATA,
                                                   dir.getAbsolutePath()));
                             }
                         }
                     })
                     .subscribeOn(Schedulers.io()),
                 // Prepare the testing photo.
                 Observable
                     .fromCallable(new Callable<String>() {
                         @Override
                         public String call()
                             throws Exception {
                             if (dir.mkdirs() || dir.isDirectory()) {
                                 final File savedFile = new File(dir, ASSET_TEST_PHOTO);

                                 // Copy asset to external disk.
                                 if (!savedFile.exists() && savedFile.createNewFile()) {
                                     FileUtil.copy(getAssets().open(ASSET_TEST_PHOTO),
                                                   new FileOutputStream(savedFile));
                                 }

                                 return savedFile.getAbsolutePath();
                             } else {
                                 throw new IOException(
                                     String.format("Cannot copy %s to %s",
                                                   ASSET_SHAPE_DETECTOR_DATA,
                                                   dir.getAbsolutePath()));
                             }
                         }
                     })
                     .subscribeOn(Schedulers.io()),
                 // Create the detector parameter.
                 new BiFunction<String, String, DetectorParams>() {
                     @Override
                     public DetectorParams apply(String shapeDetectorPath,
                                                 String testPhotoPath)
                         throws Exception {
                         return new DetectorParams(
                             shapeDetectorPath,
                             testPhotoPath);
                     }
                 })
            .subscribeOn(Schedulers.io())
            // Update progressbar message.
            .observeOn(AndroidSchedulers.mainThread())
            .map(new Function<DetectorParams, DetectorParams>() {
                @Override
                public DetectorParams apply(DetectorParams params) throws Exception {
                    showProgressBar("Initializing face detectors...");
                    return params;
                }
            })
            // Deserialize the detector.
            .observeOn(Schedulers.io())
            .map(new Function<DetectorParams, DetectorParams>() {
                @Override
                public DetectorParams apply(DetectorParams params)
                    throws Exception {
                    if (!mFaceDetector.isFaceDetectorReady()) {
                        mFaceDetector.prepareFaceDetector();
                    }
                    if (!mFaceDetector.isFaceLandmarksDetectorReady()) {
                        mFaceDetector.prepareFaceLandmarksDetector(
                            params.shapeDetectorPath);
                    }

                    return params;
                }
            });
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    private static class DetectorParams {

        final String shapeDetectorPath;
        final String testPhotoPath;

        DetectorParams(String shapeDetectorPath,
                       String testPhotoPath) {
            this.shapeDetectorPath = shapeDetectorPath;
            this.testPhotoPath = testPhotoPath;
        }
    }
}
