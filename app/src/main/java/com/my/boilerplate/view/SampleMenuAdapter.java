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

package com.my.boilerplate.view;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.my.boilerplate.R;

import java.lang.ref.WeakReference;

public class SampleMenuAdapter extends ArrayAdapter<Pair<String, String>> {

    public SampleMenuAdapter(Context context,
                             Pair<String, String>[] items) {
        super(context, 0, items);
    }

    @NonNull
    @Override
    public View getView(int position,
                        View convertView,
                        @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        ViewHolder viewHolder;

        // Check if an existing view is being reused, otherwise inflate the view.
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.sample_card_menu_item, parent, false);
            viewHolder = new ViewHolder(convertView,
                                        R.id.caption,
                                        R.id.description);

            // View lookup cache stored in tag.
            convertView.setTag(viewHolder);
        } else {
            // View is being recycled, retrieve the viewHolder object from tag.
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Pair<String, String> item = getItem(position);
        if (viewHolder.caption.get() != null && item != null) {
            viewHolder.caption.get().setText(item.first);
            viewHolder.description.get().setText(item.second);
        }

        return convertView;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    private static class ViewHolder {
        WeakReference<TextView> caption;
        WeakReference<TextView> description;

        public ViewHolder(View view,
                          @IdRes int captionRes,
                          @IdRes int descriptionRes) {
            this.caption = new WeakReference<>((TextView) view.findViewById(captionRes));
            this.description = new WeakReference<>((TextView) view.findViewById(descriptionRes));
        }
    }
}
