package com.my.boilerplate.view;

import android.graphics.Matrix;
import android.view.MotionEvent;
import android.view.View;

public abstract class MyGestureDetector {
    public abstract void startSession(View v,
                                      MotionEvent viewEvent,
                                      MotionEvent rootEvent);
    public abstract void stopSession();
    public abstract Matrix getTransformMatrix(View v,
                                              MotionEvent viewEvent,
                                              MotionEvent rootEvent);
}
