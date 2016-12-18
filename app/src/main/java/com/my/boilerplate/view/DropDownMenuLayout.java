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
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.support.v4.widget.ViewDragHelper.Callback;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.my.boilerplate.R;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class DropDownMenuLayout extends FrameLayout {

    /**
     * Whether the drawer shadow comes from setting elevation on the drawer.
     */
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

    private View mDrawerView;

    private ViewDragHelper mTopDragger;

    public DropDownMenuLayout(Context context) {
        this(context, null);
    }

    public DropDownMenuLayout(Context context,
                              AttributeSet attrs) {
        super(context, attrs);

        final float density = getResources().getDisplayMetrics().density;

        mTopDragger = ViewDragHelper.create(this, mTopCallback);
        mTopDragger.setEdgeTrackingEnabled(ViewDragHelper.EDGE_TOP);

        mDrawerElevation = DRAWER_ELEVATION * density;
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

                final int drawerWidthSpec = getChildMeasureSpec(
                    widthMeasureSpec,
                    lp.leftMargin + lp.rightMargin,
                    lp.width);
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

                child.measure(contentWidthSpec, contentHeightSpec);
            }
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

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        mDrawerView = null;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
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

    protected final Callback mTopCallback = new Callback() {
        @Override
        public boolean tryCaptureView(View child,
                                      int pointerId) {
            return false;
        }
    };

    private boolean isDrawerView(View child) {
        LayoutParams params = (LayoutParams) child.getLayoutParams();
        return params.isDrawer;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    public static class LayoutParams extends FrameLayout.LayoutParams {

        private static final int FLAG_IS_CLOSED = 0x0;
        private static final int FLAG_IS_OPENED = 0x1;
        private static final int FLAG_IS_OPENING = 0x2;
        private static final int FLAG_IS_CLOSING = 0x4;
        /**
         * The state of the drawer view.
         */
        private int openState;
        /**
         * Child view with "isDrawerView=true" would be a drawer view.
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
