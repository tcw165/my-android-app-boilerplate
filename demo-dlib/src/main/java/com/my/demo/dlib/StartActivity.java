package com.my.demo.dlib;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.my.core.protocol.IProgressBarView;
import com.my.core.util.FileUtil;
import com.my.core.util.ViewUtil;
import com.my.jni.dlib.FaceLandmarksDetector;
import com.my.jni.dlib.data.Face;
import com.my.jni.dlib.data.Messages;
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

public class StartActivity extends AppCompatActivity
    implements IProgressBarView {

    private static final String ASSET_TEST_PHOTO = "boyw165-i-am-tyson-chandler.jpg";
    private static final String ASSET_SHAPE_DETECTOR_DATA = "shape_predictor_68_face_landmarks.dat";

    // View.
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.img_input)
    ImageView mImgInput;
    @BindView(R.id.img_output)
    ImageView mImgOutput;

    // Butter Knife.
    Unbinder mUnbinder;

    // Face Detector.
    FaceLandmarksDetector mFaceDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_start);

        // Init view binding.
        mUnbinder = ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }

        // Init the input image.
        Glide.with(this)
             .load(Uri.parse(String.format("file:///android_asset/%s", ASSET_TEST_PHOTO)))
             .asBitmap()
             .placeholder(R.color.black_70)
             .into(mImgInput);

        // Init the face detector.
        mFaceDetector = new FaceLandmarksDetector(getAssets());
        grantPermission()
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap(new Function<Boolean, ObservableSource<?>>() {
                @Override
                public ObservableSource<?> apply(Boolean granted)
                    throws Exception {
                    if (granted) {
                        showProgressBar("Preparing the data...");
                        return processFaceLandmarksDetection();
                    } else {
                        return Observable.just(0);
                    }
                }
            })
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new DisposableObserver<Object>() {
                @Override
                public void onNext(Object value) {

                }

                @Override
                public void onError(Throwable e) {
                    hideProgressBar();
                }

                @Override
                public void onComplete() {
                    hideProgressBar();
                }
            });
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

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mUnbinder.unbind();
    }

    private Observable<Boolean> grantPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return RxPermissions
                .getInstance(this)
                .request(Manifest.permission.READ_EXTERNAL_STORAGE,
                         Manifest.permission.WRITE_EXTERNAL_STORAGE);
        } else {
            int permCheck1 = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
            int permCheck2 = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

            return Observable.just(permCheck1 == PackageManager.PERMISSION_GRANTED &&
                                   permCheck2 == PackageManager.PERMISSION_GRANTED);
        }
    }

    private Observable<Object> processFaceLandmarksDetection() {
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
            // Deserialize the detector.
            .observeOn(Schedulers.io())
            .map(new Function<DetectorParams, List<Face>>() {
                @Override
                public List<Face> apply(DetectorParams config)
                    throws Exception {
                    // Prepare the detectors.
                    mFaceDetector.deserializeFaceDetector();
                    mFaceDetector.deserializeShapeDetector(
                        config.shapeDetectorPath);

                    // Do the face landmarks detection.
                    return mFaceDetector.getFaces(config.testPhotoPath);
                }
            })
            // Update texting of the progress bar.
            .observeOn(AndroidSchedulers.mainThread())
            .map(new Function<List<Face>, Object>() {
                @Override
                public Object apply(List<Face> faces) throws Exception {
                    showProgressBar("Detecting face landmarks...");

                    // TODO: Render the landmarks.

                    return null;
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
