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

package com.my.demo.doodle.view;

import android.content.Context;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.my.demo.doodle.R;
import com.my.demo.doodle.protocol.ISketchEditorView;
import com.my.demo.doodle.protocol.ISketchBrush;
import com.my.demo.doodle.protocol.ISketchStroke;

import java.util.ArrayList;
import java.util.List;

public class SketchEditorView
    extends AppCompatImageView
    implements ISketchEditorView {

    // Config.
    private float mMinPathSegmentLength;
    private long mMinPathSegmentDuration;

    // State
    private long mPrevAddTime;
    private ISketchBrush mBrush;
    final List<ISketchStroke> mStrokes = new ArrayList<>();
//    int mStrokeColor;
//    float mStrokeWidth;

    public SketchEditorView(Context context) {
        this(context, null);
    }

    public SketchEditorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SketchEditorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mMinPathSegmentLength = context.getResources().getDimension(
            R.dimen.doodle_default_path_segment_length);
        mMinPathSegmentDuration = 2L;

//        mStrokeColor = ContextCompat.getColor(context, R.color.accent);
//        mStrokeWidth = context.getResources().getDimension(R.dimen.doodle_default_stroke_width);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Handle the touch event in a default way if no brush is present.
        if (mBrush == null) {
            return super.onTouchEvent(event);
        }

        final int action = MotionEventCompat.getActionMasked(event);

        // Show how often this callback is called.
        Log.d("xyz", "time=" + SystemClock.currentThreadTimeMillis() + "ms");

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                // TODO: Make it confi
//                final ISketchStroke stroke = new PenSketchStroke(
//                    getResources().getDimension(R.dimen.doodle_default_path_segment_length),
//                    3);

                mStrokes.add(mBrush.newStroke());
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                final ISketchStroke stroke = mStrokes.get(mStrokes.size() - 1);
                final float x = event.getX();
                final float y = event.getY();

                if (canAdd(stroke, x, y)) {
                    stroke.savePathTuple(x, y);
                }

                postInvalidate();
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                break;
            }
        }

//        return super.onTouchEvent(event);
        return true;
    }

    @Override
    public void setBrush(ISketchBrush brush) {
        if (brush == null) {
            throw new IllegalArgumentException("null brush.");
        }

        mBrush = brush;
    }

    @Override
    public ISketchBrush getBrush() {
        return mBrush;
    }

//    @Override
//    public void setWidth(float width) {
//
//    }
//
//    @Override
//    public Observable<Float> getWidth() {
//        return null;
//    }
//
//    @Override
//    public void setColor(int color) {
//
//    }
//
//    @Override
//    public Observable<Integer> getStrokeColor() {
//        return null;
//    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Render path strokes
        for (ISketchStroke stroke : mStrokes) {
            stroke.draw(canvas);
        }
    }

    private boolean canAdd(final ISketchStroke stroke,
                           final float x,
                           final float y) {
        if (mBrush == null) return false;
        if (stroke.size() == 0) return true;

        final long duration = SystemClock.currentThreadTimeMillis() - mPrevAddTime;
        final ISketchStroke.PathTuple tuple = stroke.getLastPathTuple();
        final ISketchStroke.Anchor anchor = tuple.getAnchorAt(0);

        Log.d("xyz", "duration=" + duration + ", " +
                     "distance=" + Math.hypot(x - anchor.getX(), y - anchor.getY()));
        if (duration > mMinPathSegmentDuration &&
            Math.hypot(x - anchor.getX(), y - anchor.getY()) > mMinPathSegmentLength) {

            mPrevAddTime = SystemClock.currentThreadTimeMillis();

            return true;
        } else {
            return false;
        }
    }
}
