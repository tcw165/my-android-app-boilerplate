// Copyright (c) 2016 boyw165
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
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.my.boilerplate.json.JsonWhatever;
import com.my.boilerplate.util.PermUtil;
import com.my.boilerplate.util.ViewUtil;
import com.my.boilerplate.util.WebApiUtil;
import com.my.boilerplate.view.CollageLayout;
import com.my.boilerplate.view.DropDownMenuView;
import com.my.boilerplate.view.IProgressBarView;
import com.my.boilerplate.view.ScrapView;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

public class StartPageActivity
    extends AppCompatActivity
    implements IProgressBarView {

    private final static String TAG = StartPageActivity.class.getSimpleName();

    private CollageLayout mCollageEditor;
    private DropDownMenuView mDrawerMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_page);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Up-down menu.
        mDrawerMenu = (DropDownMenuView) findViewById(R.id.drawer_menu);

        // The collage editor.
        mCollageEditor = (CollageLayout) findViewById(R.id.collage_editor);

//        // Example: Chain multiple observables.
//        doHttpRequests();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                toggleDrawerMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void showProgressBar() {
        ViewUtil
            .with(this)
            .setProgressBarCancelable(false)
            .showProgressBar(getString(R.string.loading));
    }

    @Override
    public void hideProgressBar() {
        ViewUtil
            .with(this)
            .hideProgressBar();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    protected OnClickListener onClickNavigationIcon() {
        return new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDrawerMenu == null) return;

                if (mDrawerMenu.isShowing()) {
                    mDrawerMenu.hideWithAnimation();
                } else {
                    mDrawerMenu.showWithAnimation();
                }
            }
        };
    }

    private void toggleDrawerMenu() {
        if (mDrawerMenu == null) return;

        if (mDrawerMenu.isShowing()) {
            mDrawerMenu.hideWithAnimation();
        } else {
            mDrawerMenu.showWithAnimation();
        }
    }

    protected OnClickListener onClickAddScrap(float ratio) {
        final float scrapRatio = ratio;

        return new OnClickListener() {
            @Override
            public void onClick(View view) {
//                int scrapWidth = (int) ((float) mCollageEditor.getWidth() / 3.f);
                ScrapView scrap = new ScrapView(StartPageActivity.this);

                scrap.setTranslationX(100);
                scrap.setTranslationY(100);
//                scrap.setScaleX(0.33f);
//                scrap.setScaleY(0.33f);
                scrap.setAspectRatio(scrapRatio);
                scrap.setBackgroundColor(Color.rgb((int) ((double) 0xFF * Math.random()),
                                                   (int) ((double) 0xFF * Math.random()),
                                                   (int) ((double) 0xFF * Math.random())));

                mCollageEditor.addView(scrap);

                Toast.makeText(StartPageActivity.this,
                               String.format("Make a scrap (aspect ratio is %f)", scrapRatio),
                               Toast.LENGTH_SHORT);
            }
        };
    }

    protected void doHttpRequests() {
        Observable
            .just(null)
            // Ask for permission.
            .concatWith(PermUtil
                            .with(this)
                            .request(Manifest.permission.CAMERA,
                                     Manifest.permission.WRITE_EXTERNAL_STORAGE))
            // Request data.
            .concatWith(WebApiUtil
                            .with(this)
                            .showProgressBar(this)
                            .getJsonWhatever())
            // Only handle the non-NULL stream.
            .filter(new Func1<Object, Boolean>() {
                @Override
                public Boolean call(Object o) {
                    return o != null;
                }
            })
            .subscribe(new Action1<Object>() {
                           @Override
                           public void call(Object o) {
                               if (o == null) {
                                   Log.i(TAG, "null");
                               } else if (o instanceof Boolean) {
                                   if ((Boolean) o) {
                                       Log.i(TAG, "Permissions are granted.");
                                   } else {
                                       Log.i(TAG, "Permissions are not granted.");
                                   }
                               } else if (o instanceof JsonWhatever) {
                                   JsonWhatever data = (JsonWhatever) o;
                                   Log.i(TAG, String.format("Get JsonWhatever data = %s", data.toString()));
                               }
                           }
                       },
                       new Action1<Throwable>() {
                           @Override
                           public void call(Throwable t) {
                               Log.e(TAG, "onError", t);
                           }
                       });
    }
}
