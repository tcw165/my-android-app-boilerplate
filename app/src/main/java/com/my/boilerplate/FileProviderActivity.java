// Copyright (c) 2016-present boyw165
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

package com.my.boilerplate;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.my.boilerplate.view.ImagePreviewerDialogFragment;
import com.my.boilerplate.view.SampleMenuAdapter;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;

public class FileProviderActivity
    extends AppCompatActivity {

    protected static final int REQ_TAKE_PHOTO = 0;

    protected Uri mPhotoPath;
    protected Observable<Void> mShowPhotoTask;

    protected Toolbar mToolbar;
    protected ListView mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_file_provider_sample);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mMenu = (ListView) findViewById(R.id.menu);
        mMenu.setAdapter(onSampleMenuCreate());
        mMenu.setOnItemClickListener(onClickMenuItem());
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mShowPhotoTask != null) {
            mShowPhotoTask.publish()
                          .connect();
            mShowPhotoTask = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    @SuppressWarnings({"unchecked"})
    protected SampleMenuAdapter onSampleMenuCreate() {
        return new SampleMenuAdapter(
            this,
            new Pair[]{
                new Pair<>("Take a photo through other camera app",
                           "After Android API 24, passing file:// URIs outside " +
                           "the package domain may leave the receiver with an " +
                           "unaccessible path. So in this example, it uses the " +
                           "FileProvider to grant the permission for other Apps " +
                           "to access the file.")
            });
    }

    protected OnItemClickListener onClickMenuItem() {
        return new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent,
                                    View view,
                                    int position,
                                    long id) {
                switch (position) {
                    case 0:
                        grantPermAndDispatchTakePhotoIntent();
                        break;
                }
            }
        };
    }

    /**
     * The callback function is called after taking a photo.
     */
    protected void onTakePhoto(int resultCode,
                               Intent data) {
        if (resultCode == RESULT_CANCELED) return;

        final File file = new File(mPhotoPath.getPath());
        final Uri uri = mPhotoPath;
        if (file.exists()) {
            mShowPhotoTask = Observable
                .create(new ObservableOnSubscribe<Void>() {
                    @Override
                    public void subscribe(ObservableEmitter<Void> e) throws Exception {
                        showPhoto(uri);
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread());
        } else {
            mPhotoPath = null;
            Snackbar.make(findViewById(android.R.id.content),
                          "The picture doesn't exist.",
                          Snackbar.LENGTH_SHORT)
                    .show();
        }
        // TODO: Add the photo to the system MediaContent.
    }

    protected void showPhoto(final Uri uri) {
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            getSupportFragmentManager()
                .beginTransaction()
                .remove(prev)
                .commit();
        }

        ImagePreviewerDialogFragment
            .newInstance(uri)
            .show(getSupportFragmentManager(), "dialog");
    }

    protected void grantPermAndDispatchTakePhotoIntent() {
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
                            Snackbar.make(findViewById(android.R.id.content),
                                          "WRITE_EXTERNAL_STORAGE | CAMERA is not granted.",
                                          Snackbar.LENGTH_SHORT)
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
            dispatchTakePhotoIntent();
        }
    }

    protected void dispatchTakePhotoIntent() {
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
                Uri uri = FileProvider.getUriForFile(this,
                                                     "com.my.boilerplate",
                                                     file);
                Log.d("xyz", String.format("Generated uri=%s", uri));

                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

                startActivityForResult(intent, REQ_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        File dir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES + "/my_boilerplate_app");
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
