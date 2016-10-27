package com.my.boilerplate.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import rx.subscriptions.CompositeSubscription;

public class CollageLayout extends ScrapView {

    public static final float DEFAULT_CHILD_WIDTH_PERCENT = 1.f / 3.f;

    protected DragGestureDetector mDragDetector;

    protected CompositeSubscription mSubscriptions;

    // FIXME: Remove following debug codes.
    protected Paint mDebugPaint;
    protected Path mDebugPath;

    public CollageLayout(Context context) {
        this(context, null);
    }

    public CollageLayout(Context context,
                         AttributeSet attrs) {
        super(context, attrs);

        // TODO: Init the detector acoording to the attributes.
        mDragDetector = new DragGestureDetector();

        setOnHierarchyChangeListener(onHierarchyChange());
    }

    // FIXME: Remove following debug codes.
//    @Override
//    protected void onDraw(Canvas canvas) {
//        TransformInfo info = mTransformInfo;
//
//        if (info.deltaPos.x != 0 || info.deltaPos.y != 0) {
//            float[] deltaVec = {info.deltaPos.x,
//                                info.deltaPos.y};
//            mDebugPath.reset();
//            mDebugPath.moveTo(info.prevPos.x, info.prevPos.y);
//            mDebugPath.lineTo(info.prevPos.x + info.deltaPos.x,
//                         info.prevPos.y + info.deltaPos.y);
//            canvas.drawPath(mDebugPath, mDebugPaint);
//        }
//    }

//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        return super.onInterceptTouchEvent(ev);
//    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        return super.onTouchEvent(event);
//    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    @Override
    protected void onMeasure(int widthMeasureSpec,
                             int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // We currently don't care about the mode.
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        widthSize = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
        heightSize = widthSize;

        // Support square canvas at the moment.
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    protected AtomicReference<View> mTouchingView = new AtomicReference<>();
    protected Matrix mTransformMatrix = new Matrix();

    protected OnTouchListener mTouchDispatcher = new OnTouchListener() {
        @Override
        public boolean onTouch(View v,
                               MotionEvent event) {
            if (!(v instanceof ScrapView)) return false;

            boolean isHandled = false;
            int action = MotionEventCompat.getActionMasked(event);

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mTouchingView.set(v);

                    mDragDetector.startSession(v, event);
                    break;
                case MotionEvent.ACTION_UP:
                    mTouchingView.set(null);

                    mDragDetector.stopSession(v, event);
                    break;
                default:
                    if (v.equals(mTouchingView.get())) {
//                        mDragDetector.getGestureMatrix();
                    }
            }

            // TODO: Apply the matrix to the view.

            return isHandled;
        }
    };

    protected OnHierarchyChangeListener onHierarchyChange() {
        return new OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                child.setOnTouchListener(mTouchDispatcher);
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {
                child.setOnTouchListener(null);
            }
        };
    }

//    private class LayersHierarchyChange implements OnHierarchyChangeListener {
//
//        @Override
//        public void onChildViewAdded(View parent, View child) {
//            mIsDrawingCacheDirty = true;
//
//            if (child instanceof MosaicView) {
//                // Update the filters flag so that this container will update the
//                // cached effects.
//                mFiltersFlag |= MOSAIC_FILTER;
//
//                // Make children subscribe to the effect.
//                ((MosaicView) child).subscribeToMosaic(mMosaicSubject);
//
//                invalidFilters();
//            }
//        }
//
//        @Override
//        public void onChildViewRemoved(View parent, View child) {
//
//        }
//    }
}
