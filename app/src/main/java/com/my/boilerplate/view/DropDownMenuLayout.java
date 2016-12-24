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

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Matrix;
import android.os.Build;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper.Callback;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import com.my.boilerplate.R;
import com.my.boilerplate.util.ScrollViewUtil;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * The layout supporting a drop-down child view with drawer liked fling gesture.
 */
public class DropDownMenuLayout extends ViewGroup {

    private static final boolean SET_DRAWER_SHADOW_FROM_ELEVATION = Build.VERSION.SDK_INT >= 21;

    /**
     * Minimum drawer margin in DP.
     */
    private static final int MIN_DRAWER_MARGIN = 64;

    /**
     * Drawer elevation in DP.
     */
    private static final int DRAWER_ELEVATION = 10;

    private float mDrawerElevation;
    /**
     * Distance to travel before a drag may begin.
     */
    private int mTouchSlop;

    private static final int STATE_DRAWER_CLOSED = 0x0;
    private static final int STATE_DRAWER_OPENED = 0x1;
    private static final int STATE_DRAWER_OPENING = 0x2;
    private static final int STATE_DRAWER_CLOSING = 0x3;

    private int mDrawerState = STATE_DRAWER_CLOSED;

    private float mTouchInitY;
    private float mTouchCurrentY;

    private Matrix mChildInverseMatrix;
    private float[] mChildTouchPoint;

    /**
     * The view with isDrawer=true LayoutParams.
     * <br/>
     * See {@link LayoutParams#isDrawer}.
     */
    private View mDrawerView;
    /**
     * The view is being dragging.
     */
    private View mTouchingChild;

//    private ViewDragHelper mDragHelper;

    public DropDownMenuLayout(Context context) {
        this(context, null);
    }

    public DropDownMenuLayout(Context context,
                              AttributeSet attrs) {
        super(context, attrs);

        final float density = getResources().getDisplayMetrics().density;
        final ViewConfiguration vc = ViewConfiguration.get(context);

        mDrawerElevation = DRAWER_ELEVATION * density;
        mTouchSlop = vc.getScaledTouchSlop();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        mChildTouchPoint = new float[2];
        mChildInverseMatrix = new Matrix();

//        mDragHelper = ViewDragHelper.create(this, 1.f, onCreateDragHelperCallback());
//        mDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_TOP);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        mChildTouchPoint = null;
        mChildInverseMatrix = null;
        mDrawerView = null;
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec,
                             int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode != MeasureSpec.EXACTLY || heightMode != MeasureSpec.EXACTLY) {
            throw new IllegalArgumentException(
                "DropDownMenuLayout must be measured with MeasureSpec.EXACTLY.");
        }

        setMeasuredDimension(widthSize, heightSize);

        final int childCount = getChildCount();
        if (childCount != 2) {
            throw new IllegalStateException(
                "DropDownMenuLayout should always have two children views " +
                "where one is a drawer view with \"isDrawerView=true\" " +
                "LayoutParams and the other is a content view.");
        }

        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);

            if (child.getVisibility() == GONE) {
                continue;
            }

            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            if (isDrawerView(child)) {
                mDrawerView = child;

                // TODO: Figure out why setting the view elevation is handled
                // TODO: in the onMeasure function.
                if (SET_DRAWER_SHADOW_FROM_ELEVATION) {
                    if (ViewCompat.getElevation(child) != mDrawerElevation) {
                        ViewCompat.setElevation(child, mDrawerElevation);
                    }
                }

//                final int drawerWidthSpec = getChildMeasureSpec(
//                    widthMeasureSpec,
//                    lp.leftMargin + lp.rightMargin,
//                    lp.width);
                final int drawerWidthSpec = MeasureSpec.makeMeasureSpec(
                    widthSize - lp.leftMargin - lp.rightMargin,
                    MeasureSpec.EXACTLY);
                final int drawerHeightSpec = getChildMeasureSpec(
                    heightMeasureSpec,
                    lp.topMargin + lp.bottomMargin,
                    lp.height);

                child.measure(drawerWidthSpec, drawerHeightSpec);
            } else {
                // Content views get measured at exactly the layout's size.
                final int contentWidthSpec = MeasureSpec.makeMeasureSpec(
                    widthSize - lp.leftMargin - lp.rightMargin,
                    MeasureSpec.EXACTLY);
                final int contentHeightSpec = MeasureSpec.makeMeasureSpec(
                    heightSize - lp.topMargin - lp.bottomMargin,
                    MeasureSpec.EXACTLY);
//                final int contentWidthSpec = getChildMeasureSpec(
//                    widthMeasureSpec,
//                    lp.leftMargin + lp.rightMargin,
//                    lp.width);
//                final int contentHeightSpec = getChildMeasureSpec(
//                    heightMeasureSpec,
//                    lp.topMargin + lp.bottomMargin,
//                    lp.height);

                child.measure(contentWidthSpec, contentHeightSpec);
            }
