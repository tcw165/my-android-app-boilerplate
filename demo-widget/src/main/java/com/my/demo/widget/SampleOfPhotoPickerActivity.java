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

package com.my.demo.widget;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.my.comp.TakePhotoDelegateActivity;
import com.my.core.data.ObservableArrayList;
import com.my.core.protocol.IPhoto;
import com.my.widget.PhotoPickerView;

import java.util.List;
import java.util.Locale;

public class SampleOfPhotoPickerActivity
    extends AppCompatActivity
    implements ObservableArrayList.Provider<IPhoto>,
               ObservableArrayList.ListChangeListener<IPhoto> {

    public static final String PARAMS_MAX_NUM_OF_SELECTION =
        SampleOfPhotoPickerActivity.class.getCanonicalName() + ".PARAMS_MAX_NUM_OF_SELECTION";
    public static final int DEFAULT_MAX_NUM_OF_SELECTION = 15;

    public static final String RESULT_PHOTOS =
        SampleOfPhotoPickerActivity.class.getCanonicalName() + ".RESULT_PHOTOS";

    // View.
    Toolbar mToolbarView;
    PhotoPickerView mPhotoPicker;
    TextView mSelectionNumView;
    MenuItem mTakePhotoButtonView;
    MenuItem mDoneButtonView;
    MenuItem mSkipButtonView;

    // State.
    ObservableArrayList<IPhoto> mSelectedPhotos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sample_of_photo_picker);

        // Toolbar.
        mToolbarView = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbarView);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // The photo selection pool.
        mSelectedPhotos = new ObservableArrayList<>(Looper.getMainLooper());
        mSelectedPhotos.addListener(this);
        mSelectedPhotos.setMaxCapacity(
            getIntent().getIntExtra(PARAMS_MAX_NUM_OF_SELECTION,
                                    DEFAULT_MAX_NUM_OF_SELECTION));

        // Photo picker.
        mPhotoPicker = (PhotoPickerView) findViewById(R.id.photo_picker);
        // Initialize the selection pool.
        mPhotoPicker.setSelection(this);
        // Load the default album and photos.
        mPhotoPicker.loadDefaultAlbumAndPhotos();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.menu_photo_picker, menu);

        mTakePhotoButtonView = menu.findItem(R.id.item_take_photo);
        mTakePhotoButtonView.setOnMenuItemClickListener(onClickToTakePhoto());
        mDoneButtonView = menu.findItem(R.id.item_done);
        mDoneButtonView.setVisible(false);
        mSkipButtonView = menu.findItem(R.id.item_skip);
        mSkipButtonView.setVisible(true);
        mSkipButtonView.setOnMenuItemClickListener(onClickToSkip());

        final View layout = MenuItemCompat.getActionView(mDoneButtonView);
        layout.setOnClickListener(onClickToAddPhoto());
        mSelectionNumView = (TextView) layout.findViewById(R.id.checked_number);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();

        if (i == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemAdded(List<IPhoto> list,
                            IPhoto item) {
        mDoneButtonView.setVisible(true);
        mSkipButtonView.setVisible(false);
        mSelectionNumView.setText(
            String.format(Locale.ENGLISH, "%d", list.size()));
    }

    @Override
    public void onItemRemoved(List<IPhoto> list,
                              IPhoto item) {
        if (list.isEmpty()) {
            mDoneButtonView.setVisible(false);
            mSkipButtonView.setVisible(true);
        }
    }

    @Override
    public void onItemChanged(List<IPhoto> list,
                              IPhoto item) {
        // DO NOTHING.
    }

    @Override
    public ObservableArrayList<IPhoto> getObservableList() {
        return mSelectedPhotos;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    private MenuItem.OnMenuItemClickListener onClickToTakePhoto() {
        return new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                startActivity(new Intent(
                    SampleOfPhotoPickerActivity.this, TakePhotoDelegateActivity.class));
                return true;
            }
        };
    }

    private MenuItem.OnMenuItemClickListener onClickToSkip() {
        return new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                setResult(RESULT_CANCELED);
                finish();
                return true;
            }
        };
    }

    private View.OnClickListener onClickToAddPhoto() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK, new Intent()
                    .putExtra(RESULT_PHOTOS, mSelectedPhotos.toArray()));
                finish();
            }
        };
    }
}
