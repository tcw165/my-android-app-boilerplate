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

package com.my.demo.bigbite.start.data;

import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.RequestManager;
import com.jakewharton.rxbinding2.view.RxView;
import com.my.demo.bigbite.R;
import com.my.reactive.IOnClickObservable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class ChallengeAdapter
    extends RecyclerView.Adapter<RecyclerView.ViewHolder>
    implements IOnClickObservable<ChallengeItem> {

    LayoutInflater mInflater;
    RequestManager mGlide;

    // State.
    List<ChallengeItem> mItems = new CopyOnWriteArrayList<>();
    Subject<ChallengeItem> mOnClickSub = PublishSubject.create();

    public ChallengeAdapter(final LayoutInflater inflater,
                            final RequestManager glide) {
        mInflater = inflater;
        mGlide = glide;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        final View itemView = mInflater.inflate(
            R.layout.view_of_challenge_menu_item, parent, false);
        final RecyclerView.ViewHolder holder = new MyViewHolder(itemView);

        // Set onClick listener.
        RxView.clicks(itemView)
              .debounce(150, TimeUnit.MILLISECONDS)
              .map(new Function<Object, ChallengeItem>() {
                  @Override
                  public ChallengeItem apply(Object ignored) throws Exception {
                      return mItems.get(holder.getAdapterPosition());
                  }
              })
              .subscribeWith(mOnClickSub);

        return holder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder,
                                 final int position) {
        final ChallengeItem item = mItems.get(position);

        mGlide.load(item.getPreviewUrl())
              .into(((MyViewHolder) holder).imageView);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public Observable<ChallengeItem> getOnClickObservable() {
        return mOnClickSub;
    }

    public boolean setItems(List<ChallengeItem> items) {
        if (items.equals(mItems)) return false;

        mItems.clear();
        mItems.addAll(items);

        notifyDataSetChanged();

         return true;
    }

    public List<ChallengeItem> getItems() {
        return mItems;
    }

    public ChallengeItem getItem(int position) {
        return mItems.get(position);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    static class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.img_preview)
        AppCompatImageView imageView;

        public MyViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}
