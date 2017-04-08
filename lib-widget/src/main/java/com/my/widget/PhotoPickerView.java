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
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.my.core.adapter.CursorRecyclerViewAdapter;
import com.my.core.data.ObservableArrayList;
import com.my.core.protocol.IObservableList;
import com.my.core.protocol.IPhoto;
import com.my.core.protocol.IPhotoAlbum;
import com.my.core.protocol.IPhotoPicker;
import com.my.core.protocol.IProgressBarView;
import com.my.core.util.MediaStoreUtil;
import com.my.widget.decoration.GridSpacingDecoration;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.Collections;
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
 *
 * mPhotoPicker = (PhotoPickerView) findViewById(R.id.photo_picker);
 *
 * // Initialize the selection pool.
 * mPhotoPicker.setSelection(getSelectionProvider());
 *
 * // Load the default album and photos.
 * mPhotoPicker.loadDefaultAlbumAndPhotos();
 *
 * </pre>
 */
// FIXME: The elastic-drag doesn't work.
// FIXME: Update selection list when the cursor is changed.
public class PhotoPickerView extends CoordinatorLayout
    implements IPhotoPicker,
               IProgressBarView,
               IObservableList.ListChangeListener<IPhoto> {

    // View.
    AppCompatSpinner mAlbumList;
    MyAlbumAdapter mAlbumListAdapter;
    RecyclerView mPhotoList;
    MyAlbumPhotoAdapter mPhotoListAdapter;
    GridLayoutManager mPhotoListLayoutMgr;
    SwipeRefreshLayout mPhotoListParent;
    MySelectionAdapter mSelectionListAdapter;
    RecyclerView mSelectionList;

    // State.
    final MySelectionViewHelper mSelectionViewHelper;

    // Selection.
    ObservableArrayList.Provider<IPhoto> mSelectionProvider;

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
        mPhotoListLayoutMgr = new GridLayoutManager(
            getContext(),
            getResources().getInteger(R.integer.photo_picker_grid_column_num),
            GridLayoutManager.VERTICAL,
            false);
        mPhotoList = (RecyclerView) findViewById(R.id.photo_list);
        mPhotoList.setHasFixedSize(true);
        mPhotoList.setLayoutManager(mPhotoListLayoutMgr);
        mPhotoList.addItemDecoration(new GridSpacingDecoration(
            mPhotoList,
            (int) getResources().getDimension(R.dimen.grid_item_spacing_big)));
        mPhotoList.setAdapter(mPhotoListAdapter);

        // The selection list.
        mSelectionListAdapter = new MySelectionAdapter(getContext(), this);
        mSelectionList = (RecyclerView) findViewById(R.id.selection_list);
        mSelectionList.setHasFixedSize(true);
        mSelectionList.setLayoutManager(new LinearLayoutManager(
            getContext(),
            LinearLayoutManager.HORIZONTAL,
            false));
        mSelectionList.addItemDecoration(new GridSpacingDecoration(
            mSelectionList,
            (int) getResources().getDimension(R.dimen.grid_item_spacing_medium)));
        mSelectionList.setAdapter(mSelectionListAdapter);

        // The photo list's parent view.
        mPhotoListParent = (SwipeRefreshLayout) findViewById(R.id.photo_list_parent);
        // Disable the "Swipe" gesture and the animation.
        mPhotoListParent.setEnabled(false);

        final ViewGroup.LayoutParams params = mSelectionList.getLayoutParams();
        // Helper.
        mSelectionViewHelper = new MySelectionViewHelper(
            getContext(),
            // The selection list.
            mSelectionList,
            // ty for completely hidden.
            0,
            // ty for partially revealing.
            -getResources().getDimension(R.dimen.partially_revealing_height),
            // ty for completely revealing.
            -params.height);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        // Because the selection is provided by the provider, so previously
        // registering listener would let the provider have the reference of
        // this class.
        // Make sure to remove the selection listener to avoid from leaking.
        if (mSelectionProvider != null) {
            mSelectionProvider
                .getObservableList()
                .removeListener(this);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent evt) {
        return mSelectionViewHelper.onInterceptTouchEvent(evt) ||
               super.onInterceptTouchEvent(evt);
    }

    @Override
    public boolean onTouchEvent(MotionEvent evt) {
        return mSelectionViewHelper.onTouchEvent(evt) ||
               super.onTouchEvent(evt);
    }

    @Override
    public void onItemAdded(List<IPhoto> list,
                            IPhoto item) {
        onSelectionChanged(list, item, null, null);
    }

    @Override
    public void onItemRemoved(List<IPhoto> list,
                              IPhoto item) {
        onSelectionChanged(list, null, item, null);
    }

    @Override
    public void onItemChanged(List<IPhoto> list,
                              IPhoto item) {
        onSelectionChanged(list, null, null, item);
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
    public List<IPhoto> getSelectedPhoto() {
        return getSelection();
    }

    @Override
    public void onClickPhoto(Checkable view,
                             IPhoto photo,
                             int position) {
        if (isPhotoSelected(photo)) {
            onDeselectPhoto(photo, position);
        } else {
            onSelectPhoto(photo, position);
        }
    }

    @Override
    public void onSelectPhoto(IPhoto photo,
                              int position) {
        // Will trigger onSelectionChanged();
        getSelection().add(photo);
    }

    @Override
    public void onDeselectPhoto(IPhoto photo,
                                int position) {
        if (getSelection().contains(photo)) {
            // Will trigger onSelectionChanged();
            getSelection().remove(photo);
        }
    }

    public void setSelection(ObservableArrayList.Provider<IPhoto> provider) {
        if (provider == null || provider.getObservableList() == null) {
            throw new IllegalArgumentException("Given provider is invalid");
        }

        // Unregister the old listener.
        if (mSelectionProvider != null) {
            mSelectionProvider
                .getObservableList()
                .removeListener(this);
        }
        // Swap provider.
        mSelectionProvider = provider;
        mSelectionProvider
            .getObservableList()
            .addListener(this);
    }

    public List<IPhoto> getSelection() {
        return mSelectionProvider.getObservableList();
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

    @Override
    protected void onMeasure(int widthMeasureSpec,
                             int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed,
                            int left,
                            int top,
                            int right,
                            int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        final int parentLeft = getPaddingLeft();
        final int parentRight = right - left - getPaddingRight();
        final int parentBottom = bottom - top - getPaddingBottom();

        // Place the selection list at the bottom.
        if (mSelectionList != null) {
            final ViewGroup.MarginLayoutParams params =
                (ViewGroup.MarginLayoutParams) mSelectionList.getLayoutParams();
            final int childHeight = mSelectionList.getMeasuredHeight();

            // We have custom behavior for this child view.
            mSelectionList.layout(parentLeft + params.leftMargin,
                                  parentBottom - params.bottomMargin,
                                  parentRight - params.rightMargin,
                                  parentBottom - params.bottomMargin + childHeight);
        }
    }

    // TODO: Complete the save-state.
    @Override
    protected Parcelable onSaveInstanceState() {
        return super.onSaveInstanceState();
    }

    // TODO: Complete the restore-state.
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
    }

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

                final String albumId = mAlbumListAdapter.getItem(position).bucketId();
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

    void onSelectionChanged(List<IPhoto> list,
                            IPhoto added,
                            IPhoto removed,
                            IPhoto updated) {
        // Notify the checked state change to the photo list.
        // (cannot use DiffUtil because the adapter is a cursor-adapter).
        final int first = mPhotoListLayoutMgr.findFirstVisibleItemPosition();
        final int last = mPhotoListLayoutMgr.findLastVisibleItemPosition();
        final int range = last - first;
        final int start = Math.max(0, first - range);
        final int end = Math.min(mPhotoListAdapter.getItemCount() - 1, last + range);
        for (int i = start; i <= end; ++i) {
            final IPhoto photo = mPhotoListAdapter.getItemAt(i);

            if (added != null && added.equals(photo)) {
                mPhotoListAdapter.notifyItemChanged(
                    i, MyAlbumPhotoAdapter.PAYLOAD_ITEM_CHECKED);
            } else if (removed != null && removed.equals(photo)) {
                mPhotoListAdapter.notifyItemChanged(
                    i, MyAlbumPhotoAdapter.PAYLOAD_ITEM_UNCHECKED);
            }
        }

        // Assign new data (will apply to DiffUtil).
        mSelectionListAdapter.setData(list);
        // Update the position of selection view.
        mSelectionViewHelper.onListUpdate();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    /**
     * The album mTargetAdapter class.
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
                 .placeholder(ContextCompat.getDrawable(
                     convertView.getContext(),
                     R.drawable.bg_borderless_rect_light_gray))
                 .into(holder.albumCover);
            // Update the album name.
            holder.albumName.setText(album.name());
            holder.albumPhotoNum.setText("(" + album.photoNum() + ")");

            return convertView;
        }
    }

    private static class MyAlbumViewHolder {
        ImageView albumCover;
        TextView albumName;
        TextView albumPhotoNum;

        MyAlbumViewHolder() {
            // DO NOTHING.
        }
    }

    /**
     * The album photo mTargetAdapter class.
     */
    private static class MyAlbumPhotoAdapter
        extends CursorRecyclerViewAdapter<RecyclerView.ViewHolder> {

        final IPhotoPicker mPicker;
        int viewHoldCount = 0;

        MyAlbumPhotoAdapter(Context context,
                            IPhotoPicker picker) {
            super(context);

            if (picker == null) {
                throw new IllegalArgumentException("Given listener is null");
            }

            mPicker = picker;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
            ++viewHoldCount;
            return new MyPhotoViewHolder(
                getInflater().inflate(R.layout.view_checkable_square_imageview,
                                      parent, false));
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder,
                                     final Cursor cursor,
                                     final List<Object> payloads) {
            final CheckableImageView imageView = (CheckableImageView) holder.itemView;
            final IPhoto photo = MediaStoreUtil.getPhoto(cursor);

            // Checked or unchecked.
            if (payloads == null || payloads.isEmpty()) {
                Log.d("xyz", "bind " + holder.getAdapterPosition() + " without payload" +
                             " (cursor=" + cursor.getPosition() + "), holder=" + holder +
                             ", item id=" + getItemId(cursor.getPosition()));

                imageView.setChecked(mPicker.isPhotoSelected(photo));

                // Update the scale effect.
                if (imageView.isChecked()) {
                    ViewCompat.animate(imageView)
                              .setDuration(0)
                              .scaleX(0.95f)
                              .scaleY(0.95f)
                              .start();
                } else {
                    ViewCompat.animate(imageView)
                              .setDuration(0)
                              .scaleX(1f)
                              .scaleY(1f)
                              .start();
                }

                // Clear old onClick listener.
                imageView.setOnClickListener(null);
                final View.OnClickListener listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mPicker.onClickPhoto(imageView,
                                             photo,
                                             holder.getAdapterPosition());
                    }
                };

                Glide.with(getContext())
                     .load(photo.thumbnailPath())
                     .listener(new RequestListener<String, GlideDrawable>() {
                         @Override
                         public boolean onException(Exception e,
                                                    String model,
                                                    Target<GlideDrawable> target,
                                                    boolean isFirstResource) {
                             return false;
                         }

                         @Override
                         public boolean onResourceReady(GlideDrawable resource,
                                                        String model,
                                                        Target<GlideDrawable> target,
                                                        boolean isFromMemoryCache,
                                                        boolean isFirstResource) {
                             // onClick listener.
                             imageView.setOnClickListener(listener);
                             return false;
                         }
                     })
                     .placeholder(ContextCompat.getDrawable(
                         getContext(), R.drawable.bg_borderless_rect_light_gray))
                     .priority(Priority.IMMEDIATE)
                     .into(imageView);
            } else {
                Log.d("xyz", "bind " + holder.getAdapterPosition() + " with payload" +
                             " (cursor=" + cursor.getPosition() + "), holder=" + holder +
                             ", item id=" + getItemId(cursor.getPosition()));
                for (Object payload : payloads) {
                    if ((Integer) payload == PAYLOAD_ITEM_CHECKED) {
                        imageView.setChecked(true);

                        // FIXME: Buggy.
                        // Update the scale effect.
                        ViewCompat.animate(imageView)
                                  .setDuration(0)
                                  .scaleX(0.95f)
                                  .scaleY(0.95f)
                                  .setInterpolator(new OvershootInterpolator(10f))
                                  .start();
                    } else if ((Integer) payload == PAYLOAD_ITEM_UNCHECKED) {
                        imageView.setChecked(false);

                        // FIXME: Buggy.
                        // Update the scale effect.
                        ViewCompat.animate(imageView)
                                  .setDuration(0)
                                  .scaleX(1f)
                                  .scaleY(1f)
                                  .setInterpolator(new AccelerateInterpolator())
                                  .start();
                    }
                }
            }
        }

        public IPhoto getItemAt(int position) {
            final Cursor cursor = getCursor();

            if (cursor.moveToPosition(position)) {
                return MediaStoreUtil.getPhoto(cursor);
            } else {
                return null;
            }
        }
    }

    private static class MyPhotoViewHolder extends RecyclerView.ViewHolder {

        MyPhotoViewHolder(View view) {
            super(view);

            // Here, we need its fixed dimension to be width.
            final CheckableImageView imgView = (CheckableImageView) view;
            imgView.setFixedDimension(CheckableImageView.FIXED_WIDTH);
        }
    }

    // TODO: Apply DiffUtil.
    private static class MySelectionAdapter
        extends RecyclerView.Adapter<MySelectionViewHolder> {

        final private Context mContext;
        final private LayoutInflater mInflater;

        final IPhotoPicker mPicker;

        private List<IPhoto> mData = Collections.emptyList();

        MySelectionAdapter(final Context context,
                           final IPhotoPicker picker) {
            mContext = context;
            mInflater = LayoutInflater.from(context);
            mPicker = picker;
        }

        @Override
        public MySelectionViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {
            return new MySelectionViewHolder(
                mInflater.inflate(R.layout.view_deletable_square_imageview, parent, false));
        }

        @Override
        public void onBindViewHolder(MySelectionViewHolder holder,
                                     int position) {
            this.onBindViewHolder(holder, position, null);
        }

        @Override
        public void onBindViewHolder(final MySelectionViewHolder holder,
                                     final int position,
                                     final List<Object> payloads) {
            final ImageView imgView = (ImageView) holder.itemView;
            final IPhoto photo = mData.get(position);

            imgView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPicker.onDeselectPhoto(photo, holder.getAdapterPosition());
                }
            });

            Glide.with(mContext)
                 .load(photo.thumbnailPath())
                 .placeholder(ContextCompat.getDrawable(
                     mContext, R.drawable.bg_borderless_rect_light_gray))
                 .priority(Priority.IMMEDIATE)
                 .into(imgView);
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        public void setData(List<IPhoto> data) {
            if (data == null) {
                throw new IllegalArgumentException("Given list is null");
            }

            // Compare old data and new data.
            final List<IPhoto> oldData = mData;
            mData = new ArrayList<>(data);

            // Leverage diff-util.
            DiffUtil.DiffResult diff = DiffUtil.calculateDiff(
                new MySelectionDiff(oldData, mData));

            diff.dispatchUpdatesTo(this);
        }
    }

    private static class MySelectionViewHolder extends RecyclerView.ViewHolder {

        MySelectionViewHolder(View view) {
            super(view);

            // Here, we need its fixed dimension to be height.
            final CheckableImageView imgView = (CheckableImageView) view;
            imgView.setFixedDimension(CheckableImageView.FIXED_HEIGHT);
        }
    }

    /**
     * Selection list comparator.
     */
    private static class MySelectionDiff extends DiffUtil.Callback {

        final List<IPhoto> mOld;
        final List<IPhoto> mNew;

        MySelectionDiff(List<IPhoto> oldList,
                        List<IPhoto> newList) {
            mOld = oldList;
            mNew = newList;
        }

        @Override
        public int getOldListSize() {
            return mOld.size();
        }

        @Override
        public int getNewListSize() {
            return mNew.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition,
                                       int newItemPosition) {
            final IPhoto o = mOld.get(oldItemPosition);
            final IPhoto n = mNew.get(newItemPosition);
            return o.fullSizePath().equals(n.fullSizePath());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition,
                                          int newItemPosition) {
            final IPhoto o = mOld.get(oldItemPosition);
            final IPhoto n = mNew.get(newItemPosition);
            return o.fullSizePath().equals(n.fullSizePath()) &&
                   o.width() == n.width() &&
                   o.height() == n.height();
        }

        @Nullable
        @Override
        public Object getChangePayload(int oldItemPosition,
                                       int newItemPosition) {
            return super.getChangePayload(oldItemPosition, newItemPosition);
        }
    }

    /**
     * For handling show/hide the target view.
     */
    private static class MySelectionViewHelper {

        private static final int STATE_SETTLING = 0;
        private static final int STATE_PARTIALLY_REVEALING = 1;
        private static final int STATE_COMPLETELY_REVEALING = 2;
        private static final int STATE_COMPLETELY_HIDDEN = 3;

        private int mState = STATE_COMPLETELY_HIDDEN;
        private boolean mIsHandling = false;
        private float mTouchSlop;

        private final RecyclerView mTargetView;

        private final float mTyForCompletelyHidden;
        private final float mTyForPartiallyRevealing;
        private final float mTyForCompletelyRevealing;

        private final PointF mActionDownPoint = new PointF();

        private final PointF mStartPoint = new PointF();
        private final PointF mCurrentPoint = new PointF();
        private final PointF mStartTranslationY = new PointF();

        private final Rect mHitRect = new Rect();

        /**
         * For handling show/hide the target view.
         * <br/>
         * <ul>
         *     <li>See {@link #onInterceptTouchEvent(MotionEvent)}</li>
         *     <li>See {@link #onTouchEvent(MotionEvent)}</li>
         *     <li>See {@link #onListUpdate()}</li>
         * </ul>
         *
         * @param targetView               The target {@link RecyclerView}
         * @param tyForCompletelyHidden    The translationY of targetView for
         *                                 completely hidden state.
         * @param tyForPartiallyRevealing  The translationY of targetView for
         *                                 partially revealing state.
         * @param tyForCompletelyRevealing The translationY of targetView for
         *                                 completely revealing state.
         */
        MySelectionViewHelper(Context context,
                              RecyclerView targetView,
                              float tyForCompletelyHidden,
                              float tyForPartiallyRevealing,
                              float tyForCompletelyRevealing) {
            final ViewConfiguration cfg = ViewConfiguration.get(context);
            mTouchSlop = cfg.getScaledTouchSlop();

            mTargetView = targetView;
            mTyForCompletelyHidden = tyForCompletelyHidden;
            mTyForPartiallyRevealing = tyForPartiallyRevealing;
            mTyForCompletelyRevealing = tyForCompletelyRevealing;
        }

        /**
         * Called in caller's {@link ViewGroup#onInterceptTouchEvent(MotionEvent)}.
         */
        boolean onInterceptTouchEvent(MotionEvent evt) {
            final int act = MotionEventCompat.getActionMasked(evt);

            if (act == MotionEvent.ACTION_DOWN) {
                mActionDownPoint.set(evt.getX(), evt.getY());
            }

            // Record the dragging start.
            if (act == MotionEvent.ACTION_MOVE && hasSelected()) {
                // Cache the boolean for determining the state later.
                final boolean wasHandling = mIsHandling;

                // Get the hit boundary.
                mTargetView.getHitRect(mHitRect);

                if (mState == STATE_PARTIALLY_REVEALING) {
                    // Enlarge the hit rect.
                    mHitRect.inset(0, (int) (-3f * mTouchSlop));

                    // TODO: Check out the history #0.
                    // Try to initiate the gesture.
                    mIsHandling =
                        // Is touching the view.
                        mHitRect.contains((int) evt.getX(), (int) evt.getY()) &&
                        // Is dragging up.
                        (mActionDownPoint.y - evt.getY()) > mTouchSlop;
                } else if (mState == STATE_COMPLETELY_REVEALING) {
                    // Enlarge the hit rect.
                    mHitRect.inset(0, (int) -mTouchSlop);

                    // TODO: Check out the history #0.
                    // Try to initiate the gesture.
                    mIsHandling =
                        // Is touching the view.
                        mHitRect.contains((int) evt.getX(), (int) evt.getY()) &&
                        // Is dragging down.
                        (evt.getY() - mActionDownPoint.y) > mTouchSlop;
                }

                if (wasHandling != mIsHandling) {
                    // Cache the starting state if it is from partially revealing
                    // to completely revealing.
                    mStartPoint.set(evt.getX(), evt.getY());
                    mStartTranslationY.set(mTargetView.getTranslationX(),
                                           mTargetView.getTranslationY());
                }
            }

            return mIsHandling;
        }

        /**
         * Called in caller's {@link View#onTouchEvent(MotionEvent)}.
         */
        boolean onTouchEvent(MotionEvent evt) {
            final int act = MotionEventCompat.getActionMasked(evt);

            if (act == MotionEvent.ACTION_MOVE) {
                // Cancel the animation if it is present.
                ViewCompat.animate(mTargetView).cancel();

                // Update the translationY.
                mState = STATE_SETTLING;
                mCurrentPoint.set(evt.getX(), evt.getY());

                // Animate the target view.
                ViewCompat.setTranslationY(mTargetView, getTranslationY());
            } else if (act == MotionEvent.ACTION_UP ||
                       act == MotionEvent.ACTION_CANCEL) {
                final float ty = mTargetView.getTranslationY();

                // Determine the gesture state.
                if (hasSelected()) {
                    if (Math.abs(ty) > mTargetView.getHeight() / 2) {
                        mState = STATE_COMPLETELY_REVEALING;
                    } else {
                        mState = STATE_PARTIALLY_REVEALING;
                    }
                } else {
                    mState = STATE_COMPLETELY_HIDDEN;
                }

                // Animate the target view.
                ViewCompat.animate(mTargetView)
                          .setDuration(150)
                          .translationY(getTranslationY())
                          .start();

                // Terminate the gesture.
                mIsHandling = false;
            }

            return mIsHandling;
        }

        /**
         * Called in caller's {@link IObservableList.ListChangeListener}.
         */
        void onListUpdate() {
            if (hasSelected()) {
                if (mState == STATE_COMPLETELY_HIDDEN) {
                    mState = STATE_PARTIALLY_REVEALING;
                }
            } else {
                mState = STATE_COMPLETELY_HIDDEN;
            }

            ViewCompat.animate(mTargetView)
                      .translationY(getTranslationY())
                      .start();

            // TODO: Scroll to the latest update one.
//            final int newPosition = mTargetView.getAdapter().getItemCount() - 1;
//            if (newPosition > 0) {
//                mTargetView.smoothScrollToPosition(newPosition);
//            }
        }

        private boolean hasSelected() {
            return mTargetView.getAdapter().getItemCount() > 0;
        }

        private float getTranslationY() {
            if (hasSelected()) {
                if (mState == STATE_COMPLETELY_REVEALING) {
                    return mTyForCompletelyRevealing;
                } else if (mState == STATE_PARTIALLY_REVEALING) {
                    return mTyForPartiallyRevealing;
                } else if (mState == STATE_SETTLING) {
                    float ty = mStartTranslationY.y + (mCurrentPoint.y - mStartPoint.y);

                    // Constraint ty.
                    ty = Math.min(mTyForPartiallyRevealing, ty);
                    ty = Math.max(mTyForCompletelyRevealing, ty);

                    return ty;
                } else {
                    throw new IllegalStateException(
                        "State(=" + mState + ") is not valid when " +
                        "hasSelected()=" + hasSelected());
                }
            } else {
                return mTyForCompletelyHidden;
            }
        }
    }
}
