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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.my.boilerplate.protocol.IOnClickObjectListener;
import com.my.boilerplate.util.PrefUtil;
import com.my.widget.CheckableImageView;
import com.my.widget.IProgressBarView;
import com.my.widget.util.ViewUtil;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.Orientation;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class LoginActivity
    extends AppCompatActivity
    implements IProgressBarView,
               IOnClickObjectListener {

    DiscreteScrollView mAvatarList;
    MyAvatarAdapter mAvatarListAdapter;
    TextView mBtnDone;

    String mSelectedAvatar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        // List menu.
        mAvatarListAdapter = new MyAvatarAdapter(this, this);
        mAvatarList = (DiscreteScrollView) findViewById(R.id.avatar_list);
        mAvatarList.setAdapter(mAvatarListAdapter);
        mAvatarList.setOrientation(Orientation.HORIZONTAL);
        mAvatarList.setItemTransformer(new ScaleTransformer.Builder()
                                           .setMinScale(0.75f)
                                           .build());
        mAvatarList.setItemTransitionTimeMillis(100);
        mAvatarList.setOnItemChangedListener(
            new DiscreteScrollView.OnItemChangedListener<RecyclerView.ViewHolder>() {
            @Override
            public void onCurrentItemChanged(@NonNull RecyclerView.ViewHolder viewHolder,
                                             int adapterPosition) {
                final String avatar = mAvatarListAdapter.mData.get(adapterPosition);

                mSelectedAvatar = avatar;

                PrefUtil.setString(getApplicationContext(),
                                   PrefUtil.PREF_AVATAR_IMAGE_PATH,
                                   mSelectedAvatar);
            }
        });

        // Button.
        mBtnDone = (TextView) findViewById(R.id.btn_done);
        mBtnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(mSelectedAvatar)) return;

                startActivity(new Intent(LoginActivity.this,
                                         StartActivity.class));
                finish();
            }
        });

        // Load avatars.
        Observable
            .fromCallable(new Callable<List<String>>() {
                @Override
                public List<String> call() throws Exception {
                    final String folderPath = "avatars/";
                    final InputStream is = getAssets().open(folderPath + "info.json");
                    final InputStreamReader reader = new InputStreamReader(is);
                    final List<String> raw = new Gson()
                        .fromJson(reader, MyAvatarJson.class)
                        .avatars;
                    final List<String> data = new ArrayList<>();

                    for (String rawPath : raw) {
                        data.add("file:///android_asset/" + folderPath + rawPath);
                    }

                    return data;
                }
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new DisposableObserver<List<String>>() {
                @Override
                public void onNext(List<String> value) {
                    mAvatarListAdapter.setData(value);
                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onComplete() {

                }
            });
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

    @Override
    public void updateProgress(int progress) {
        showProgressBar();
    }

    @Override
    public void onClickObject(View view,
                              Object data) {
//        final String imagePath = (String) data;
//
        // DO NOTHING.
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    private static class MyAvatarJson {
        @SerializedName("avatars")
        List<String> avatars;
    }

    private static class MyAvatarAdapter
        extends RecyclerView.Adapter<MyAvatarViewHolder> {

        final private Context mContext;
        final private LayoutInflater mInflater;
        final private IOnClickObjectListener mListener;

        private List<String> mData = Collections.emptyList();

        MyAvatarAdapter(final Context context,
                        final IOnClickObjectListener listener) {
            mContext = context;
            mInflater = LayoutInflater.from(context);
            mListener = listener;
        }

        @Override
        public MyAvatarViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
            return new MyAvatarViewHolder(
                mInflater.inflate(R.layout.view_avatar_item,
                                  parent, false));
        }

        @Override
        public void onBindViewHolder(MyAvatarViewHolder holder,
                                     int position) {
            this.onBindViewHolder(holder, position, null);
        }

        @Override
        public void onBindViewHolder(final MyAvatarViewHolder holder,
                                     final int position,
                                     final List<Object> payloads) {
            final CheckableImageView imgView = (CheckableImageView) holder.itemView;
            final String photo = mData.get(position);

            imgView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onClickObject(imgView, photo);
                }
            });

            Glide.with(mContext)
                 .load(photo)
                 .placeholder(android.R.color.transparent)
                 .priority(Priority.IMMEDIATE)
                 .into(imgView);
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        public void setData(List<String> data) {
            if (data == null) {
                throw new IllegalArgumentException("Given list is null");
            }

            mData = data;
            notifyDataSetChanged();
        }
    }

    private static class MyAvatarViewHolder extends RecyclerView.ViewHolder {

        MyAvatarViewHolder(View view) {
            super(view);
        }
    }
}
