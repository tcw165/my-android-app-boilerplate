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

package com.my.demo.bigbite;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.my.core.protocol.IProgressBarView;
import com.my.core.util.ViewUtil;
import com.my.widget.adapter.SampleMenuAdapter;
import com.my.widget.adapter.SampleMenuAdapter.SampleMenuItem;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class StartActivity
    extends AppCompatActivity
    implements IProgressBarView {

    // View
//    @BindView(R.id.toolbar)
//    Toolbar mToolbar;
//    @BindView(R.id.menu)
//    ListView mStartMenu;
    ProgressDialog mProgress;

    // Butter Knife.
    Unbinder mUnbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_start);

        // Init view binding.
        mUnbinder = ButterKnife.bind(this);

//        // Toolbar.
//        setSupportActionBar(mToolbar);
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//            getSupportActionBar().setDisplayShowHomeEnabled(false);
//        }
//
//        // List menu.
//        mStartMenu.setAdapter(onCreateSampleMenu());
//        mStartMenu.setOnItemClickListener(onClickSampleMenuItem());

        // Progress.
        mProgress = new ProgressDialog(this);
    }

    @Override
    public void showProgressBar() {
        mProgress.setMessage(getString(R.string.loading));
        mProgress.setCancelable(false);
        mProgress.show();
    }

    @Override
    public void showProgressBar(String msg) {
        mProgress.setMessage(msg);
        mProgress.setCancelable(false);
        mProgress.show();
    }

    @Override
    public void hideProgressBar() {
        mProgress.dismiss();
    }

    @Override
    public void updateProgress(int progress) {
        mProgress.setProgress(progress);
        mProgress.setCancelable(false);
        mProgress.show();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mUnbinder.unbind();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////
}
