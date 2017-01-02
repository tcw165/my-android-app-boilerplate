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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.my.boilerplate.view.ImagePreviewerDialogFragment;
import com.my.boilerplate.view.SampleMenuAdapter;
import com.my.comp.TakePhotoDelegateActivity;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class FileProviderActivity
    extends AppCompatActivity {

    static final int REQ_TAKE_PHOTO = 0;

    Observable<Void> mShowPhotoTask;

    Toolbar mToolbar;
    ListView mMenu;

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
    private SampleMenuAdapter onSampleMenuCreate() {
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

    private OnItemClickListener onClickMenuItem() {
        return new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent,
                                    View view,
                                    int position,
                                    long id) {
                switch (position) {
                    case 0:
                        startActivityForResult(
                            new Intent(FileProviderActivity.this,
                                       TakePhotoDelegateActivity.class),
                            REQ_TAKE_PHOTO);
                        break;
                }
            }
        };
    }

    /**
     * The callback function is called after taking a photo.
     */
    void onTakePhoto(int resultCode,
                     Intent data) {
        if (resultCode == RESULT_CANCELED) return;

        final Uri uri = data.getData();
        final File file = new File(uri.getPath());
        if (file.exists()) {
            // Because the onActivityResult() is called before onResume(),
            // showing DialogFragment at the moment is prohibited. So we
            // create a Runnable task to be processed in the onResume().
            mShowPhotoTask = Observable
                .create(new ObservableOnSubscribe<Void>() {
                    @Override
                    public void subscribe(ObservableEmitter<Void> e) throws Exception {
                        showPhoto(uri);
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread());
        } else {
            Snackbar.make(findViewById(android.R.id.content),
                          "The picture doesn't exist.",
                          Snackbar.LENGTH_SHORT)
                    .show();
        }
    }

    void showPhoto(final Uri uri) {
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
}
