package com.my.boilerplate.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

public class ScrapView extends FrameLayout {

    public static final float ASPECT_RATIO_SQUARE = 1.f;
    public static final float ASPECT_RATIO_GOLD_RECT = 4.f / 3.f;

    protected float mAspectRatio;

    public ScrapView(Context context) {
        this(context, null);
    }

    public ScrapView(Context context,
                     AttributeSet attrs) {
        super(context, attrs);

        mAspectRatio = 1.f;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // We currently don't care about the mode.
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        widthSize = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
        heightSize = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);

        // Support square canvas at the moment.
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Log.d("xyz", String.format("current matrix=%s", getMatrix()));
    }

    public void setAspectRatio(float ratio) {
        mAspectRatio = ratio;

        invalidate();
        requestLayout();
    }
}
