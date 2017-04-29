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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;

import com.my.jni.dlib.data.Face;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class FaceLandmarksImageView extends AppCompatImageView {

    private static final float WIDTH = 1f;
    private final int mWidth;
    private final Paint mPaint;

    private final List<Face> mNormalizedFaces = new CopyOnWriteArrayList<>();
    private final List<Face> mDenormalizedFaces = new CopyOnWriteArrayList<>();

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

        final float density = getContext()
            .getResources().getDisplayMetrics().density;

        mWidth = (int) (density * WIDTH);
        mPaint = new Paint();
        mPaint.setColor(ContextCompat.getColor(getContext(), com.my.widget.R.color.red));
        mPaint.setStyle(Paint.Style.FILL);
    }

    public void setFaces(List<Face> faces) {
        if (getDrawable() == null) {
            throw new IllegalStateException("The drawable is null");
        }
        Log.d("xyz", "drawable bound=" + getDrawable().getBounds());
        Log.d("xyz", "getImageMatrix()=" + getImageMatrix());

        final Rect bound = getDrawable().getBounds();
        mNormalizedFaces.clear();
        mNormalizedFaces.addAll(faces);

        // Give the normalized landmarks real dimension.
        mDenormalizedFaces.clear();
        for (int i = 0; i < mNormalizedFaces.size(); ++i) {
            // The normalized face.
            final Face nFace = mNormalizedFaces.get(i);
            // The denormalized face.
            final Face dFace = new Face(nFace, bound.width(), bound.height());

            mDenormalizedFaces.add(dFace);
        }
        invalidate();
    }

    @Override
    public void onDrawForeground(Canvas canvas) {
        super.onDrawForeground(canvas);

        if (getDrawable() != null && !mDenormalizedFaces.isEmpty()) {
            // Render faces.
            canvas.save();
            canvas.concat(getImageMatrix());
            for (int i = 0; i < mDenormalizedFaces.size(); ++i) {
                final Face face = mDenormalizedFaces.get(i);

                for (int j = 0; j < face.getAllLandmarks().size(); ++j) {
                    final Face.Landmark landmark = face.getAllLandmarks().get(j);

                    canvas.drawRect((int) (landmark.x - mWidth), (int) (landmark.y - mWidth),
                                    (int) (landmark.x + mWidth), (int) (landmark.y + mWidth),
                                    mPaint);
                }
            }
            canvas.restore();
        }
    }
}