//            else {
//                throw new IllegalStateException(
//                    "Child " + child + " at " + i + "is not either a drawer " +
//                    "view nor a ScrollingView view.");
//            }
        }
    }

    @Override
    protected void onLayout(boolean changed,
                            int left,
                            int top,
                            int right,
                            int bottom) {
        final int childCount = getChildCount();

        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);

            if (child.getVisibility() == GONE) {
                continue;
            }

            final int childWidth = child.getMeasuredWidth();
            final int childHeight = child.getMeasuredHeight();
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            if (isDrawerView(child)) {
                child.layout(lp.leftMargin,
                             -childHeight,
                             lp.rightMargin + childWidth,
                             0);
            } else {
                child.layout(lp.leftMargin,
                             lp.topMargin,
                             lp.leftMargin + childWidth,
                             lp.topMargin + childHeight);
            }
        }
    }

    /**
     * It is responsible for detecting whether the touching view is being over
     * scrolled. If so, it will coordinate the positions of the child views.
     * If not, do it the same way as {@link ViewGroup}.
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (mDrawerView == null) {
            return super.dispatchTouchEvent(event);
        }

        final int action = MotionEventCompat.getActionMasked(event);

        // The layout would be interested in that the touched list view is
        // scrolling out.
        if (action == MotionEvent.ACTION_DOWN) {
            mTouchInitY = event.getY();
            mTouchCurrentY = event.getY();
            Log.d("xyz", "dispatchTouchEvent(ACTION_DOWN)");
        } else if (action == MotionEvent.ACTION_MOVE) {
            final float dy = event.getY() - mTouchCurrentY;
            final float offsetY = event.getY() - mTouchInitY;
            final boolean ifCross = ifCrossDragSlop(offsetY);
            Log.d("xyz", String.format("dispatchTouchEvent(ACTION_MOVE), ifCross=%s", ifCross));

            if (ifCross) {
                final View touchingChild = findTopChildUnder(event.getX(), event.getY());

                if (mDrawerState == STATE_DRAWER_CLOSED) {
                    if (ScrollViewUtil.ifOverScrollingVertically(touchingChild, dy)) {
                        Log.d("xyz", String.format("dispatchTouchEvent(ACTION_MOVE), over scrolling, dy=%f", dy));
                        mDrawerState = STATE_DRAWER_OPENING;

                        // FIXME: Because the layout will handle the dragging later,
                        // FIXME: dispatch ACTION_CANCEL to all the child views.
                        cancelTouchingChildren(event);

                        // We cheat the event so that mTouchInitY will be updated
                        // for calculating the translationY correctly.
                        event.setAction(MotionEvent.ACTION_DOWN);
                    }
                } else if (mDrawerState == STATE_DRAWER_OPENED) {

                }
            }

            mTouchCurrentY = event.getY();
        } else if (action == MotionEvent.ACTION_UP ||
                   action == MotionEvent.ACTION_CANCEL) {
            Log.d("xyz", "dispatchTouchEvent(ACTION_UP|ACTION_CANCEL)");
            mTouchInitY = 0;
            mTouchCurrentY = 0;
        }

        if (mDrawerState == STATE_DRAWER_OPENING ||
            mDrawerState == STATE_DRAWER_CLOSING) {
            return onTouchEvent(event);
        } else {
            return super.dispatchTouchEvent(event);
        }
    }

//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent event) {
//        final int action = MotionEventCompat.getActionMasked(event);
//        switch (action) {
//            case MotionEvent.ACTION_DOWN:
//                break;
//            case MotionEvent.ACTION_MOVE:
//                break;
//            case MotionEvent.ACTION_UP:
//            case MotionEvent.ACTION_CANCEL:
//                break;
//        }
//
//        return mTouchingChild != null &&
//               mDrawerState == STATE_DRAWER_OPENING ||
//               mDrawerState == STATE_DRAWER_CLOSING;
//    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = MotionEventCompat.getActionMasked(event);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.d("xyz", "onTouchEvent(ACTION_DOWN)");
                mTouchingChild = findTopChildUnder(event.getX(), event.getY());
                mTouchInitY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d("xyz", "onTouchEvent(ACTION_MOVE)");
                if (mDrawerState == STATE_DRAWER_OPENING) {
                    final float ty = Math.min(Math.max(0, event.getY() - mTouchInitY),
                                              mDrawerView.getHeight());
                    ViewCompat.setTranslationY(mDrawerView, ty);
                    ViewCompat.setTranslationY(mTouchingChild, ty);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                Log.d("xyz", "onTouchEvent(ACTION_UP)");

                ViewCompat.setTranslationY(mDrawerView, 0);
                ViewCompat.setTranslationY(mTouchingChild, 0);
                // TODO: Smooth to open or close the drawer and udpate the correct
                // TODO: state.
                mDrawerState = STATE_DRAWER_CLOSED;

                mTouchingChild = null;
                break;
        }

        // Note: Be careful of using this layout in a nested view hierarchy.
        // Because it always return true, the parent's onTouchEvent will never
        // be called.
        return true;
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        super.requestDisallowInterceptTouchEvent(disallowIntercept);

        // Ignore the disabling and pass to its parent.
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(MATCH_PARENT, MATCH_PARENT);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

//    private Callback onCreateDragHelperCallback() {
//        return new Callback() {
//            @Override
//            public boolean tryCaptureView(View child,
//                                          int pointerId) {
//                Log.d("xyz", String.format("tryCaptureView(%s)", child));
//                // Drawer view must be present.
//                if (mDrawerView == null) return false;
//
//                return false;
//            }
//
//            @Override
//            public int clampViewPositionVertical(View child,
//                                                 int top,
//                                                 int dy) {
//                if (mDragHelper.getCapturedView() == mDrawerView) {
//                    // Dragging the drawer.
//                    return 0;
//                } else {
//                    // Dragging the content.
//                    if (mDrawerState == STATE_DRAWER_OPENING) {
//                        int topBound = getPaddingTop();
//                        int bottomBound = getHeight() - getPaddingBottom();
//                        int drawerHeight = mDrawerView.getHeight();
//
//                        return Math.min(Math.max(topBound, top), bottomBound);
//                    } else {
//                        return 0;
//                    }
//                }
//            }
//
//            @Override
//            public int getViewVerticalDragRange(View child) {
//                return getWidth();
//            }
//
//            @Override
//            public void onViewDragStateChanged(int state) {
//                super.onViewDragStateChanged(state);
//            }
//        };
//    }

    private boolean isDrawerView(View child) {
        LayoutParams params = (LayoutParams) child.getLayoutParams();
        return params.isDrawer;
    }

    private boolean ifCrossDragSlop(float dy) {
        return Math.abs(dy) > mTouchSlop;
    }

    /**
     * Find the topmost child under the given point within the parent view's
     * coordinate system. The child order is determined using
     * {@link Callback#getOrderedChildIndex(int)}.
     *
     * @param y Y position to test in the parent's coordinate system
     * @return The topmost child view under (x, y) or null if none found.
     */
    private View findTopChildUnder(float x, float y) {
        View foundChild = null;
        final int childCount = getChildCount();

        for (int i = childCount - 1; i >= 0; i--) {
            final View child = getChildAt(i);

            // Transform the touch point to the child's coordinate system.
            mChildTouchPoint[0] = x;
            mChildTouchPoint[1] = y;
            child.getMatrix().invert(mChildInverseMatrix);
            mChildInverseMatrix.mapPoints(mChildTouchPoint);

            final float childX = mChildTouchPoint[0];
            final float childY = mChildTouchPoint[1];
            if (childX >= child.getLeft() && childX <= child.getRight() &&
                childY >= child.getTop() && childY < child.getBottom()) {
                foundChild = child;
                break;
            }
        }

        return foundChild;
    }

    private void cancelTouchingChildren(MotionEvent event) {
        final MotionEvent canceledEvent = MotionEvent.obtain(event);

        canceledEvent.setAction(MotionEvent.ACTION_CANCEL);
        super.dispatchTouchEvent(canceledEvent);
        canceledEvent.recycle();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {

        private static final int FLAG_IS_CLOSED = 0x0;

        /**
         * The state of the drawer view.
         */
        private int openState;
        /**
         * Child view with "isDrawerView=true" attribute would be a drawer view.
         * <pre>
         *     <DropDownMenuLayout ...>
         *         <FrameLayout ...
         *             app:isDrawerView="true"/>
         *     <DropDownMenuLayout/>
         * </pre>
         */
        private boolean isDrawer;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);

            final TypedArray array = c.obtainStyledAttributes(attrs, R.styleable.DropDownMenuLayout);
            try {
                openState = FLAG_IS_CLOSED;
                isDrawer = array.getBoolean(R.styleable.DropDownMenuLayout_isDrawerView, false);
            } finally {
                array.recycle();
            }
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }
    }
}
