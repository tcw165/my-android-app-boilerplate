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

package com.my.demo.bigbite.game;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.my.core.protocol.IProgressBarView;
import com.my.demo.bigbite.R;
import com.my.demo.bigbite.game.data.IBiteDetector;
import com.my.demo.bigbite.game.data.ICameraMetadata;
import com.my.demo.bigbite.game.detector.DLibBiteDetector;
import com.my.demo.bigbite.game.detector.DLibLandmarksDetector;
import com.my.demo.bigbite.game.event.FrameUiEvent;
import com.my.demo.bigbite.game.reactive.CameraObservable;
import com.my.demo.bigbite.game.reactive.LottieAnimObservable;
import com.my.demo.bigbite.game.view.CameraSourcePreview;
import com.my.demo.bigbite.game.view.FaceLandmarksOverlayView;
import com.my.demo.bigbite.util.DlibModelHelper;
import com.my.jni.dlib.DLibLandmarks68Detector;
import com.my.jni.dlib.IDLibFaceDetector;
import com.my.jni.dlib.data.DLibFace;
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
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class GameActivity
    extends AppCompatActivity
    implements ICameraMetadata,
               IProgressBarView {

    // View.
    @BindView(R.id.camera)
    CameraSourcePreview mCameraView;
    @BindView(R.id.overlay)
    FaceLandmarksOverlayView mDebugOverlayView;
    @BindView(R.id.txt_bite_count)
    TextView mBiteCountView;
    @BindView(R.id.animation_view)
    LottieAnimationView mCountDownView;
    ProgressDialog mProgressDialog;

    // Butter Knife.
    Unbinder mUnbinder;

    // DLibFace Detector.
    IDLibFaceDetector mLandmarksDetector;

    // Data.
    CompositeDisposable mDisposables;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_game);

        // Init view binding.
        mUnbinder = ButterKnife.bind(this);

        // The progress bar.
        mProgressDialog = new ProgressDialog(this);

        // Init the detectors.
        mLandmarksDetector = new DLibLandmarks68Detector();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // TODO: Check if the Google Play Service is present.
        mDisposables = new CompositeDisposable();

        // TODO: Can subject be canceled?
        // Define the preparation stream including granting permission and
        // initializing the DLib model.
        final Subject<UniverseEvent> preparationSub = PublishSubject.create();
        mDisposables.add(
            grantPermission()
                // Init DLib model.
                .observeOn(Schedulers.io())
                .flatMap(new Function<Boolean, ObservableSource<UniverseEvent>>() {
                    @Override
                    public ObservableSource<UniverseEvent> apply(Boolean granted)
                        throws Exception {
                        if (granted) {
                            return initFaceLandmarksDetector()
                                .startWith(false)
                                .onErrorReturnItem(false)
                                .map(new Function<Boolean, UniverseEvent>() {
                                    @Override
                                    public UniverseEvent apply(Boolean initialized)
                                        throws Exception {
                                        if (initialized) {
                                            return new UniverseEvent(UniverseEvent.MODEL_INITIALIZED);
                                        } else {
                                            return new UniverseEvent(UniverseEvent.MODEL_NOT_INITIALIZED);
                                        }
                                    }
                                });
                        } else {
                            return Observable.just(
                                new UniverseEvent(UniverseEvent.PERM_NOT_GRANTED));
                        }
                    }
                })
                // Init camera preview source.
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<UniverseEvent>() {
                    @Override
                    public void accept(UniverseEvent event)
                        throws Exception {
                        preparationSub.onNext(event);
                    }
                }));

        // Observe the preparation stream.
        mDisposables.add(
            preparationSub
                // Update UI.
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<UniverseEvent>() {
                    @Override
                    public void accept(UniverseEvent event)
                        throws Exception {
                        if (event.status == UniverseEvent.PERM_NOT_GRANTED) {
                            Toast.makeText(getApplicationContext(),
                                           R.string.warning_permission_not_granted,
                                           Toast.LENGTH_SHORT)
                                 .show();
                            finish();
                        } else if (event.status == UniverseEvent.MODEL_NOT_INITIALIZED) {
                            showProgressBar();
                        } else {
                            hideProgressBar();
                        }
                    }
                }));


        // The camera preview is 90 degree clockwise rotated.
        //  height
        // .------.
        // |      |
        // |      | width
        // |      |
        // '------'
        final int previewWidth = 320;
        final int previewHeight = 240;

        // Define the camera detection stream.
        final Subject<FrameUiEvent<DLibFace>> frameSub = PublishSubject.create();
        mDisposables.add(
            preparationSub
                // Camera.
                .skipWhile(new Predicate<UniverseEvent>() {
                    @Override
                    public boolean test(UniverseEvent event)
                        throws Exception {
                        return event.status != UniverseEvent.MODEL_INITIALIZED;
                    }
                })
                .switchMap(new Function<UniverseEvent, CameraObservable<DLibFace>>() {
                    @Override
                    public CameraObservable<DLibFace> apply(UniverseEvent event)
                        throws Exception {
                        // Set the preview config.
                        if (isPortraitMode()) {
                            mDebugOverlayView.setCameraPreviewSize(
                                previewHeight, previewWidth);
                        } else {
                            mDebugOverlayView.setCameraPreviewSize(
                                previewWidth, previewHeight);
                        }

                        // Create camera observable.
                        return CameraObservable.create(
                                GameActivity.this,
                                mCameraView,
                                previewWidth, previewHeight,
                                getFaceDetector());
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<FrameUiEvent<DLibFace>>() {
                    @Override
                    public void accept(FrameUiEvent<DLibFace> event)
                        throws Exception {
                        frameSub.onNext(event);
                    }
                }));

        // Observe the stream of camera first frame to count down.
        final LottieAnimObservable countDownAnim = new LottieAnimObservable(mCountDownView);
        mDisposables.add(
            frameSub
                // Start to play the count down animation at first frame emitted.
                .filter(new Predicate<FrameUiEvent<DLibFace>>() {
                    @Override
                    public boolean test(FrameUiEvent<DLibFace> event)
                        throws Exception {
                        return event.isFirstFrame();
                    }
                })
                // Update UI.
                .flatMap(new Function<FrameUiEvent<DLibFace>, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(FrameUiEvent<DLibFace> event) throws Exception {
                        mCountDownView.setVisibility(View.VISIBLE);
                        mCountDownView.setAnimation("animation/count_down_321.json");
                        return countDownAnim;
                    }
                })
                .subscribe());

        // Observe the stream of face detection to render the landmarks for
        // debugging.
        mDisposables.add(
            frameSub
                // Wait for the count down.
                .skipWhile(new Predicate<FrameUiEvent<DLibFace>>() {
                    @Override
                    public boolean test(FrameUiEvent<DLibFace> event)
                        throws Exception {
                        return countDownAnim.isAnimating();
                    }
                })
                // Update UI.
                .subscribe(new Consumer<FrameUiEvent<DLibFace>>() {
                    @Override
                    public void accept(FrameUiEvent<DLibFace> event)
                        throws Exception {
                        // Convert sparse array to array list.
                        final SparseArray<DLibFace> data = event.data;
                        final List<DLibFace> faces = new ArrayList<>();
                        for (int i = 0; i < data.size(); ++i) {
                            faces.add(data.get(data.keyAt(i)));
                        }

                        mDebugOverlayView.setFaces(faces);
                    }
                }));

        // Observe the stream of face detection to detect bite gesture and
        // react the result on the UI.
        final IBiteDetector biteDetector = new DLibBiteDetector();
        mDisposables.add(
            frameSub
                // Wait for the count down.
                .skipWhile(new Predicate<FrameUiEvent<DLibFace>>() {
                    @Override
                    public boolean test(FrameUiEvent<DLibFace> event)
                        throws Exception {
                        return countDownAnim.isAnimating();
                    }
                })
                // Detect bite pose.
                .map(new Function<FrameUiEvent<DLibFace>, Integer>() {
                    @Override
                    public Integer apply(FrameUiEvent<DLibFace> event)
                        throws Exception {
                        final SparseArray<DLibFace> faces = event.data;
                        if (faces.size() > 0) {
                            biteDetector.detect(faces.get(faces.keyAt(0)));
                        }
                        return biteDetector.getBiteCount();
                    }
                })
                // Update UI.
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer bitesCount)
                        throws Exception {
                        // Update count.
                        final int count = Integer.parseInt(
                            mBiteCountView.getText().toString());
                        if (bitesCount != count) {
                            mBiteCountView.setText(String.valueOf(bitesCount));
                        }
                    }
                }));
    }

    @Override
    protected void onPause() {
        super.onPause();

        hideProgressBar();

        // Close camera.
        mCameraView.release();

        mDisposables.clear();
    }

    @Override
    public boolean isFacingFront() {
        return CameraSource.CAMERA_FACING_FRONT ==
               mCameraView.getCameraSource()
                          .getCameraFacing();
    }

    @Override
    public boolean isFacingBack() {
        return CameraSource.CAMERA_FACING_BACK ==
               mCameraView.getCameraSource()
                          .getCameraFacing();
    }

    @Override
    public boolean isPortraitMode() {
        int orientation = getResources().getConfiguration().orientation;
        return orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    @Override
    public boolean isLandscapeMode() {
        int orientation = getResources().getConfiguration().orientation;
        return orientation == Configuration.ORIENTATION_LANDSCAPE;
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
        mProgressDialog.dismiss();
    }

    @Override
    public void updateProgress(int progress) {
        mProgressDialog.setProgress(progress);
        mProgressDialog.show();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // View binding.
        mUnbinder.unbind();
    }

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
                         Manifest.permission.ACCESS_NETWORK_STATE,
                         Manifest.permission.CAMERA);
        } else {
            return Observable.just(
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
        }
    }

    private Observable<Boolean> initFaceLandmarksDetector() {
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

                    if (!mLandmarksDetector.isFaceDetectorReady()) {
                        mLandmarksDetector.prepareFaceDetector();
                    }
                    if (!mLandmarksDetector.isFaceLandmarksDetectorReady()) {
                        mLandmarksDetector.prepareFaceLandmarksDetector(
                            face68ModelPath.getAbsolutePath());
                    }

                    return true;
                }
            });
    }

    private Context getContext() {
        return this;
    }

    public Detector<DLibFace> getFaceDetector() {
        // Create Google Vision face detector with FAST mode.
        final Detector<Face> faceDetector = new FaceDetector.Builder(getContext())
            .setClassificationType(FaceDetector.FAST_MODE)
            .setLandmarkType(FaceDetector.NO_LANDMARKS)
            .build();
        // Encapsulate the face detector with the landmarks detector.
        // The detector would directly draw the result onto the
        // given overlay view.
        return new DLibLandmarksDetector(
            this, faceDetector, mLandmarksDetector);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    private static final class UniverseEvent {

        static final int UNDEFINED = -1;

        static final int PERM_GRANTED = 1001;
        static final int PERM_NOT_GRANTED = 1002;

        static final int MODEL_NOT_INITIALIZED = 2001;
        static final int MODEL_INITIALIZED = 2002;

        static final int ON_DETECTING_BITE = 3001;

        int status = UNDEFINED;

        UniverseEvent(int status) {
            this.status = status;
        }
    }
}
