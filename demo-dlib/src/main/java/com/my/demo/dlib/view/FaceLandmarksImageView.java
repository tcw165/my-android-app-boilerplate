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

package com.my.demo.dlib.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.bumptech.glide.Glide;
import com.my.jni.dlib.data.Face;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class FaceLandmarksImageView extends AppCompatImageView {

    final List<Face> mFaces = new CopyOnWriteArrayList<>();

    public FaceLandmarksImageView(Context context) {
        this(context, null);
    }

    public FaceLandmarksImageView(Context context,
                                  AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FaceLandmarksImageView(Context context,
                                  AttributeSet attrs,
                                  int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

//    public void setImage(String path) {
//        Glide.with(getContext())
//             .load(path)
//             .dontTransform()
//             .into(this);
//    }

    public void setFaces(List<Face> faces) {
        mFaces.clear();
        mFaces.addAll(faces);

        // Invalidate if the drawable is present.
        final Drawable drawable = getDrawable();
        if (drawable != null &&
            drawable.getIntrinsicHeight() > 0 &&
            drawable.getIntrinsicHeight() > 0) {
            invalidate();
        }
    }
}
