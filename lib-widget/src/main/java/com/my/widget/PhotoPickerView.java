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

package com.my.widget;

import android.Manifest;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Checkable;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.my.widget.adapter.CursorRecyclerViewAdapter;
import com.my.widget.data.IPhoto;
import com.my.widget.data.IPhotoAlbum;
import com.my.widget.data.ObservableHashSet;
import com.my.widget.protocol.IPhotoPicker;
import com.my.widget.util.MediaStoreUtil;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * <br/>
 * Usage:
 * <pre>
 * mPhotoPicker = (PhotoPickerView) findViewById(R.id.photo_picker);
 * // Initialize the selection pool.
 * mPhotoPicker.setSelection(this);
 * // Load the default album and photos.
 * mPhotoPicker.loadDefaultAlbumAndPhotos();
 * </pre>
 */
public class PhotoPickerView extends FrameLayout
    implements IPhotoPicker,
               IProgressBarView {

    // View.
    AppCompatSpinner mAlbumList;
    MyAlbumAdapter mAlbumListAdapter;
    RecyclerView mPhotoList;
    MyAlbumPhotoAdapter mPhotoListAdapter;
    SwipeRefreshLayout mPhotoListParent;

    // State.
    // FIXME: Temporarily here.
    // FIXME: Make it order sensitive.
    ObservableHashSet.Provider<IPhoto> mSelectionProvider;

    public PhotoPickerView(@NonNull Context context) {
        this(context, null);
    }

    public PhotoPickerView(@NonNull Context context,
                           @Nullable AttributeSet attrs) {
        super(context, attrs);

        // Inflate the layout.
        inflate(context, R.layout.view_photo_picker, this);

        // The album list.
        mAlbumListAdapter = new MyAlbumAdapter(getContext());
        mAlbumList = (AppCompatSpinner) findViewById(R.id.album_list);
        mAlbumList.setAdapter(mAlbumListAdapter);
        mAlbumList.setOnItemSelectedListener(onClickAlbum());

        // The photo list of the selected album.
        mPhotoListAdapter = new MyAlbumPhotoAdapter(getContext(), this);
        mPhotoList = (RecyclerView) findViewById(R.id.photo_list);
        mPhotoList.setHasFixedSize(true);
        mPhotoList.setAdapter(mPhotoListAdapter);
        mPhotoList.setLayoutManager(new GridLayoutManager(
            getContext(),
            getResources().getInteger(R.integer.photo_picker_grid_column_num)));
        mPhotoList.addItemDecoration(new GridItemDecoration(
            (int) getResources().getDimension(R.dimen.grid_item_spacing),
            getResources().getInteger(R.integer.photo_picker_grid_column_num)));

        // The photo list's parent view.
        mPhotoListParent = (SwipeRefreshLayout) findViewById(R.id.photo_list_parent);
        // Disable the "Swipe" gesture and the animation.
        mPhotoListParent.setEnabled(false);
    }

    @Override
    public void showProgressBar() {
        if (mPhotoListParent == null) return;

        mPhotoListParent.setRefreshing(true);
    }

    @Override
    public void hideProgressBar() {
        if (mPhotoListParent == null) return;

        mPhotoListParent.setRefreshing(false);
    }

    @Override
    public void updateProgress(int i) {
        if (mPhotoListParent == null) return;

        mPhotoListParent.setRefreshing(true);
    }

    @Override
    public boolean isPhotoSelected(IPhoto photo) {
        return photo != null && getSelection().contains(photo);
    }

    @Override
    public void onClickPhoto(Checkable view,
                             IPhoto photo,
                             int position) {
        // Update the checkable state.
        view.toggle();

        // Update the selection pool.
        if (view.isChecked()) {
            getSelection().add(photo);
        } else {
            if (getSelection().contains(photo)) {
                getSelection().remove(photo);
            }
        }
    }

    public void setSelection(ObservableHashSet.Provider<IPhoto> provider) {
        if (provider == null || provider.getObservableSet() == null) {
            throw new IllegalArgumentException("Given provider is invalid");
        }

        mSelectionProvider = provider;
    }

    public ObservableHashSet<IPhoto> getSelection() {
        return mSelectionProvider.getObservableSet();
    }

    public void loadDefaultAlbumAndPhotos() {
        if (mSelectionProvider == null ||
            getSelection() == null) {
            throw new IllegalStateException(
                "The selection set is not initialized. Call setSelection " +
                "before calling this method.");
        }

        // FIXME: Figure out the way of Observable.chain(ob1, ob2, ob3, ...)
        // FIXME: And make showing/hiding the progress bar automatically.
        showProgressBar();
        // Ask for read/write permission first.
        RxPermissions
            .getInstance(getContext())
            .request(Manifest.permission.READ_EXTERNAL_STORAGE,
                     Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap(new Function<Boolean, ObservableSource<List<IPhotoAlbum>>>() {
                @Override
                public ObservableSource<List<IPhotoAlbum>> apply(Boolean granted)
                    throws Exception {
                    if (granted) {
                        // Load album list.
                        return loadAlbumList();
                    } else {
                        final String msg = "READ_EXTERNAL_STORAGE && " +
                                           "WRITE_EXTERNAL_STORAGE are " +
                                           "not granted.";
                        // Show a toast.
                        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT)
                             .show();

                        return Observable.error(new SecurityException(msg));
                    }
                }
            })
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new DisposableObserver<List<IPhotoAlbum>>() {
                @Override
                public void onNext(List<IPhotoAlbum> albums) {
                    // Update the album list.
                    mAlbumListAdapter.setData(albums);
                    // Set selection will trigger AdapterView#onItemSelected.
                    mAlbumList.setSelection(0);
                }

                @Override
                public void onError(Throwable e) {
                    hideProgressBar();
                }

                @Override
                public void onComplete() {
                    hideProgressBar();
                }
            });
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    Observable<List<IPhotoAlbum>> loadAlbumList() {
        return Observable
            .create(new ObservableOnSubscribe<List<IPhotoAlbum>>() {
                @Override
                public void subscribe(ObservableEmitter<List<IPhotoAlbum>> emitter)
                    throws Exception {
                    Log.d("xyz", "loadAlbumList");
                    // TODO: Return cursor instead of the list so that we know
                    // TODO: the cursor change.

                    // TODO: Handle #dispose by using the validToken parameter.

                    // Emit to downstream if not disposed.
                    if (!emitter.isDisposed()) {
                        emitter.onNext(MediaStoreUtil.getAlbums(
                            getContext().getContentResolver(),
                            new AtomicBoolean(true)));
                    }
                    emitter.onComplete();
                }
            })
            .subscribeOn(Schedulers.io());
    }

    Observable<Cursor> loadAlbumPhotoCursor(final String albumId) {
        return Observable
            .create(new ObservableOnSubscribe<Cursor>() {
                @Override
                public void subscribe(ObservableEmitter<Cursor> emitter)
                    throws Exception {
                    Log.d("xyz", "loadAlbumPhotoCursor, albumId=" + albumId);
                    // Emit to downstream if not disposed.
                    if (!emitter.isDisposed()) {
                        emitter.onNext(MediaStoreUtil.getPhotosOfAlbum(
                            getContext().getContentResolver(),
                            albumId));
                    }
                    emitter.onComplete();
                }
            })
            .subscribeOn(Schedulers.io());
    }

    AdapterView.OnItemSelectedListener onClickAlbum() {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int position,
                                       long id) {
                if (mAlbumListAdapter.getCount() == 0) return;

                final String albumId = mAlbumListAdapter.getItem(position).id();
                loadAlbumPhotoCursor(albumId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableObserver<Cursor>() {
                        @Override
                        public void onNext(Cursor cursor) {
                            mPhotoListAdapter.setData(cursor);
                            // Show the top most one.
                            mPhotoList.scrollToPosition(0);
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
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // DO NOTHING.
            }
        };
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    /**
     * The album adapter class.
     */
    private static class MyAlbumAdapter extends BaseAdapter {

        LayoutInflater mInflater;
        List<IPhotoAlbum> mAlbums;

        MyAlbumAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
            mAlbums = new ArrayList<>();
        }

        @Override
        public int getCount() {
            return mAlbums.size();
        }

        @Override
        public IPhotoAlbum getItem(int position) {
            return getCount() > 0 ? mAlbums.get(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @NonNull
        @Override
        public View getView(int position,
                            View convertView,
                            ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position,
                                    View convertView,
                                    ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        public void setData(List<IPhotoAlbum> albums) {
            mAlbums.clear();
            mAlbums.addAll(albums);

            notifyDataSetChanged();
        }

        private View getCustomView(int position,
                                   View convertView,
                                   ViewGroup parent) {
            // Using view-holder is to avoid often finding view ID.
            MyAlbumViewHolder holder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.view_spinner_album_item,
                                                parent,
                                                false);

                holder = new MyAlbumViewHolder();
                holder.albumCover = (ImageView) convertView.findViewById(R.id.album_cover);
                holder.albumName = (TextView) convertView.findViewById(R.id.album_name);
                holder.albumPhotoNum = (TextView) convertView.findViewById(R.id.album_photo_num);

                convertView.setTag(holder);
            } else {
                holder = (MyAlbumViewHolder) convertView.getTag();
            }

            IPhotoAlbum album = getItem(position);
            // TODO: Load the cover image.
            Glide.with(parent.getContext())
                 .load(mAlbums.get(position).thumbnailPath())
                 .into(holder.albumCover);
            // Update the album name.
            holder.albumName.setText(album.name());
            holder.albumPhotoNum.setText("(" + album.photoNum() + ")");

            return convertView;
        }
    }

    /**
     * The album photo adapter class.
     */
    private static class MyAlbumPhotoAdapter
        extends CursorRecyclerViewAdapter<RecyclerView.ViewHolder> {

        final IPhotoPicker mListener;

        MyAlbumPhotoAdapter(Context context,
                            IPhotoPicker listener) {
            super(context);

            if (listener == null) {
                throw new IllegalArgumentException("Given listener is null");
            }

            mListener = listener;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
            return new MyPhotoViewHolder(
                getInflater().inflate(R.layout.view_photo_grid_item,
                                      parent, false));
        }

        // FIXME: These two functions will cause Glide loading incorrectly.
//        @Override
//        public void onViewRecycled(RecyclerView.ViewHolder holder) {
//            super.onViewRecycled(holder);
//            Log.d("xyz", "onViewRecycled");
//            Glide.clear(holder.itemView);
//        }
//
//        @Override
//        public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
//            super.onViewDetachedFromWindow(holder);
//            Log.d("xyz", "onViewDetachedFromWindow");
//            Glide.clear(holder.itemView);
//        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder,
                                     final Cursor cursor,
                                     List<Object> payloads) {
            final CheckableImageView imageView = (CheckableImageView) viewHolder.itemView;
            final int position = viewHolder.getAdapterPosition();
            final IPhoto photo = MediaStoreUtil.getPhotoInfo(cursor);

            imageView.setChecked(mListener.isPhotoSelected(photo));
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onClickPhoto(imageView,
                                           photo,
                                           position);
                }
            });

            Glide.with(getContext())
                 .load(photo.thumbnailPath())
                 .priority(Priority.IMMEDIATE)
                 // FIXME: Animation causes ghost images.
                 .dontAnimate()
//                 .skipMemoryCache(true)
//                 .diskCacheStrategy(DiskCacheStrategy.NONE)
                 .into(imageView);
        }
    }

    private static class MyAlbumViewHolder {
        ImageView albumCover;
        TextView albumName;
        TextView albumPhotoNum;
    }

    private static class MyPhotoViewHolder extends RecyclerView.ViewHolder {

        MyPhotoViewHolder(View view) {
            super(view);
        }
    }
}
