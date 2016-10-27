package com.my.boilerplate.view;

import android.graphics.Matrix;
import android.view.MotionEvent;
import android.view.View;

public abstract class MyGestureDetector {

    public abstract void startSession(View v, MotionEvent event);
    public abstract void stopSession(View v, MotionEvent event);
    public abstract boolean getGestureMatrix(View v, MotionEvent event);
}
