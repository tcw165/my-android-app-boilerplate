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
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.cameraview.CameraView;
import com.google.protobuf.InvalidProtocolBufferException;
import com.my.core.protocol.IProgressBarView;
import com.my.demo.dlib.util.DlibModelHelper;
import com.my.demo.dlib.view.FaceLandmarksOverlayView;
import com.my.jni.dlib.DLibLandmarks68Detector;
import com.my.jni.dlib.data.DLibFace;
import com.my.jni.dlib.data.DLibFace68;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
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
    @BindView(R.id.overlay)
    FaceLandmarksOverlayView mLandmarksPreview;
    @BindView(R.id.camera)
    CameraView mCameraView;
    ProgressDialog mProgressDialog;

    // Butter Knife.
    Unbinder mUnbinder;

    // Face Detector.
    Handler mDetectorHandler;
    DLibLandmarks68Detector mFaceDetector;

    // Data.
    RectF mFaceBound;
    byte[] mData;
    CompositeDisposable mComposition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sample_of_landmarks_only);

        // Init view binding.
        mUnbinder = ButterKnife.bind(this);

        // The progress bar.
        mProgressDialog = new ProgressDialog(this);

        // Back button.
        mBtnBack.setOnClickListener(onClickToBack());

        // Camera view.
        mCameraView.addCallback(mCameraCallback);

        // Init the face detector.
        mFaceDetector = new DLibLandmarks68Detector();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mComposition = new CompositeDisposable();
        mComposition.add(
            grantPermission()
                // Show the progress-bar.
                .map(new Function<Boolean, Boolean>() {
                    @Override
                    public Boolean apply(Boolean granted) throws Exception {
                        showProgressBar("Preparing the model...");
                        return granted;
                    }
                })
                // Face landmarks detection.
                .observeOn(Schedulers.io())
                .flatMap(new Function<Boolean, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(Boolean granted)
                        throws Exception {
                        if (granted) {
                            return startFaceLandmarksDetection();
                        } else {
                            return Observable.just(false);
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                // Open the camera.
                .map(new Function<Object, Object>() {
                    @Override
                    public Object apply(Object value) throws Exception {
                        // Prepare the capturing rect.
                        mFaceBound = new RectF(
                            (float) mFaceBoundView.getLeft() / mCameraView.getWidth(),
                            (float) mFaceBoundView.getTop() / mCameraView.getHeight(),
                            (float) mFaceBoundView.getRight() / mCameraView.getWidth(),
                            (float) mFaceBoundView.getBottom() / mCameraView.getHeight());

                        // Open the camera.
                        mCameraView.start();

                        return value;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    new Consumer<Object>() {
                        @Override
                        public void accept(Object o)
                            throws Exception {
                            // DO NOTHING.
                        }
                    },
                    new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable err)
                            throws Exception {
                            Log.e("xyz", err.getMessage());

                            hideProgressBar();

                            Toast.makeText(SampleOfLandmarksOnlyActivity.this,
                                           err.getMessage(), Toast.LENGTH_SHORT)
                                 .show();
                        }
                    },
                    new Action() {
                        @Override
                        public void run() throws Exception {
                            hideProgressBar();
                        }
                    }));
    }

    @Override
    protected void onPause() {
        super.onPause();

        hideProgressBar();

        mComposition.clear();

        // Close camera.
        mCameraView.stop();
    }

    @Override
    public void showProgressBar() {
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(getString(R.string.loading));
        mProgressDialog.show();
    }

    @Override
    public void showProgressBar(String msg) {
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(msg);
        mProgressDialog.show();
    }

    @Override
    public void hideProgressBar() {
        mProgressDialog.hide();
    }

    @Override
    public void updateProgress(int progress) {
        mProgressDialog.setProgress(progress);
        mProgressDialog.show();
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (!mCameraView.isCameraOpened() || isFinishing()) return true;

        switch (msg.what) {
            case MSG_TAKE_PHOTO:
                // Will lead to onPictureTaken callback.
                mCameraView.takePicture();
                return true;
            case MSG_DETECT_LANDMARKS:
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4;

                final Bitmap rawBitmap = BitmapFactory
                    .decodeByteArray(mData, 0, mData.length, options);
                // Optimized bitmap.
                Bitmap optBitmap;

                // FIXME: It's a workaround because I can't get rotation info
                // FIXME: from the CameraView.
                if (mCameraView.getFacing() == CameraView.FACING_FRONT) {
                    final int bw = rawBitmap.getWidth();
                    final int bh = rawBitmap.getHeight();
                    final Matrix matrix = new Matrix();

                    // Flip vertically.
                    matrix.postScale(1, -1, bw / 2, bh / 2);
                    // Adjust the width and height because shape of buffer
                    // doesn't match of the visible shape.
                    final int vw = mCameraView.getWidth();
                    final int vh = mCameraView.getHeight();
                    final float scale = Math.min((float) bw / vw,
                                                 (float) bh / vh);

                    optBitmap = Bitmap.createBitmap(rawBitmap, 0, 0,
                                                    (int) (scale * vw),
                                                    (int) (scale * vh),
                                                    matrix, true);
                } else {
                    optBitmap = Bitmap.createBitmap(rawBitmap, 0, 0,
                                                    rawBitmap.getWidth(),
                                                    rawBitmap.getHeight());
                }

                try {
                    // Do landmarks detection only
                    // Call detector JNI.
                    final int bw = optBitmap.getWidth();
                    final int bh = optBitmap.getHeight();
                    final Rect bound = new Rect(
                        (int) (mFaceBound.left * bw),
                        (int) (mFaceBound.top * bh),
                        (int) (mFaceBound.right * bw),
                        (int) (mFaceBound.bottom * bh));
                    final List<DLibFace.Landmark> landmarks =
                        mFaceDetector.findLandmarksFromFace(optBitmap, bound);

                    // Display the landmarks.
                    List<DLibFace> faces = new ArrayList<>();
                    faces.add(new DLibFace68(landmarks));
                    if (mLandmarksPreview != null) {
                        mLandmarksPreview.setFaces(faces);
                    }

//                    // Do face detection and then landmarks detection.
//                    // Call detector JNI.
//                    final List<DLibFace> faces =
//                        mFaceDetector.findFacesAndLandmarks(optBitmap);
//
//                    // Display the faces.
//                    if (mLandmarksPreview != null) {
//                        mLandmarksPreview.setFaces(faces);
//                    }
                } catch (InvalidProtocolBufferException err) {
                    Log.e("xyz", err.getMessage());
                }

                optBitmap.recycle();
                rawBitmap.recycle();

                // Schedule next take-photo.
                if (mCameraView.isCameraOpened()) {
                    mDetectorHandler.sendEmptyMessageDelayed(MSG_TAKE_PHOTO, 200);
                }
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
                mDetectorHandler.sendEmptyMessageDelayed(MSG_TAKE_PHOTO, 1000);
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

    private Observable<?> startFaceLandmarksDetection() {
        return DlibModelHelper
            .getService()
            // Download the trained model.
            .downloadFace68Model(
                this,
                getApplicationContext().getPackageName())
            // Update progressbar message.
            .observeOn(AndroidSchedulers.mainThread())
            .map(new Function<File, File>() {
                @Override
                public File apply(File face68ModelPath) throws Exception {
                    showProgressBar("Initializing face detectors...");
                    return face68ModelPath;
                }
            })
            // Deserialize the detector.
            .observeOn(Schedulers.io())
            .map(new Function<File, Boolean>() {
                @Override
                public Boolean apply(File face68ModelPath)
                    throws Exception {
                    if (face68ModelPath == null || !face68ModelPath.exists()) {
                        throw new RuntimeException(
                            "The face68 model is invalid.");
                    }

                    if (!mFaceDetector.isFaceDetectorReady()) {
                        mFaceDetector.prepareFaceDetector();
                    }
                    if (!mFaceDetector.isFaceLandmarksDetectorReady()) {
                        mFaceDetector.prepareFaceLandmarksDetector(
                            face68ModelPath.getAbsolutePath());
                    }

                    return true;
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
