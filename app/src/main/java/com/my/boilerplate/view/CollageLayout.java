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

public class CollageLayout extends ScrapView {

    private static final String TAG = CollageLayout.class.getSimpleName();

    public static final float DEFAULT_CHILD_WIDTH_PERCENT = 1.f / 3.f;

    /**
     * For making one view consume the events at a time.
     */
    protected View mTouchingView;
    /**
     * The starting matrix of a gesture session.
     */
    protected Matrix mStartMatrix = new Matrix();
    /**
     * For passing the event in the root coordinate to the children gesture
     * detectors.
     */
    protected MotionEvent mRootTouchEvent;
    /**
     * Shared matrix values for the matrix calculation in callbacks like
     * {@code onDraw}, {@code onTouch}, ...etc.
     */
    protected float[] mSharedMatrixVals = new float[9];
    /**
     * The shared dragging gesture detector for the children view.
     */
    protected DragGestureDetector mChildrenDragDetector;

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
        mChildrenDragDetector = new DragGestureDetector();

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

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // It's called before calling its children onTouch callbacks. And we
        // need to pass the event in the root coordinate to the gesture detectors
        // later in the onTouch callback.
        mRootTouchEvent = ev;
        return false;
    }

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
        // DO NOTHING.
    }

    /**
     * The gesture dispatcher which is responsible for determining what gesture
     * to use and how to apply the transformation to the view.
     */
    protected OnTouchListener mTouchDispatcher = new OnTouchListener() {
        @Override
        public boolean onTouch(View v,
                               MotionEvent event) {
            boolean isHandled = false;
            int action = MotionEventCompat.getActionMasked(event);
            Matrix transformMatrix = null;

            switch (action) {
                // Only one view can handle the event at a time; the other views
                // won't receive the event during the gesture session.
                case MotionEvent.ACTION_DOWN:
                    if (mTouchingView == null) {
                        mTouchingView = v;

                        // The container saves the starting transformation.
                        mStartMatrix.set(v.getMatrix());
                        // FIXME: The root determine the starting transformation
                        // and pass to the gesture detectors.
                        mChildrenDragDetector.startSession(v, event, mRootTouchEvent);

                        // It's necessary to notify the caller the event is
                        // handled by this view.
                        isHandled = true;
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    mTouchingView = null;
                    mChildrenDragDetector.stopSession();
                    break;
                default:
                    transformMatrix = mChildrenDragDetector.getTransformMatrix(v, event, mRootTouchEvent);
            }

            // TODO: Add snap-to-grid, snap-to-rotation, ... or more.
            // Apply the affine matrix to the view.
            if (transformMatrix != null) {
                mStartMatrix.getValues(mSharedMatrixVals);
                float startTx = mSharedMatrixVals[Matrix.MTRANS_X];
                float startTy = mSharedMatrixVals[Matrix.MTRANS_Y];

                transformMatrix.getValues(mSharedMatrixVals);
                v.setTranslationX(startTx + mSharedMatrixVals[Matrix.MTRANS_X]);
                v.setTranslationY(startTy + mSharedMatrixVals[Matrix.MTRANS_Y]);
                v.postInvalidate();

                isHandled = true;
            }

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
}
