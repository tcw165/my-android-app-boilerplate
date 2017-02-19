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

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.my.comp.data.IPhotoAlbum;
import com.my.comp.util.MediaStoreUtil;
import com.my.widget.IProgressBarView;
import com.my.widget.util.ViewUtil;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class PhotoPickerActivity
    extends AppCompatActivity
    implements IProgressBarView {

    MyAlbumAdapter mAlbumAdapter;
    MyAlbumPhotoAdapter mAlbumPhotoAdapter;

    Toolbar mToolbar;
    AppCompatSpinner mAlbumList;
    RecyclerView mPhotoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_photo_picker);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // The album list.
        mAlbumAdapter = new MyAlbumAdapter(this);
        mAlbumList = (AppCompatSpinner) findViewById(R.id.album_list);
        mAlbumList.setAdapter(mAlbumAdapter);
        mAlbumList.setOnItemSelectedListener(onClickAlbum());

        // The photo list of the selected album.
        mAlbumPhotoAdapter = new MyAlbumPhotoAdapter();
        mPhotoList = (RecyclerView) findViewById(R.id.photo_list);
        mPhotoList.setAdapter(mAlbumPhotoAdapter);

        // FIXME: Add observer.
//        Cursor#requery()

        // Load the albums.
        loadAlbumListAndPhotosOfDefaultAlbum();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mAlbumPhotoAdapter != null) {
            mAlbumPhotoAdapter.clear();
        }

        // TODO: Support "don't keep Activity".
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.menu_photo_picker, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();

        if (i == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (i == R.id.item_take_photo) {
            startActivity(new Intent(this, TakePhotoDelegateActivity.class));
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void showProgressBar() {
        ViewUtil
            .with(this)
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
        // DO NOTHING.
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    Observable<List<IPhotoAlbum>> loadAlbumList() {
        return Observable
            .create(new ObservableOnSubscribe<List<IPhotoAlbum>>() {
                @Override
                public void subscribe(ObservableEmitter<List<IPhotoAlbum>> emitter)
                    throws Exception {
                    emitter.onNext(MediaStoreUtil.getAlbums(getContentResolver()));
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
                    // TODO: Complete it.
                    emitter.onComplete();
                }
            })
            .subscribeOn(Schedulers.io());
    }

    void loadAlbumListAndPhotosOfDefaultAlbum() {
        // FIXME: Figure out the way of Observable.chain(ob1, ob2, ob3, ...)
        loadAlbumList()
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
                public void onNext(Cursor value) {
                    // TODO: Complete it.
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

    void loadPhotoListByAlbum(String albumId) {
        loadAlbumPhotoCursor(albumId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new DisposableObserver<Cursor>() {
                @Override
                public void onNext(Cursor value) {

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

    OnItemSelectedListener onClickAlbum() {
        return new OnItemSelectedListener() {
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
    static class MyAlbumAdapter extends BaseAdapter {

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
                            @NotNull ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position,
                                    View convertView,
                                    @NotNull ViewGroup parent) {
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
            AlbumViewHolder holder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.view_spinner_album_item,
                                                parent,
                                                false);

                holder = new AlbumViewHolder();
                holder.albumCover = (ImageView) convertView.findViewById(R.id.album_cover);
                holder.albumName = (TextView) convertView.findViewById(R.id.album_name);
                holder.albumPhotoNum = (TextView) convertView.findViewById(R.id.album_photo_num);

                convertView.setTag(holder);
            } else {
                holder = (AlbumViewHolder) convertView.getTag();
            }

            IPhotoAlbum album = getItem(position);
            // TODO: Load the cover image.
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
    static class MyAlbumPhotoAdapter extends RecyclerView.Adapter<ViewHolder> {

        Cursor mCursor;

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent,
                                             int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            return new AlbumPhotoViewHolder(
                inflater.inflate(R.layout.view_photo_grid_item, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder,
                                     int position) {
            final Context context = holder.itemView.getContext();

//            Glide.with(context)
//                .load()
        }

        @Override
        public int getItemCount() {
            if (mCursor == null || mCursor.isClosed()) {
                return 0;
            } else {
                return mCursor.getCount();
            }
        }

        void setData(Cursor cursor) {
            clear();

            mCursor = cursor;
        }

        void clear() {
            if (mCursor != null && !mCursor.isClosed()) {
                mCursor.close();
            }
        }

        static class AlbumPhotoViewHolder extends ViewHolder {

            AlbumPhotoViewHolder(View view) {
                super(view);
            }
        }
    }
}
