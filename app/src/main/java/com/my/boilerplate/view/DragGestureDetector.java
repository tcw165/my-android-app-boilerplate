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

import android.graphics.Matrix;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;

public class DragGestureDetector extends MyGestureDetector {

    private static final String TAG = DragGestureDetector.class.getSimpleName();

    protected PointF mStartPt;
    protected final Matrix mTransformMatrix;

    public DragGestureDetector() {
        mTransformMatrix = new Matrix();
    }

    @Override
    public boolean canHandle(View v,
                             int action,
                             MotionEvent viewEvent,
                             MotionEvent rootEvent) {
        return false;
    }

    @Override
    public void startSession(View v,
                             MotionEvent viewEvent,
                             MotionEvent rootEvent) {
        mStartPt = new PointF(rootEvent.getX(0),
                              rootEvent.getY(0));
    }

    @Override
    public void stopSession() {
        mStartPt = null;
    }

    @Override
    public Matrix getTransformMatrix(View v,
                                     MotionEvent viewEvent,
                                     MotionEvent rootEvent) {
        if (mStartPt == null) return null;

        float dx = rootEvent.getX(0) - mStartPt.x;
        float dy = rootEvent.getY(0) - mStartPt.y;

        mTransformMatrix.reset();
        mTransformMatrix.setTranslate(dx, dy);

        return mTransformMatrix;
    }
}
