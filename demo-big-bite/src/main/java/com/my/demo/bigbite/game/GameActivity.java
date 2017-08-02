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
import android.content.res.Configuration;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.my.core.protocol.IProgressBarView;
import com.my.demo.bigbite.R;
import com.my.reactive.result.AnimResult;
import com.my.demo.bigbite.game.event.result.LoadDetectorResult;
import com.my.reactive.result.PermissionResult;
import com.my.reactive.result.RxResult;
import com.my.reactive.uiModel.UiModel;
import com.my.reactive.uiEvent.AnimUiEvent;
import com.my.reactive.result.MsgProgressResult;
import com.my.demo.bigbite.game.data.ICameraMetadata;
import com.my.demo.bigbite.game.detector.DLibLandmarksDetector;
import com.my.demo.bigbite.game.uiModel.BiteUiModel;
import com.my.demo.bigbite.game.event.action.DetectBiteAction;
import com.my.demo.bigbite.game.event.result.DetectBiteResult;
import com.my.demo.bigbite.game.event.result.FileResult;
import com.my.demo.bigbite.game.event.uiEvent.FrameUiEvent;
import com.my.demo.bigbite.game.reactive.CameraObservable;
import com.my.demo.bigbite.game.reactive.LottieAnimObservable;
import com.my.demo.bigbite.game.view.CameraSourcePreview;
import com.my.demo.bigbite.game.view.FaceLandmarksOverlayView;
import com.my.demo.bigbite.protocol.Common;
import com.my.demo.bigbite.start.data.ChallengeItem;
import com.my.demo.bigbite.util.DLibModelHelper;
import com.my.jni.dlib.DLibLandmarks68Detector;
import com.my.jni.dlib.IDLibFaceDetector;
import com.my.jni.dlib.data.DLibFace;
import com.my.reactive.result.TickResult;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

public class GameActivity
    extends AppCompatActivity
    implements ICameraMetadata,
               IProgressBarView {

    private static final int PREVIEW_WIDTH = 320;
    private static final int PREVIEW_HEIGHT = 240;

    // View.
    @BindView(R.id.main)
    ConstraintLayout mMainView;
    @BindView(R.id.camera)
    CameraSourcePreview mCameraView;
    @BindView(R.id.overlay)
    FaceLandmarksOverlayView mDebugOverlayView;
    @BindView(R.id.txt_bite_count)
    TextView mBiteCountView;
    @BindView(R.id.food_view)
    AppCompatImageView mFoodView;
    @BindView(R.id.food_scrap_view)
    LottieAnimationView mFoodScrapView;
    @BindView(R.id.count_down_view)
    LottieAnimationView mCountDownView;

    ProgressDialog mProgressDialog;

    ConstraintSet mConstraintSet;

    // Butter Knife.
    Unbinder mUnbinder;

    // Image Loader.
    RequestManager mGlide;

    // DLibFace Detector.
    IDLibFaceDetector mLandmarksDetector;

    // Data.
    CompositeDisposable mDisposables;
    RxPermissions mRxPermissions;
    ChallengeItem mChallengeItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_game);

        // Init view binding.
        mUnbinder = ButterKnife.bind(this);

        // Init progress bar.
        mProgressDialog = new ProgressDialog(this);

        // Init the detectors.
        mLandmarksDetector = new DLibLandmarks68Detector();

        // Init the image loader.
        mGlide = Glide.with(this);

        // Init rx-permissions.
        mRxPermissions = new RxPermissions(this);

        // Init constraint set.
        mConstraintSet = new ConstraintSet();
        mConstraintSet.clone(mMainView);

        // Get the challenge.
        mChallengeItem = getIntent().getExtras().getParcelable(Common.PARAMS_DATA);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // TODO: Check if the Google Play Service is present.
        mDisposables = new CompositeDisposable();

