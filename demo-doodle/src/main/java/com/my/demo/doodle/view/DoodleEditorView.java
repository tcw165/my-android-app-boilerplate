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
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.my.demo.doodle.R;
import com.my.demo.doodle.data.DefaultDoodleBrush;
import com.my.demo.doodle.protocol.ISketchStroke;

import java.util.ArrayList;
import java.util.List;

public class DoodleEditorView extends AppCompatImageView {

    // State
    final List<ISketchStroke> mStrokes = new ArrayList<>();
    int mStrokeColor;
    float mStrokeWidth;

    public DoodleEditorView(Context context) {
        this(context, null);
    }

    public DoodleEditorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DoodleEditorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mStrokeColor = ContextCompat.getColor(context, R.color.accent);
        mStrokeWidth = context.getResources().getDimension(R.dimen.doodle_default_stroke_width);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = MotionEventCompat.getActionMasked(event);

        // Show how often this callback is called.
        Log.d("xyz", "time=" + SystemClock.currentThreadTimeMillis() + "ms");

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                final ISketchStroke stroke = new DefaultDoodleBrush(
                    getResources().getDimension(R.dimen.doodle_default_path_segment_length),
                    3);

                mStrokes.add(stroke);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                final ISketchStroke stroke = mStrokes.get(mStrokes.size() - 1);
                final float x = event.getX();
                final float y = event.getY();

                stroke.savePathTuple(x, y, mStrokeWidth, mStrokeColor);
                Log.d("xyz", "(x=" + x + ", y=" + y + ")" +
                             ", stroke.getAllPathTuples()=" +
                             stroke.getAllPathTuples().size());

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
}
