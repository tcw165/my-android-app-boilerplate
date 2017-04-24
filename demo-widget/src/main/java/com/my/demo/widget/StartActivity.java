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

package com.my.demo.widget;

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

public class StartActivity
    extends AppCompatActivity
    implements IProgressBarView {

    Toolbar mToolbar;
    ListView mStartMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_start);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }

        // List menu.
        mStartMenu = (ListView) findViewById(R.id.menu);
        mStartMenu.setAdapter(onCreateSampleMenu());
        mStartMenu.setOnItemClickListener(onClickSampleMenuItem());
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
        showProgressBar();
    }

    @Override
    public void hideProgressBar() {
        ViewUtil
            .with(this)
            .hideProgressBar();
    }

    @Override
    public void updateProgress(int progress) {
        showProgressBar();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    @SuppressWarnings({"unchecked"})
    protected SampleMenuAdapter onCreateSampleMenu() {
        return new SampleMenuAdapter(
            this,
            new SampleMenuItem[]{
                new SampleMenuItem(
                    "CameraTextureView",
                    "Use TextureView or SurfaceView to provide the " +
                    "camera feature.",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(
                                new Intent(StartActivity.this,
                                           SampleOfCameraActivity.class));
                        }
                    }),
                new SampleMenuItem(
                    "DropDownMenuLayout",
                    "The ViewGroup is responsible for intercepting the " +
                    "touch event.",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(
                                new Intent(StartActivity.this,
                                           SampleOfDropDownMenuLayoutActivity.class));
                        }
                    }),
                new SampleMenuItem(
                    "ElasticDragLayout",
                    "Inheriting from CoordinatorLayout and support elastic " +
                    "drag UX like iOS's scroll-view.",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(
                                new Intent(StartActivity.this,
                                           SampleOfElasticDragLayoutActivity.class));
                        }
                    }),
                new SampleMenuItem(
                    "ElasticDragDismissLayout (ElasticDragLayout)",
                    "Idea inspired from the sample code of Plaid app. It " +
                    "inherits from the CoordinatorLayout and is using a " +
                    "translucent Activity to implement the drag-to-dismiss " +
                    "gesture.",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                            startActivity(
//                                new Intent(StartActivity.this,
//                                           SampleOfElasticDragDismissLayoutActivity.class),
//                                // Necessary option to enable the scene transition.
//                                ActivityOptionsCompat
//                                    .makeSceneTransitionAnimation(
//                                        getActivity(),
//                                        (android.support.v4.util.Pair<View, String>[]) null)
//                                    .toBundle());
                            startActivity(
                                new Intent(StartActivity.this,
                                           SampleOfElasticDragDismissLayoutActivity.class));
                        }
                    }),
                new SampleMenuItem(
                    "ElasticDragMenuLayout (ElasticDragLayout)",
                    "A child class inheriting from ElasticDragDismissLayout. " +
                    "The layout allows a NestedScrollingChild child view " +
                    "being over dragged.",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(
                                new Intent(StartActivity.this,
                                           SampleOfElasticDragMenuLayoutActivity.class));
                        }
                    }),
                new SampleMenuItem(
                    "PhotoPickerView",
                    "A google-photo like picker.",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivityForResult(
                                new Intent(StartActivity.this,
                                           SampleOfPhotoPickerActivity.class), 0);
                        }
                    })
            });
    }

    protected AdapterView.OnItemClickListener onClickSampleMenuItem() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent,
                                    View view,
                                    int position,
                                    long id) {
                final SampleMenuItem item = (SampleMenuItem) parent.getAdapter()
                                                                   .getItem(position);
                item.onClickListener.onClick(view);
            }
        };
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////
}
