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

package com.my.comp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.reactivex.observers.DisposableObserver;

/**
 * An invisible Activity responsible for taking care of the necessary permissions
 * and dispatching the intent. It uses {@link FileProvider} to grant the access
 * of URI. You don't need to define the {@link FileProvider} in your project's
 * {@code AndroidManifest.xml} because it's defined in the library.
 * <br/>
 * <br/>
 * Usage:
 * <br/>
 * 1. Define {@code <string name="file_provider_authority"></string>} in the
 * {@code res/values/strings.xml} file.
 * <br/>
 * 2. Define your {@code res/xml/paths.xml}.
 * <br/>
 * 3. Use {@link AppCompatActivity#startActivityForResult(Intent, int)} like the
 * example:
 * <pre>
 *
 * startActivityForResult(
 *     new Intent(FileProviderActivity.this,
 *     TakePhotoDelegateActivity.class),
 *     REQ_TAKE_PHOTO);
 *
 * </pre>
 */
public class TakePhotoDelegateActivity extends AppCompatActivity {

    static final int REQ_TAKE_PHOTO = 0;

    static final String PARAMS_PHOTO_PATH = "params_photo_path";
    static final String PARAMS_IF_DISPATCH_INTENT = "params_if_dispatch_intent";

    /**
     * Used to determine action doing in the
     * {@link TakePhotoDelegateActivity#onResume()} function. It is for avoiding
     * duplicated intent dispatching when the "don't keep Activity" is enabled.
     */
    boolean mIfDispatchIntent = true;
    /**
     * A valid URI for a 3rd party camera app to save the photo.
     */
    Uri mPhotoPath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo_delegatee);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mIfDispatchIntent) {
            mIfDispatchIntent = false;
            grantPermAndDispatchTakePhotoIntent();
        }
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {
        switch (requestCode) {
            case REQ_TAKE_PHOTO:
                onTakePhoto(resultCode, data);
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }

        // Terminate the delegate Activity.
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(PARAMS_PHOTO_PATH, mPhotoPath);
        outState.putBoolean(PARAMS_IF_DISPATCH_INTENT, mIfDispatchIntent);
    }

    @Override
    protected void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);

        mPhotoPath = inState.getParcelable(PARAMS_PHOTO_PATH);
        mIfDispatchIntent = inState.getBoolean(PARAMS_IF_DISPATCH_INTENT, false);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    /**
     * The callback function is called after taking a photo.
     */
    void onTakePhoto(int resultCode,
                     Intent data) {
        if (resultCode == RESULT_CANCELED || mPhotoPath == null) return;

        final File file = new File(mPhotoPath.getPath());
        if (file.exists()) {
            setResult(RESULT_OK, new Intent().setData(mPhotoPath));
        } else {
            mPhotoPath = null;
            setResult(RESULT_CANCELED);
        }

        // Add the photo to the system MediaContent.
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                          .setData(mPhotoPath));
    }

    void grantPermAndDispatchTakePhotoIntent() {
        if (Build.VERSION.SDK_INT >= 23) {
            RxPermissions
                .getInstance(this)
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                         Manifest.permission.CAMERA)
                .subscribe(new DisposableObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean granted) {
                        if (granted) {
                            dispatchTakePhotoIntent();
                        } else {
                            Toast.makeText(
                                TakePhotoDelegateActivity.this,
                                "WRITE_EXTERNAL_STORAGE | CAMERA is not granted.",
                                Toast.LENGTH_SHORT)
                                 .show();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        // DO NOTHING.
                    }

                    @Override
                    public void onComplete() {
                        // DO NOTHING.
                    }
                });
        } else {
            int permCheck1 = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int permCheck2 = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA);

            if (permCheck1 == PackageManager.PERMISSION_GRANTED &&
                permCheck2 == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePhotoIntent();
            } else {
                Toast.makeText(
                    TakePhotoDelegateActivity.this,
                    "WRITE_EXTERNAL_STORAGE | CAMERA is not granted.",
                    Toast.LENGTH_SHORT)
                     .show();
            }
        }
    }

    void dispatchTakePhotoIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(getPackageManager()) != null) {
            File file = null;
            try {
                file = createImageFile();
            } catch (IOException ex) {
                // DO NOTHING.
                Log.d("xyz", ex.getMessage());
            }

            // Continue only if the File was successfully created.
            if (file != null) {
                // Save the path.
                mPhotoPath = Uri.fromFile(file);

                Log.d("xyz", String.format("Saved file=%s", file.getAbsolutePath()));

                // Specify the authorities under which this content provider can
                // be found. Multiple authorities may be supplied by separating
                // them with a semicolon. Authority names should use a Java-style
                // naming convention (such as com.google.provider.MyProvider) in
                // order to avoid conflicts. Typically this name is the same as
                // the class implementation describing the provider's data
                // structure.
                final String authority = getResources().getString(R.string.file_provider_authority);
                final Uri uri = FileProvider.getUriForFile(this,
                                                           authority,
                                                           file);
                Log.d("xyz", String.format("Generated uri=%s", uri));

                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

                startActivityForResult(intent, REQ_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                                                Locale.ENGLISH).format(new Date());
        final String appName = getResources().getString(R.string.app_name);
        final File dir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES + "/" + appName);
        // Create the missing parents.
        if (dir.mkdirs() || dir.isDirectory()) {
            return File.createTempFile(timeStamp,
                                       ".jpg",
                                       dir);
        } else {
            throw new IOException(String.format("%s is not present.",
                                                dir.getAbsolutePath()));
        }
    }
}