//        // TODO: Can subject be canceled?
//        // Define the preparation stream including granting permission and
//        // initializing the DLib model.
//        final Subject<UniverseEvent> preparationSub = PublishSubject.create();
        mDisposables.add(
            // Stream starts from granting for permissions.
            grantPermission()
                // Download DLib detector.
                .compose(downloadFaceDetectorIfOk(Schedulers.io()))
                // Load DLib detector into memory.
                .compose(loadFaceDetectorIfOk(Schedulers.io()))
                // Start game.
                .compose(startGameIfOk())
                // State accumulator (from result to UI model).
                .scan(UiModel.idle(new Object()), new BiFunction<UiModel<Object>, RxResult, UiModel<Object>>() {
                    @Override
                    public UiModel<Object> apply(UiModel<Object> model,
                                                 RxResult result)
                        throws Exception {
                        Log.d("yyy", result.toString());

                        if (result instanceof MsgProgressResult) {
                            final String msg = ((MsgProgressResult) result).message;

                            if (result.isSuccessful) {
                                return UiModel.succeed((Object) msg);
                            } else if (result.isInProgress) {
                                return UiModel.inProgress((Object) msg);
                            } else {
                                return UiModel.failed(result.err);
                            }
                        }

                        return model;
                    }
                })
                // React to the UI model.
                .observeOn(AndroidSchedulers.mainThread())
                .doOnDispose(new Action() {
                    @Override
                    public void run() throws Exception {
                        hideProgressBar();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<UiModel<Object>>() {
                    @Override
                    public void accept(UiModel<Object> model)
                        throws Exception {
                        // If the bundle is a String, it must be for the progress bar.
                        if (model.bundle instanceof String) {
                            final String msg = (String) model.bundle;

                            if (model.isSuccessful) {
                                hideProgressBar();
                            } else if (model.isInProgress) {
                                showProgressBar(msg);
                            } else {
                                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }));
//                .subscribe(new Consumer<RxResult>() {
//                    @Override
//                    public void accept(RxResult result)
//                        throws Exception {
//                        Log.d("xyz", result.toString());
//                    }
//                }));
//                .subscribe(new Consumer<UniverseEvent>() {
//                    @Override
//                    public void accept(UniverseEvent event)
//                        throws Exception {
//                        preparationSub.onNext(event);
//                    }
//                }));

//        // Observe the preparation stream.
//        mDisposables.add(
//            preparationSub
//                // Update UI.
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Consumer<UniverseEvent>() {
//                    @Override
//                    public void accept(UniverseEvent event)
//                        throws Exception {
//                        if (event.status == UniverseEvent.PERM_NOT_GRANTED) {
//                            Toast.makeText(getApplicationContext(),
//                                           R.string.warning_permission_not_granted,
//                                           Toast.LENGTH_SHORT)
//                                 .show();
//                            finish();
//                        } else if (event.status == UniverseEvent.MODEL_NOT_INITIALIZED) {
//                            showProgressBar();
//                        } else {
//                            hideProgressBar();
//                        }
//                    }
//                }));
//
//
//        // The camera preview is 90 degree clockwise rotated.
//        //  height
//        // .------.
//        // |      |
//        // |      | width
//        // |      |
//        // '------'
//        // Define the camera detection stream.
//        final Subject<FrameUiEvent<DLibFace>> frameSub = PublishSubject.create();
//        mDisposables.add(
//            preparationSub
//                // Camera.
//                .skipWhile(new Predicate<UniverseEvent>() {
//                    @Override
//                    public boolean test(UniverseEvent event)
//                        throws Exception {
//                        return event.status != UniverseEvent.MODEL_INITIALIZED;
//                    }
//                })
//                .switchMap(new Function<UniverseEvent, CameraObservable<DLibFace>>() {
//                    @Override
//                    public CameraObservable<DLibFace> apply(UniverseEvent event)
//                        throws Exception {
//                        // Set the preview config.
//                        if (isPortraitMode()) {
//                            mDebugOverlayView.setCameraPreviewSize(
//                                PREVIEW_HEIGHT, PREVIEW_WIDTH);
//                        } else {
//                            mDebugOverlayView.setCameraPreviewSize(
//                                PREVIEW_WIDTH, PREVIEW_HEIGHT);
//                        }
//
//                        // Create camera observable.
//                        return CameraObservable.create(
//                            GameActivity.this,
//                            mCameraView,
//                            PREVIEW_WIDTH, PREVIEW_HEIGHT,
//                            getFaceDetector());
//                    }
//                })
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Consumer<FrameUiEvent<DLibFace>>() {
//                    @Override
//                    public void accept(FrameUiEvent<DLibFace> event)
//                        throws Exception {
//                        frameSub.onNext(event);
//                    }
//                }));

//        // FIXME: This will cause UI race condition.
//        // Observe the stream of camera first frame to count down.
//        final LottieAnimObservable countDownAnim = new LottieAnimObservable(mCountDownView);
//        mDisposables.add(
//            frameSub
//                // Start to play the count down animation at first frame emitted.
//                .filter(new Predicate<FrameUiEvent<DLibFace>>() {
//                    @Override
//                    public boolean test(FrameUiEvent<DLibFace> event)
//                        throws Exception {
//                        return event.isFirstFrame();
//                    }
//                })
//                // Update UI.
//                .flatMap(new Function<FrameUiEvent<DLibFace>, ObservableSource<?>>() {
//                    @Override
//                    public ObservableSource<?> apply(FrameUiEvent<DLibFace> event) throws Exception {
//                        mCountDownView.setVisibility(View.VISIBLE);
//                        mCountDownView.setAnimation("animation/count_down_321.json");
//                        return countDownAnim;
//                    }
//                })
//                .subscribe());
//
//        // Observe the stream of face detection to render the landmarks for
//        // debugging.
//        mDisposables.add(
//            frameSub
//                // Wait for the count down.
//                .filter(new Predicate<FrameUiEvent<DLibFace>>() {
//                    @Override
//                    public boolean test(FrameUiEvent<DLibFace> event)
//                        throws Exception {
//                        return !countDownAnim.isAnimating();
//                    }
//                })
//                // Update UI.
//                .subscribe(new Consumer<FrameUiEvent<DLibFace>>() {
//                    @Override
//                    public void accept(FrameUiEvent<DLibFace> event)
//                        throws Exception {
//                        // Convert sparse array to array list.
//                        final SparseArray<DLibFace> data = event.data;
//                        final List<DLibFace> faces = new ArrayList<>();
//                        for (int i = 0; i < data.size(); ++i) {
//                            faces.add(data.get(data.keyAt(i)));
//                        }
//
//                        mDebugOverlayView.setFaces(faces);
//                    }
//                }));
//
//        // Observe the stream of face detection to detect bite gesture and
//        // react the result on the UI.
//        mDisposables.add(
//            frameSub
//                // Wait for the count down.
//                .filter(new Predicate<FrameUiEvent<DLibFace>>() {
//                    @Override
//                    public boolean test(FrameUiEvent<DLibFace> event)
//                        throws Exception {
//                        return !countDownAnim.isAnimating() &&
//                               event.data.size() > 0;
//                    }
//                })
//                // Detect bite pose.
//                .map(toDetectBiteAction())
//                .compose(new DLibBiteDetectorTransformer(
//                    new DLibBiteDetector(),
//                    Schedulers.computation()))
//                .map(toBiteUiModel())
//                // Update UI.
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(onBiteDetected()));
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

        // Image Loader.
        mGlide.onDestroy();
    }

    private View.OnClickListener onClickToBack() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        };
    }

    private Consumer<? super BiteUiModel> onBiteDetected() {
        return new Consumer<BiteUiModel>() {
            @Override
            public void accept(BiteUiModel model)
                throws Exception {

                // Update the food position.
                if (isPortraitMode()) {
                    final float scale = Math.min((float) mMainView.getWidth() / PREVIEW_HEIGHT,
                                                 (float) mMainView.getHeight() / PREVIEW_WIDTH);
                    final int canvasWidth = (int) (scale * PREVIEW_HEIGHT);
                    final int canvasHeight = (int) (scale * PREVIEW_WIDTH);
                    final RectF mouthBound = model.mouthBound;

                    final int width = (int) (mouthBound.width() * canvasWidth);
                    final int left = (int) (mouthBound.left * canvasWidth);
                    final int top = (int) (mouthBound.top * canvasHeight);

                    mConstraintSet.setGuidelineBegin(
                        R.id.guideline_food_left, left + (width - mFoodView.getWidth()) / 2);
                    mConstraintSet.setGuidelineBegin(
                        R.id.guideline_food_top, top);
                    mConstraintSet.applyTo(mMainView);
                }

                // Update the text and food image.
                if (!Integer.toString(model.biteCount).equalsIgnoreCase(
                    mBiteCountView.getText().toString())) {
                    final int size = mChallengeItem.getSpriteUrls().size();
                    if (size > 0) {
                        mGlide.load(mChallengeItem.getSpriteUrls().get(model.biteCount % size))
                              .into(mFoodView);
                    }

                    mBiteCountView.setText(String.valueOf(model.biteCount));
                }
            }
        };
    }

    private Context getContext() {
        return this;
    }

    private Observable<RxResult> grantPermission() {
        return mRxPermissions
            .request(Manifest.permission.READ_EXTERNAL_STORAGE,
                     Manifest.permission.WRITE_EXTERNAL_STORAGE,
                     Manifest.permission.ACCESS_NETWORK_STATE,
                     Manifest.permission.CAMERA)
            .map(new Function<Boolean, RxResult>() {
                @Override
                public RxResult apply(Boolean granted)
                    throws Exception {
                    if (granted) {
                        return PermissionResult.succeed();
                    } else {
                        return PermissionResult.failed(new RuntimeException(
                            "Permissions denied."));
                    }
                }
            })
            .startWith(PermissionResult.inProgress());
    }

    private ObservableTransformer<RxResult, RxResult> downloadFaceDetectorIfOk(final Scheduler worker) {
        return new ObservableTransformer<RxResult, RxResult>() {
            @Override
            public ObservableSource<RxResult> apply(final Observable<RxResult> upstream) {
                return upstream
                    .flatMap(new Function<RxResult, ObservableSource<RxResult>>() {
                        @Override
                        public ObservableSource<RxResult> apply(RxResult result)
                            throws Exception {
                            // upstream   --[?]-[?]--[success]-----------------------------------
                            //
                            //                            -* transformer *-
                            //
                            // downstream --[?]-[?]--[success]-[in]-[0]-[1]-...-[success]-[file]-
                            if (result.isSuccessful) {
                                return Observable.merge(
                                    Observable.just(result),
                                    DLibModelHelper
                                        .getService()
                                        .downloadFace68Model(
                                            getApplicationContext(),
                                            getApplicationContext().getPackageName())
                                        .subscribeOn(worker)
                                        .flatMap(new Function<File, ObservableSource<RxResult>>() {
                                            @Override
                                            public ObservableSource<RxResult> apply(File file)
                                                throws Exception {
                                                return Observable.merge(
                                                    Observable.just(MsgProgressResult.succeed(
                                                        "Downloading the face detector...done")),
                                                    Observable.just(FileResult.succeed(file))
                                                );
                                            }
                                        })
                                        .startWith(MsgProgressResult.inProgress(
                                            "Downloading the face detector...", 0))
                                        .onErrorReturn(new Function<Throwable, RxResult>() {
                                            @Override
                                            public RxResult apply(Throwable err)
                                                throws Exception {
                                                return FileResult.failed(err);
                                            }
                                        }));
                            } else {
                                return Observable.just(result);
                            }
                        }
                    });
            }
        };
    }

    private ObservableTransformer<RxResult, RxResult> loadFaceDetectorIfOk(final Scheduler worker) {
        return new ObservableTransformer<RxResult, RxResult>() {
            @Override
            public ObservableSource<RxResult> apply(Observable<RxResult> upstream) {
                return upstream
                    // TODO: It's like a mapper mapping from result to another observable.
                    .flatMap(new Function<RxResult, ObservableSource<RxResult>>() {
                        @Override
                        public ObservableSource<RxResult> apply(final RxResult result)
                            throws Exception {
                            if (result.isSuccessful &&
                                result instanceof FileResult) {
                                // Load the detector into memory.
                                final File file = ((FileResult) result).file;
                                return Observable.merge(
                                    Observable.just(result),
                                    Observable
                                        .fromCallable(new Callable<RxResult>() {
                                            @Override
                                            public RxResult call() throws Exception {
                                                if (file == null || !file.exists()) {
                                                    throw new RuntimeException(
                                                        "The face68 model is invalid.");
                                                }

                                                if (!mLandmarksDetector.isFaceDetectorReady()) {
                                                    mLandmarksDetector.prepareFaceDetector();
                                                }
                                                if (!mLandmarksDetector.isFaceLandmarksDetectorReady()) {
                                                    mLandmarksDetector.prepareFaceLandmarksDetector(
                                                        file.getAbsolutePath());
                                                }

                                                return MsgProgressResult.succeed("Loading detector... done");
                                            }
                                        })
                                        .subscribeOn(worker)
                                        .flatMap(new Function<RxResult, ObservableSource<RxResult>>() {
                                            @Override
                                            public ObservableSource<RxResult> apply(RxResult result)
                                                throws Exception {
                                                return Observable.merge(
                                                    Observable.just(result),
                                                    Observable.just(LoadDetectorResult.succeed()));
                                            }
                                        })
                                        .startWith(MsgProgressResult.inProgress(
                                            "Loading detector...", 0))
                                        .onErrorReturn(new Function<Throwable, RxResult>() {
                                            @Override
                                            public RxResult apply(Throwable err)
                                                throws Exception {
                                                return MsgProgressResult.failed(err);
                                            }
                                        })
                                );
                            } else {
                                return Observable.just(result);
                            }
                        }
                    });
            }
        };
    }

    private ObservableTransformer<RxResult, RxResult> startGameIfOk() {
        return new ObservableTransformer<RxResult, RxResult>() {
            @Override
            public ObservableSource<RxResult> apply(Observable<RxResult> upstream) {
                return upstream
                    .flatMap(new Function<RxResult, ObservableSource<RxResult>>() {
                        @Override
                        public ObservableSource<RxResult> apply(RxResult result)
                            throws Exception {
                            if (result.isSuccessful &&
                                result instanceof LoadDetectorResult) {
                                // Start the game when the given result is successful.
                                final AtomicBoolean canDetect = new AtomicBoolean(false);
                                final AtomicBoolean isGameOver = new AtomicBoolean(false);

                                // FIXME: How to satisfy the lint?
                                return Observable.mergeArray(
                                    Observable.just(result),
                                    startLifeTickTimer(),
                                    startCameraDetection(canDetect),
                                    startIntroAnimation());
                            } else {
                                return Observable.just(result);
                            }
                        }
                    });
            }
        };
    }

    private Observable<RxResult> startCameraDetection(final AtomicBoolean canDetect) {
        // Set the preview config.
        if (isPortraitMode()) {
            mDebugOverlayView.setCameraPreviewSize(
                PREVIEW_HEIGHT, PREVIEW_WIDTH);
        } else {
            mDebugOverlayView.setCameraPreviewSize(
                PREVIEW_WIDTH, PREVIEW_HEIGHT);
        }

        // Create camera observable.
        return CameraObservable
            .create(
                GameActivity.this,
                mCameraView,
                PREVIEW_WIDTH, PREVIEW_HEIGHT,
                getFaceDetector())
            // Ready to detect if it is allowed to.
            .filter(new Predicate<FrameUiEvent<DLibFace>>() {
                @Override
                public boolean test(FrameUiEvent<DLibFace> ignored)
                    throws Exception {
                    return canDetect.get();
                }
            })
            // Detect the face and landmarks.
            .observeOn(Schedulers.computation())
            .flatMap(new Function<FrameUiEvent<DLibFace>, ObservableSource<RxResult>>() {
                @Override
                public ObservableSource<RxResult> apply(FrameUiEvent<DLibFace> event)
                    throws Exception {
                    // TODO: Do something with the data.
//                    event.data
                    return null;
                }
            });
    }

    private Observable<RxResult> startIntroAnimation() {
        return Observable
            // I intentionally postpone the intro animation a bit late so that
            // the camera preview is likely ready when the animation starts.
            .timer(250, TimeUnit.MILLISECONDS)
            .flatMap(new Function<Long, ObservableSource<RxResult>>() {
                @Override
                public ObservableSource<RxResult> apply(Long ignored)
                    throws Exception {
                    // Animation would automatically play when it subscribes to
                    // the upstream.
                    return new LottieAnimObservable(mCountDownView,
                                                    "animation/count_down_321.json")
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .ofType(AnimUiEvent.class)
                        .map(new Function<AnimUiEvent, RxResult>() {
                            @Override
                            public RxResult apply(AnimUiEvent event)
                                throws Exception {
                                if (event.justStart ||
                                    event.doing) {
                                    return AnimResult.inProgress();
                                } else {
                                    return AnimResult.succeed();
                                }
                            }
                        });
                }
            });
    }

    private Observable<RxResult> startLifeTickTimer() {
        return Observable
            .interval(1L, TimeUnit.SECONDS)
            .map(new Function<Long, RxResult>() {
                @Override
                public RxResult apply(Long value) throws Exception {
                    return TickResult.tick(value);
                }
            });
    }

    private Detector<DLibFace> getFaceDetector() {
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

    private Function<FrameUiEvent<DLibFace>, DetectBiteAction> toDetectBiteAction() {
        return new Function<FrameUiEvent<DLibFace>, DetectBiteAction>() {
            @Override
            public DetectBiteAction apply(FrameUiEvent<DLibFace> event)
                throws Exception {
                final SparseArray<DLibFace> faces = event.bundle;
                return new DetectBiteAction(faces.get(faces.keyAt(0)));
            }
        };
    }

    private Function<? super DetectBiteResult, BiteUiModel> toBiteUiModel() {
        return new Function<DetectBiteResult, BiteUiModel>() {
            @Override
            public BiteUiModel apply(DetectBiteResult result)
                throws Exception {
                return new BiteUiModel(result.mouthBound, result.biteCount);
            }
        };
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

//    private static final class UniverseEvent {
//
//        static final int UNDEFINED = -1;
//
//        static final int PERM_GRANTED = 1001;
//        static final int PERM_NOT_GRANTED = 1002;
//
//        static final int MODEL_NOT_INITIALIZED = 2001;
//        static final int MODEL_INITIALIZED = 2002;
//
//        static final int ON_DETECTING_BITE = 3001;
//
//        int status = UNDEFINED;
//
//        UniverseEvent(int status) {
//            this.status = status;
//        }
//    }
}
