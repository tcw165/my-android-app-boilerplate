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
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.my.core.protocol.IProgressBarView;
import com.my.core.util.FileUtil;
import com.my.core.util.ViewUtil;
import com.my.demo.dlib.view.FaceLandmarksImageView;
import com.my.jni.dlib.FaceLandmarksDetector68;
import com.my.jni.dlib.data.Face;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class SampleOfBasicUsageActivity extends AppCompatActivity
    implements IProgressBarView {

//    private static final String ASSET_TEST_PHOTO = "boyw165-i-am-tyson-chandler.jpg";
    private static final String ASSET_TEST_PHOTO = "5-ppl.jpg";
    private static final String ASSET_SHAPE_DETECTOR_DATA = "shape_predictor_68_face_landmarks.dat";

    // View.
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.img_preview)
    FaceLandmarksImageView mImgPreview;

    // Butter Knife.
    Unbinder mUnbinder;

    // Face Detector.
    FaceLandmarksDetector68 mFaceDetector;

    // Data.
    CompositeDisposable mComposition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sample_of_basic_usage);

        // Init view binding.
        mUnbinder = ButterKnife.bind(this);

        // Toolbar
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }

        // Load image to preview.
        Glide.with(this)
             .load(String.format("file:///android_asset/%s", ASSET_TEST_PHOTO))
             .into(mImgPreview);

        // Init the face detector.
        mFaceDetector = new FaceLandmarksDetector68();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mComposition = new CompositeDisposable();
        mComposition.add(
            grantPermission()
                .observeOn(AndroidSchedulers.mainThread())
                // Show the progress-bar.
                .map(new Function<Boolean, Boolean>() {
                    @Override
                    public Boolean apply(Boolean granted) throws Exception {
                        if (granted) {
                            showProgressBar("Preparing the data...");
                        }
                        return granted;
                    }
                })
                // Face landmarks detection.
                .flatMap(new Function<Boolean, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(Boolean granted)
                        throws Exception {
                        if (granted) {
                            // Start face landmarks detection.
                            return processFaceLandmarksDetection()
                                .subscribeOn(Schedulers.io());
                        } else {
                            return Observable.just(0);
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Object>() {
                    @Override
                    public void onNext(Object value) {
                        hideProgressBar();
                    }

                    @Override
                    public void onError(Throwable e) {
                        hideProgressBar();
                    }

                    @Override
                    public void onComplete() {
                        hideProgressBar();
                    }
                }));
    }

    @Override
    protected void onPause() {
        super.onPause();

        mComposition.clear();

        hideProgressBar();
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

    private Observable<Boolean> processFaceLandmarksDetection() {
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
            // FIXME: A workaround to make sure the drawable is ready before
            // FIXME: the detection starts.
            .delay(1000, TimeUnit.MILLISECONDS)
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
            })
            // Update progressbar message.
            .observeOn(AndroidSchedulers.mainThread())
            .map(new Function<DetectorParams, DetectorParams>() {
                @Override
                public DetectorParams apply(DetectorParams params) throws Exception {
                    showProgressBar("Detecting face and landmarks...");
                    return params;
                }
            })
            // Detect face and landmarks.
            .observeOn(Schedulers.io())
            .map(new Function<DetectorParams, List<Face>>() {
                @Override
                public List<Face> apply(DetectorParams params) throws Exception {
                    final BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    options.inJustDecodeBounds = true;

                    // TODO: Make it a BitmapUtil function.
                    // Pyramid down the image.
                    BitmapFactory.decodeFile(params.testPhotoPath, options);
                    final float scale = Math.max((float) options.outWidth / 800f,
                                                 (float) options.outHeight / 800f);
                    if (scale > 1f) {
                        // Do logarithm with base 2 (not e).
                        options.inSampleSize = 1 << (int) (Math.log(Math.ceil(scale)) / Math.log(2));
                    }
                    options.inJustDecodeBounds = false;
                    final Bitmap resizedBitmap = BitmapFactory.decodeFile(
                        params.testPhotoPath,
                        options);

                    return mFaceDetector.findFacesAndLandmarks(resizedBitmap);
                }
            })
            // Update message of the progress bar.
            .observeOn(AndroidSchedulers.mainThread())
            .map(new Function<List<Face>, Boolean>() {
                @Override
                public Boolean apply(List<Face> faces) throws Exception {
                    showProgressBar("Rendering...");

                    // Render the landmarks.
                    mImgPreview.setFaces(faces);

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
