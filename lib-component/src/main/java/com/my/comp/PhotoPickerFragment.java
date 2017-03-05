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

import android.Manifest;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.my.comp.data.IPhotoAlbum;
import com.my.comp.util.MediaStoreUtil;
import com.my.comp.widget.CursorRecyclerViewAdapter;
import com.my.widget.IProgressBarView;
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

// TODO: We could actually make it a ViewGroup and be used by a ViewPager.
public class PhotoPickerFragment extends Fragment
    implements IProgressBarView {

    // View.
    AppCompatSpinner mAlbumList;
    RecyclerView mPhotoList;
    SwipeRefreshLayout mPhotoListParent;

    MyAlbumAdapter mAlbumAdapter;
    MyAlbumPhotoAdapter mAlbumPhotoAdapter;

    public PhotoPickerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PhotoPickerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PhotoPickerFragment newInstance() {
        final PhotoPickerFragment fragment = new PhotoPickerFragment();
        final Bundle args = new Bundle();

        // Custom arguments.
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_photo_picker,
                                       container, false);

        // The album list.
        mAlbumAdapter = new MyAlbumAdapter(getActivity());
        mAlbumList = (AppCompatSpinner) layout.findViewById(R.id.album_list);
        mAlbumList.setAdapter(mAlbumAdapter);
        mAlbumList.setOnItemSelectedListener(onClickAlbum());

        // The photo list of the selected album.
        mAlbumPhotoAdapter = new MyAlbumPhotoAdapter(getActivity());
        mPhotoList = (RecyclerView) layout.findViewById(R.id.photo_list);
        mPhotoList.setHasFixedSize(true);
        mPhotoList.setAdapter(mAlbumPhotoAdapter);
        // TODO: Use
        mPhotoList.setLayoutManager(new GridLayoutManager(
            getContext(), getResources().getInteger(R.integer.photo_picker_grid_column_num)));

        // The photo list's parent view.
        mPhotoListParent = (SwipeRefreshLayout) layout.findViewById(R.id.photo_list_parent);

        // Load the albums.
        loadDefaultAlbumAndPhotos();

        return layout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Recycler the cursor.
        mAlbumPhotoAdapter.setData(null);
        // TODO: Support "don't keep Activity".
    }

    @Override
    public void showProgressBar() {
        mPhotoListParent.setRefreshing(true);
    }

    @Override
    public void hideProgressBar() {
        mPhotoListParent.setRefreshing(false);
    }

    @Override
    public void updateProgress(int i) {
        mPhotoListParent.setRefreshing(true);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    Observable<List<IPhotoAlbum>> loadAlbumList() {
        return Observable
            .create(new ObservableOnSubscribe<List<IPhotoAlbum>>() {
                @Override
                public void subscribe(ObservableEmitter<List<IPhotoAlbum>> emitter)
                    throws Exception {
                    // TODO: Return cursor instead of the list so that we know
                    // TODO: the cursor change.

                    // TODO: Handle #dispose by using the validToken parameter.

                    // Emit to downstream if not disposed.
                    if (!emitter.isDisposed()) {
                        emitter.onNext(MediaStoreUtil.getAlbums(
                            getActivity().getContentResolver(),
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
                    // Emit to downstream if not disposed.
                    if (!emitter.isDisposed()) {
                        emitter.onNext(MediaStoreUtil.getPhotosOfAlbum(
                            getActivity().getContentResolver(),
                            albumId));
                    }
                    emitter.onComplete();
                }
            })
            .subscribeOn(Schedulers.io());
    }

    void loadDefaultAlbumAndPhotos() {
        // FIXME: Figure out the way of Observable.chain(ob1, ob2, ob3, ...)
        // FIXME: And make showing/hiding the progress bar automatically.
        showProgressBar();
        // Ask for read/write permission first.
        RxPermissions
            .getInstance(getActivity())
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
                        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT)
                             .show();

                        return Observable.error(new SecurityException(msg));
                    }
                }
            })
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap(new Function<List<IPhotoAlbum>, ObservableSource<Cursor>>() {
                @Override
                public ObservableSource<Cursor> apply(List<IPhotoAlbum> albums)
                    throws Exception {
                    final String defaultAlbumId = albums.get(0).id();

                    // Update the album list.
                    mAlbumAdapter.setData(albums);

                    return loadAlbumPhotoCursor(defaultAlbumId);
                }
            })
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new DisposableObserver<Cursor>() {
                @Override
                public void onNext(Cursor cursor) {
                    // DO NOTHING.
                    // The spinner will trigger the OnItemSelectedListener if
                    // the data is valid.
                }

                @Override
                public void onError(Throwable e) {
                    hideProgressBar();
                    // Finish the activity.
                    getActivity().onBackPressed();
                }

                @Override
                public void onComplete() {
                    hideProgressBar();
                }
            });
    }

    void loadPhotoListByAlbum(String albumId) {
        loadAlbumPhotoCursor(albumId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new DisposableObserver<Cursor>() {
                @Override
                public void onNext(Cursor cursor) {
                    mAlbumPhotoAdapter.setData(cursor);
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

    AdapterView.OnItemSelectedListener onClickAlbum() {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int position,
                                       long id) {
                if (mAlbumAdapter.getCount() == 0) return;

                loadPhotoListByAlbum(mAlbumAdapter
                                         .getItem(position)
                                         .id());
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
            MyAlbumAdapter.AlbumViewHolder holder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.view_spinner_album_item,
                                                parent,
                                                false);

                holder = new MyAlbumAdapter.AlbumViewHolder();
                holder.albumCover = (ImageView) convertView.findViewById(R.id.album_cover);
                holder.albumName = (TextView) convertView.findViewById(R.id.album_name);
                holder.albumPhotoNum = (TextView) convertView.findViewById(R.id.album_photo_num);

                convertView.setTag(holder);
            } else {
                holder = (MyAlbumAdapter.AlbumViewHolder) convertView.getTag();
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

        static class AlbumViewHolder {
            ImageView albumCover;
            TextView albumName;
            TextView albumPhotoNum;
        }
    }

    /**
     * The album photo adapter class.
     */
    private static class MyAlbumPhotoAdapter
        extends CursorRecyclerViewAdapter<RecyclerView.ViewHolder> {

        MyAlbumPhotoAdapter(Context context) {
            super(context);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
            return new MyAlbumPhotoAdapter.AlbumPhotoViewHolder(
                getInflater().inflate(R.layout.view_photo_grid_item,
                                      parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder,
                                     Cursor cursor,
                                     List<Object> payloads) {
            final AppCompatImageView imageView = (AppCompatImageView) viewHolder.itemView;

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: DO SOMETHING.
                }
            });

            Glide.with(getContext())
                 .load(MediaStoreUtil.getThumbnailPath(cursor))
                 .into(imageView);
        }

        static class AlbumPhotoViewHolder extends RecyclerView.ViewHolder {

            AlbumPhotoViewHolder(View view) {
                super(view);
            }
        }
    }
}
