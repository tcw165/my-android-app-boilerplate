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

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.my.widget.IProgressBar;
import com.my.widget.util.ViewUtil;

public class PhotoPickerActivity
    extends AppCompatActivity
    implements IProgressBar {

    Toolbar mToolbar;
    AppCompatSpinner mAlbumList;
    RecyclerView mPhotoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_photo_picker);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        mAlbumList = (AppCompatSpinner) findViewById(R.id.album_list);

        mPhotoList = (RecyclerView) findViewById(R.id.photo_list);

//        MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//        MediaStore.Images.Media.INTERNAL_CONTENT_URI;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void showProgressBar() {
        ViewUtil
            .with(this)
            .showProgressBar(getString(R.string.loading));
    }

    @Override
    public void hideProgressBar() {
        ViewUtil
            .with(this)
            .hideProgressBar();
    }

    @Override
    public void updateProgress(int progress) {
        // DO NOTHING.
    }
}
