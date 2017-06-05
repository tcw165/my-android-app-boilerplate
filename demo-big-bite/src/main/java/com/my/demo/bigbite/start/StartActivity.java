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

package com.my.demo.bigbite.start;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.google.gson.Gson;
import com.jakewharton.rxbinding2.view.RxView;
import com.my.core.protocol.IProgressBarView;
import com.my.demo.bigbite.R;
import com.my.demo.bigbite.game.GameActivity;
import com.my.demo.bigbite.protocol.Common;
import com.my.demo.bigbite.start.data.ChallengeAdapter;
import com.my.demo.bigbite.start.data.ChallengeItem;
import com.my.demo.bigbite.start.data.IChallengeItem;
import com.my.demo.bigbite.start.data.JsonChallenges;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.transform.Pivot;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class StartActivity
    extends AppCompatActivity
    implements IProgressBarView {

    // View
    @BindView(R.id.challenge_menu)
    DiscreteScrollView mChallengeMenu;
    @BindView(R.id.btn_start)
    View mBtnStart;
    @BindView(R.id.btn_settings)
    View mBtnSettings;
    ProgressDialog mProgress;

    // State.
    CompositeDisposable mDisposables;
    RequestManager mGlide;
    ChallengeAdapter mChallengeMenuAdapter;

    // Butter Knife.
    Unbinder mUnbinder;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_start);

        // Init view binding.
        mUnbinder = ButterKnife.bind(this);

        // Init Glide.
        mGlide = Glide.with(this);

        // Progress.
        mProgress = new ProgressDialog(this);

        // Challenge Menu.
        mChallengeMenuAdapter = new ChallengeAdapter(
            LayoutInflater.from(this), mGlide);
        mChallengeMenu.setAdapter(mChallengeMenuAdapter);
        mChallengeMenu.setItemTransformer(
            new AlphaScaleTransformer.Builder()
                .setMaxScale(1.05f)
                .setMinScale(0.7f)
                .setMinAlpha(0.3f)
                .setPivotX(Pivot.X.CENTER)
                .setPivotY(Pivot.Y.CENTER)
                .build());

        // Start Button.
        RxView.clicks(mBtnStart)
              .subscribe(new Consumer<Object>() {
                  @Override
                  public void accept(Object ignored) throws Exception {
                      onClickToStart();
                  }
              });
    }

    @Override
    protected void onResume() {
        super.onResume();

        mGlide.onStart();

        mDisposables = new CompositeDisposable();
        // First time to load the challenge items.
        mDisposables.add(
            getChallengeItems()
                .startWith(Collections.<ChallengeItem>emptyList())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<ChallengeItem>>() {
                    @Override
                    public void accept(List<ChallengeItem> items)
                        throws Exception {
                        if (items.isEmpty()) {
                            // Disable button if the data is not ready.
                            mBtnStart.setEnabled(false);
                        } else {
                            showItems(items);
                            mBtnStart.setEnabled(true);
                        }
                    }
                }));
        mDisposables.add(
            RxView.clicks(mBtnSettings)
                  .subscribe(new Consumer<Object>() {
                      @Override
                      public void accept(Object ignored)
                          throws Exception {

                      }
                  }));
        mDisposables.add(
            mChallengeMenuAdapter
                .getOnClickObservable()
                .subscribe(new Consumer<ChallengeItem>() {
                    @Override
                    public void accept(ChallengeItem item)
                        throws Exception {
                        clickItem(item);
                    }
                }));
    }

    @Override
    protected void onPause() {
        super.onPause();

        mGlide.onStop();

        mDisposables.clear();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mGlide.onDestroy();

        mUnbinder.unbind();
    }

    void onClickToStart() {
        final ChallengeItem item = mChallengeMenuAdapter
            .getItem(mChallengeMenu.getCurrentItem());

        startActivity(
            new Intent(this, GameActivity.class)
                .putExtra(Common.PARAMS_DATA, item));
    }

    // TODO: Refactor it with the presenter.
    private void showItems(List<ChallengeItem> items) {
        if (mChallengeMenuAdapter.setItems(items)) {
            mChallengeMenu.scrollToPosition(items.size() > 1 ? 1 : 0);
        }
    }

    // TODO: Refactor it with the presenter.
    private void clickItem(ChallengeItem item) {
        // DO NOTHING.
    }

    // TODO: Refactor it with the presenter.
    Observable<List<ChallengeItem>> getChallengeItems() {
        // TODO: Load JSON.
        return Observable
            .fromCallable(new Callable<List<ChallengeItem>>() {
                @Override
                public List<ChallengeItem> call() throws Exception {
                    if (mChallengeMenuAdapter.getItemCount() > 0) {
                        return mChallengeMenuAdapter.getItems();
                    } else {
                        final InputStream stream = getAssets().open("challenge_items.json");
                        final InputStreamReader reader = new InputStreamReader(stream);
                        final JsonChallenges jsonObject = new Gson().fromJson(
                            reader, JsonChallenges.class);

                        final List<ChallengeItem> items = new ArrayList<>();
                        for (IChallengeItem jsonItem : jsonObject.getItems()) {
                            items.add(new ChallengeItem(jsonItem, "file:///android_asset/"));
                        }

                        return items;
                    }
                }
            })
            .subscribeOn(Schedulers.io());
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////
}
