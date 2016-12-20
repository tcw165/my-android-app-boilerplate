package com.my.boilerplate.view;

import android.graphics.Matrix;
import android.view.MotionEvent;
import android.view.View;

public class PinchGestureDetector extends MyGestureDetector {

    @Override
    public boolean canHandle(View v, int action, MotionEvent viewEvent, MotionEvent rootEvent) {
        return false;
    }

    @Override
    public void startSession(View v, MotionEvent viewEvent, MotionEvent rootEvent) {

    }

    @Override
    public void stopSession() {

    }

    @Override
    public Matrix getTransformMatrix(View v, MotionEvent viewEvent, MotionEvent rootEvent) {
        return null;
    }
}
