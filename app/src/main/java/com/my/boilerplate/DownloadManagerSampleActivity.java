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

import android.*;
import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.icu.text.LocaleDisplayNames;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.my.boilerplate.view.SampleMenuAdapter;
import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

public class DownloadManagerSampleActivity extends AppCompatActivity {

    protected Toolbar mToolbar;
    protected ListView mMenu;

    protected DownloadManager mDownloadManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_download_manager_sample);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mMenu = (ListView) findViewById(R.id.menu);
        mMenu.setAdapter(onSampleMenuCreate());
        mMenu.setOnItemClickListener(onClickMenuItem());

        // Init the download manager.
        mDownloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
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

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    @SuppressWarnings({"unchecked"})
    protected SampleMenuAdapter onSampleMenuCreate() {
        return new SampleMenuAdapter(
            this,
            new Pair[]{
                new Pair<>("Download a large file ",
                           "The downloading is still alive when the app is in " +
                           "the background and there's also a progress bar in " +
                           "the activity. Everything should be synchronized.")
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
                        startDownloading();
                        break;
                }
            }
        };
    }

    protected void startDownloading() {
        // FIXME: Might crash on API<21.
        // Ask for writing storage permission.
        RxPermissions
            .getInstance(this)
            .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .subscribe(new DisposableObserver<Boolean>() {
                @Override
                public void onNext(Boolean granted) {
                    if (!granted) return;

                    Uri uri = Uri.parse("http://eoimages.gsfc.nasa.gov/images/imagerecords" +
                                        "/84000/84214/bluemarble_2014090_xlrg.jpg");

                    mDownloadManager.enqueue(
                        new DownloadManager.Request(uri)
                            .setTitle("Download the BLOB.")
                            .setDescription("The BLOB is an image of very large size.")
                            // Save the file into a public directory.
                            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                                                               "com.my.boilerplate")
                            // Set the network types.
                            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                                                    DownloadManager.Request.NETWORK_MOBILE));
                }

                @Override
                public void onError(Throwable e) {
                    Log.d("xyz", String.format("Grant perms error: %s", e));
                }

                @Override
                public void onComplete() {
                    // DO NOTHING.
                }
            });
    }
}
