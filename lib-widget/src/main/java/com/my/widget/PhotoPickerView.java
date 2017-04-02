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
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.my.widget.data.IObservableList;
import com.my.widget.data.IPhoto;
import com.my.widget.data.IPhotoAlbum;
import com.my.widget.data.ObservableArrayList;
import com.my.widget.protocol.IPhotoPicker;
import com.my.widget.util.MediaStoreUtil;
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
 * mPhotoPicker = (PhotoPickerView) findViewById(R.id.photo_picker);
 * // Initialize the selection pool.
 * mPhotoPicker.setSelection(getSelectionProvider());
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
    MySelectionAdapter mSelectionListAdapter;
    RecyclerView mSelectionList;

    // State.
    // FIXME:
    final int mEdgeSlop;
    final int mTouchSlop;
    final PointF mActionDownPoint = new PointF();
    boolean mIsSelectionListExpanded;

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
        mPhotoList = (RecyclerView) findViewById(R.id.photo_list);
        mPhotoList.setHasFixedSize(true);
        mPhotoList.setLayoutManager(new GridLayoutManager(
            getContext(),
            getResources().getInteger(R.integer.photo_picker_grid_column_num),
            GridLayoutManager.VERTICAL,
            false));
        mPhotoList.addItemDecoration(new GridSpacingDecoration(
            mPhotoList,
            (int) getResources().getDimension(R.dimen.grid_item_spacing_big)));
        mPhotoList.setAdapter(mPhotoListAdapter);

        // The selection list.
        mSelectionListAdapter = new MySelectionAdapter(getContext());
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

        final ViewConfiguration cfg = ViewConfiguration.get(context);
        // Edge slop.
        mEdgeSlop = 2 * cfg.getScaledEdgeSlop();
        mTouchSlop = cfg.getScaledTouchSlop();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent evt) {
        final int bottom = getHeight() - getPaddingBottom();
        final int act = MotionEventCompat.getActionMasked(evt);
        final boolean isInterceptDefault = super.onInterceptTouchEvent(evt);

        // Record the dragging start.
        if (act == MotionEvent.ACTION_DOWN) {
            mActionDownPoint.set(evt.getX(),  evt.getY());

            return isInterceptDefault;
        } else if (act == MotionEvent.ACTION_MOVE) {
            // TODO: Check out the history #0.
            final boolean hasSelected = mSelectionListAdapter.getItemCount() > 0;
            final boolean isWithinEdgeSlop = Math.abs(mActionDownPoint.y - bottom) < mEdgeSlop;
            final boolean isDraggingV = Math.abs(evt.getY() - mActionDownPoint.y) > mTouchSlop;
            final boolean isIntercept = (hasSelected && isWithinEdgeSlop && isDraggingV) ||
                                        isInterceptDefault;

            Log.d("xyz", "hasSelected=" + hasSelected +
                         ", isWithinEdgeSlop=" + isWithinEdgeSlop +
                         ", isDraggingV=" + isDraggingV);

            return isIntercept;
        } else {
            return isInterceptDefault;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent evt) {
        final boolean ret = super.onTouchEvent(evt);
        final int act = MotionEventCompat.getActionMasked(evt);

        if (act == MotionEvent.ACTION_MOVE) {
            final float ty = constraintTranslationY(
                evt.getY() - mActionDownPoint.y);

            Log.d("xyz", "ty=" + ty);

            ViewCompat.setTranslationY(mSelectionList, ty);

            return true;
        } else {
            return ret;
        }
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
    public void onSelectPhoto(Checkable view,
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

    public void setSelection(ObservableArrayList.Provider<IPhoto> provider) {
        if (provider == null || provider.getObservableList() == null) {
            throw new IllegalArgumentException("Given provider is invalid");
        }

        // Unregister the old listener.
        if (mSelectionProvider != null) {
            mSelectionProvider
                .getObservableList()
                .removeListener(mOnSelectionChangeListener);
        }
        // Swap provider.
        mSelectionProvider = provider;
        mSelectionProvider
            .getObservableList()
            .addListener(mOnSelectionChangeListener);
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
            final FrameLayout.LayoutParams params =
                (FrameLayout.LayoutParams) mSelectionList.getLayoutParams();
            final int childHeight = mSelectionList.getMeasuredHeight();

            // We have custom behavior for this child view.
            mSelectionList.layout(parentLeft + params.leftMargin,
                                  parentBottom - params.bottomMargin,
                                  parentRight - params.rightMargin,
                                  parentBottom - params.bottomMargin + childHeight);
            // TODO: Use dimens.xml instead.
//            mSelectionList.setTranslationY(-mSelectionList.getMeasuredHeight() / 2);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        // Because the selection is provided by the provider, so previously
        // registering listener would let the provider have the reference of
        // this class.
        // Make sure to remove the selection listener to avoid from leaking.
        if (mSelectionProvider != null) {
            mSelectionProvider
                .getObservableList()
                .removeListener(mOnSelectionChangeListener);
        }
    }

    ObservableArrayList.ListChangeListener<IPhoto> mOnSelectionChangeListener =
        new IObservableList.ListChangeListener<IPhoto>() {
        @Override
        public void onListUpdate(List<IPhoto> list) {
            // Assign new data (will apply to DiffUtil).
            mSelectionListAdapter.setData(list);

            if (mSelectionListAdapter.getItemCount() > 0) {
                if (mIsSelectionListExpanded) {
                    // Expand the selection list.
                    ViewCompat.animate(mSelectionList)
                              .translationY(-mSelectionList.getHeight())
                              .start();
                } else {
                    // Show part of the selection list.
                    ViewCompat.animate(mSelectionList)
                              .translationY(-mEdgeSlop)
                              .start();
                }
            } else {
                mIsSelectionListExpanded = false;

                // Hide the selection list.
                ViewCompat.animate(mSelectionList)
                          .setDuration(200)
                          .translationY(0)
                          .start();
            }

            // TODO: Scroll to the latest update one.
            final int newPosition = mSelectionListAdapter.getItemCount() - 1;
            if (newPosition > 0) {
                mSelectionList.smoothScrollToPosition(newPosition);
            }
        }
    };

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

    float constraintTranslationY(float value) {
        final float min = mAlbumListAdapter.getCount() > 0 ? -mEdgeSlop : 0;

        value = Math.min(min, value);
        value = Math.max(-mSelectionList.getHeight(), value);

        return value;
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
    }

    /**
     * The album photo adapter class.
     */
    private static class MyAlbumPhotoAdapter
        extends CursorRecyclerViewAdapter<RecyclerView.ViewHolder> {

        final IPhotoPicker mPicker;

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
            return new MyPhotoViewHolder(
                getInflater().inflate(R.layout.view_checkable_square_imageview,
                                      parent, false));
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder,
                                     final Cursor cursor,
                                     List<Object> payloads) {
            final CheckableImageView imageView = (CheckableImageView) viewHolder.itemView;
            final int position = viewHolder.getAdapterPosition();
            final IPhoto photo = MediaStoreUtil.getPhotoInfo(cursor);

            imageView.setChecked(mPicker.isPhotoSelected(photo));
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPicker.onSelectPhoto(imageView,
                                          photo,
                                          position);
                }
            });

            Glide.with(getContext())
                 .load(photo.thumbnailPath())
                 .placeholder(ContextCompat.getDrawable(
                     getContext(), R.drawable.bg_borderless_rect_light_gray))
                 .priority(Priority.IMMEDIATE)
                 .into(imageView);
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

        private List<IPhoto> mData = Collections.emptyList();

        MySelectionAdapter(final Context context) {
            mContext = context;
            mInflater = LayoutInflater.from(context);
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
        public void onBindViewHolder(MySelectionViewHolder holder,
                                     int position,
                                     List<Object> payloads) {
            final ImageView imgView = (ImageView) holder.itemView;
            final Drawable drawable = ContextCompat.getDrawable(
                mContext, R.drawable.bg_borderless_rect_light_gray);
            final IPhoto photo = mData.get(position);

            // TODO:

            Glide.with(mContext)
                 .load(photo.thumbnailPath())
                 .placeholder(drawable)
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
}
