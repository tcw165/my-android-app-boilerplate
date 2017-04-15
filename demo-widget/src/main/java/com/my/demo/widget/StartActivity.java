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

package com.my.demo.widget;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.my.core.protocol.IProgressBarView;
import com.my.core.util.ViewUtil;

import java.lang.ref.WeakReference;

public class StartActivity
    extends AppCompatActivity
    implements IProgressBarView {

    Toolbar mToolbar;
    ListView mStartMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_start);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }

        // List menu.
        mStartMenu = (ListView) findViewById(R.id.menu);
        mStartMenu.setAdapter(onMenuCreate());
        mStartMenu.setOnItemClickListener(onClickMenuItem());
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

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    @SuppressWarnings({"unchecked"})
    protected SampleMenuAdapter onMenuCreate() {
        return new SampleMenuAdapter(
            this,
            new MenuItem[]{
                new MenuItem(
                    "CameraTextureView",
                    "Use TextureView or SurfaceView to provide the " +
                    "camera feature.",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(
                                new Intent(StartActivity.this,
                                           SampleOfCameraActivity.class));
                        }
                    }),
                new MenuItem(
                    "DropDownMenuLayout",
                    "The ViewGroup is responsible for intercepting the " +
                    "touch event.",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(
                                new Intent(StartActivity.this,
                                           SampleOfDropDownMenuLayoutActivity.class));
                        }
                    }),
                new MenuItem(
                    "ElasticDragLayout",
                    "Inheriting from CoordinatorLayout and support elastic " +
                    "drag UX like iOS's scroll-view.",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(
                                new Intent(StartActivity.this,
                                           SampleOfElasticDragLayoutActivity.class));
                        }
                    }),
                new MenuItem(
                    "ElasticDragDismissLayout (ElasticDragLayout)",
                    "Idea inspired from the sample code of Plaid app. It " +
                    "inherits from the CoordinatorLayout and is using a " +
                    "translucent Activity to implement the drag-to-dismiss " +
                    "gesture.",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                            startActivity(
//                                new Intent(StartActivity.this,
//                                           SampleOfElasticDragDismissLayoutActivity.class),
//                                // Necessary option to enable the scene transition.
//                                ActivityOptionsCompat
//                                    .makeSceneTransitionAnimation(
//                                        getActivity(),
//                                        (android.support.v4.util.Pair<View, String>[]) null)
//                                    .toBundle());
                            startActivity(
                                new Intent(StartActivity.this,
                                           SampleOfElasticDragDismissLayoutActivity.class));
                        }
                    }),
                new MenuItem(
                    "ElasticDragMenuLayout (ElasticDragLayout)",
                    "A child class inheriting from ElasticDragDismissLayout. " +
                    "The layout allows a NestedScrollingChild child view " +
                    "being over dragged.",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(
                                new Intent(StartActivity.this,
                                           SampleOfElasticDragMenuLayoutActivity.class));
                        }
                    }),
                new MenuItem(
                    "PhotoPickerView",
                    "A google-photo like picker.",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivityForResult(
                                new Intent(StartActivity.this,
                                           SampleOfPhotoPickerActivity.class), 0);
                        }
                    })
            });
    }

    protected AdapterView.OnItemClickListener onClickMenuItem() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent,
                                    View view,
                                    int position,
                                    long id) {
                final MenuItem item = (MenuItem) parent.getAdapter()
                                                       .getItem(position);
                item.onClickListener.onClick(view);
            }
        };
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    private static class SampleMenuAdapter extends ArrayAdapter<MenuItem> {

        private final LayoutInflater mInflater;

        /**
         * A common adapter for displaying the sample menu.
         *
         * @param context Usually is an Activity, so that the it could get the
         *                resource with correct theme.
         * @param items   Array containing pairs of title and caption.
         */
        SampleMenuAdapter(Context context,
                          MenuItem[] items) {
            super(context, 0, items);

            mInflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public View getView(int position,
                            View convertView,
                            @NonNull ViewGroup parent) {
            ViewHolder viewHolder;

            // Check if an existing view is being reused, otherwise inflate the view.
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.sample_card_menu_item, parent, false);
                viewHolder = new ViewHolder(convertView,
                                            R.id.caption,
                                            R.id.description);

                // View lookup cache stored in tag.
                convertView.setTag(viewHolder);
            } else {
                // View is being recycled, retrieve the viewHolder object from tag.
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final MenuItem item = getItem(position);
            if (viewHolder.caption.get() != null && item != null) {
                viewHolder.caption.get().setText(item.title);
                viewHolder.description.get().setText(item.description);
            }

            return convertView;
        }
    }

    private static class ViewHolder {
        WeakReference<TextView> caption;
        WeakReference<TextView> description;

        ViewHolder(View view,
                   @IdRes int captionRes,
                   @IdRes int descriptionRes) {
            this.caption = new WeakReference<>((TextView) view.findViewById(captionRes));
            this.description = new WeakReference<>((TextView) view.findViewById(descriptionRes));
        }
    }

    private static class MenuItem {

        final String title;
        final String description;
        final View.OnClickListener onClickListener;

        public MenuItem(String title,
                        String description,
                        View.OnClickListener onClickListener) {
            this.title = title;
            this.description = description;
            this.onClickListener = onClickListener;
        }
    }
}
