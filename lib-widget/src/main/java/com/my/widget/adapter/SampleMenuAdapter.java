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

package com.my.widget.adapter;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.my.widget.R;

import java.lang.ref.WeakReference;

public class SampleMenuAdapter extends ArrayAdapter<SampleMenuAdapter.SampleMenuItem> {

    private final LayoutInflater mInflater;

    /**
     * A common adapter for displaying the sample menu.
     *
     * @param context Usually is an Activity, so that the it could get the
     *                resource with correct theme.
     * @param items   Array containing pairs of title and caption.
     */
    public SampleMenuAdapter(Context context,
                             SampleMenuItem[] items) {
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

        final SampleMenuItem item = getItem(position);
        if (viewHolder.caption.get() != null && item != null) {
            viewHolder.caption.get().setText(item.title);
            viewHolder.description.get().setText(item.description);
        }

        return convertView;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    public static class SampleMenuItem {

        final public String title;
        final public String description;
        final public View.OnClickListener onClickListener;

        public SampleMenuItem(String title,
                              String description,
                              View.OnClickListener onClickListener) {
            this.title = title;
            this.description = description;
            this.onClickListener = onClickListener;
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
}
