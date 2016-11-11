package com.my.boilerplate.view;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.Log;
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
