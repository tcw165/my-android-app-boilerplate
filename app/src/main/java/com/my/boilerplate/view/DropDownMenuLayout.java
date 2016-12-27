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

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
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
import android.view.animation.AccelerateDecelerateInterpolator;

import com.my.boilerplate.R;
import com.my.boilerplate.util.ScrollableViewUtil;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * The layout supporting a drop-down child view with drawer liked fling gesture.
 */
public class DropDownMenuLayout extends ViewGroup
    implements INavMenu {

    /**
     * Minimum drawer margin in DP.
     */
    private static final int MIN_DRAWER_MARGIN_IN_DP = 64;

    /**
     * Drawer elevation in DP.
     */
    private static final int DRAWER_ELEVATION_IN_DP = 10;
    private static final boolean SET_DRAWER_SHADOW_FROM_ELEVATION = Build.VERSION.SDK_INT >= 21;
    private float mDrawerElevation;

    private static final int STATE_DRAWER_CLOSED = 0x0;
    private static final int STATE_DRAWER_OPENED = 0x1;
    private static final int STATE_DRAWER_OPENING = 0x2;
    private static final int STATE_DRAWER_CLOSING = 0x3;
    private int mDrawerState = STATE_DRAWER_CLOSED;

    /**
     * Distance to travel before a drag may begin.
     */
    private int mTouchSlop;
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
    private View mContentView;
    /**
     * The view is being dragging.
     */
    private View mTouchingChild;
    /**
     * The black translucent cover.
     */
    private Paint mCoveredFadePaint;
    private Rect mCoveredFadeRect;
    private static final int COVER_FADE_PAINT_ALPHA = (int) (0.75 * 0xFF);
    /**
     * Animator for the drawer animation.
     */
    private AnimatorSet mAnimatorSet;
    private static final float ANIMATION_DURATION_OPEN = 300.f;
    private static final float ANIMATION_DURATION_CLOSE = 250.f;

    private OnMenuStateChange mMenuStateListener;

    public DropDownMenuLayout(Context context) {
        this(context, null);
    }

    public DropDownMenuLayout(Context context,
                              AttributeSet attrs) {
        super(context, attrs);

        final float density = getResources().getDisplayMetrics().density;
        final ViewConfiguration vc = ViewConfiguration.get(context);

        mDrawerElevation = DRAWER_ELEVATION_IN_DP * density;
        mTouchSlop = vc.getScaledTouchSlop();
        mCoveredFadePaint = new Paint();
        mCoveredFadePaint.setColor(Color.BLACK);
        mCoveredFadeRect = new Rect();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        mChildTouchPoint = new float[2];
        mChildInverseMatrix = new Matrix();
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
                mContentView = child;

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
//                    "Child " + child + " at " + i + "is neither a drawer " +
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
        mCoveredFadeRect.set(getLeft(),
                             getTop(),
                             getRight(),
                             getBottom());

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
     * Responsible for whether to intercept the touch event. If so, call
     * {@link DropDownMenuLayout#onTouchEvent(MotionEvent)}.
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (mDrawerView == null) {
            return super.dispatchTouchEvent(event);
        }

        // The layout would be interested in that the touched list view is
        // scrolling out.
        final View touchingChild = findTopChildUnder(event.getX(), event.getY());
        final int action = MotionEventCompat.getActionMasked(event);
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mTouchInitY = event.getY();
                mTouchCurrentY = event.getY();
                Log.d("xyz", "dispatchTouchEvent(ACTION_DOWN)");

                if (mDrawerState == STATE_DRAWER_OPENED &&
                    touchingChild != mDrawerView) {
                    // Touching the black translucent overlay.
                    mDrawerState = STATE_DRAWER_CLOSING;
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                Log.d("xyz", "dispatchTouchEvent(ACTION_MOVE)");
                final float dy = event.getY() - mTouchCurrentY;
                final float offsetY = event.getY() - mTouchInitY;
                final boolean isDragging = ifCrossDragSlop(offsetY);
                if (!isDragging) break;

                if (mDrawerState == STATE_DRAWER_OPENED) {
                    // Dragging on the drawer.
                    Log.d("xyz", "dispatchTouchEvent(ACTION_MOVE), closing drawer");
                    mDrawerState = STATE_DRAWER_CLOSING;

                    // Because the layout will handle the dragging later,
                    // dispatch ACTION_CANCEL to all the child views.
                    cancelChildrenTouchEvent(event);
                } else {
                    if (dy > 0 &&
                        ScrollableViewUtil.ifOverScrollingVertically(touchingChild, dy)) {
                        if (mDrawerState == STATE_DRAWER_CLOSED) {
                            // If sliding thumb down till the content view is being
                            // over scrolled, intercept the touch event.
                            mDrawerState = STATE_DRAWER_OPENING;

                            Log.d("xyz", "dispatchTouchEvent(ACTION_MOVE), should intercept");

                            // Because the layout will handle the dragging later,
                            // dispatch ACTION_CANCEL to all the child views.
                            cancelChildrenTouchEvent(event);
                        }
                    } else if (dy < 0 &&
                               mDrawerView.getTranslationY() == 0.f) {
                        if (mDrawerState == STATE_DRAWER_OPENING) {
                            // If sliding thumb up till the drawer is completely
                            // hidden, return the touch event to the child view.
                            Log.d("xyz",
                                  "dispatchTouchEvent(ACTION_MOVE), *** Scrolling up will return touch event to the child view");
                            mDrawerState = STATE_DRAWER_CLOSED;

                            // Cancel the intercepting.
                            cancelSelfTouchEvent(event);

                            // Must cheat the action so that the child view knows
                            // to start a new touch handling session.
                            event.setAction(MotionEvent.ACTION_DOWN);
                        }
                    }
                }

                mTouchCurrentY = event.getY();
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                Log.d("xyz", "dispatchTouchEvent(ACTION_UP|ACTION_CANCEL)");
                mTouchInitY = 0;
                mTouchCurrentY = 0;
                break;
            }
        }

        if (mDrawerState == STATE_DRAWER_OPENING ||
            mDrawerState == STATE_DRAWER_CLOSING) {
            return onTouchEvent(event);
        } else {
            return super.dispatchTouchEvent(event);
        }
    }

    /**
     * Responsible for coordinating the position of the drawer and the content
     * view.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = MotionEventCompat.getActionMasked(event);

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                Log.d("xyz", "  onTouchEvent(ACTION_DOWN)");
                mTouchInitY = event.getY();
                mTouchingChild = findTopChildUnder(event.getX(), event.getY());
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                Log.d("xyz", "  onTouchEvent(ACTION_MOVE)");

                if (mAnimatorSet != null) {
                    mAnimatorSet.cancel();
                    mAnimatorSet = null;
                }

                if (mDrawerState == STATE_DRAWER_OPENING) {
                    // Coordinate the positions of drawer and content views when
                    // the drawer state is under opening.
                    if (mTouchingChild == mContentView) {
                        final float offsetY = event.getY() - mTouchInitY;
                        final float ty = Math.min(
                            Math.max(0, offsetY),
                            mDrawerView.getHeight());
                        ViewCompat.setTranslationY(mDrawerView, ty);
                        ViewCompat.setTranslationY(mContentView, ty);
                        Log.d("xyz", String.format("    onTouchEvent(ACTION_MOVE), opening drawer, ty=%f", ty));
                        invalidate();
                    }
                } else if (mDrawerState == STATE_DRAWER_CLOSING) {
                    final View touchingChild = findTopChildUnder(event.getX(), event.getY());
                    if (touchingChild == mDrawerView &&
                        mTouchingChild != mDrawerView) {
                        // If it is dragging the drawer but it was not previously,
                        // update the initial touch-y.
                        mTouchInitY = event.getY();
                        mTouchingChild = touchingChild;
                    }

                    // Coordinate the position of drawer only when the drawer
                    // state is under closing.
                    if (mTouchingChild == mDrawerView) {
                        final float offsetY = event.getY() - mTouchInitY;
                        final float ty = Math.min(
                            Math.max(0, mDrawerView.getHeight() + offsetY),
                            mDrawerView.getHeight());
                        ViewCompat.setTranslationY(mDrawerView, ty);
                        Log.d("xyz", String.format("    onTouchEvent(ACTION_MOVE), closing drawer, ty=%f", ty));
                        invalidate();
                    }
                }

                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (ifOpenDrawer()) {
                    Log.d("xyz", "  onTouchEvent(ACTION_UP|ACTION_CANCEL), open drawer");
                    openDrawer();
                } else {
                    Log.d("xyz", "  onTouchEvent(ACTION_UP|ACTION_CANCEL), close drawer");
                    closeDrawer();
                }
                Log.d("xyz", "\n");
                mTouchingChild = null;
                break;
            }
        }

        // Note: Be careful of using this layout in a nested view hierarchy.
        // Because it always return true, the parent's onTouchEvent will never
        // be called.
        return true;
    }

    @Override
    public void setOnMenuStateChangeListener(OnMenuStateChange listener) {
        mMenuStateListener = listener;
    }

    public boolean isDrawerOpened() {
        return mDrawerState == STATE_DRAWER_OPENED;
    }

    public void openDrawer() {
        if (mDrawerView == null) return;

        mDrawerState = STATE_DRAWER_OPENED;

        if (mAnimatorSet != null) {
            mAnimatorSet.cancel();
        }

        // The duration is proportional to the translationY.
        final float ty1 = ViewCompat.getTranslationY(mDrawerView);
        final int duration1 = (int) Math.max(
            ANIMATION_DURATION_OPEN *
            (mDrawerView.getHeight() - Math.abs(ty1)) / mDrawerView.getHeight(),
            ANIMATION_DURATION_OPEN / 2.f);

        ObjectAnimator anim1 = ObjectAnimator.ofFloat(mDrawerView, "translationY", mDrawerView.getHeight());
        anim1.setDuration(duration1);
        anim1.setInterpolator(new AccelerateDecelerateInterpolator());
        anim1.addUpdateListener(onAnimatingDrawer());

        // TODO: Animation the translucent overlay.

        if (mTouchingChild != null && mTouchingChild == mContentView) {
            final float ty3 = mContentView.getTranslationY();
            final int duration3 = (int) Math.max(
                ANIMATION_DURATION_OPEN * ty3 / mContentView.getHeight(),
                ANIMATION_DURATION_OPEN / 2.f);

            ObjectAnimator anim3 = ObjectAnimator.ofFloat(mContentView, "translationY", 0);
            anim3.setDuration(duration3);
            anim3.setInterpolator(new AccelerateDecelerateInterpolator());

            mAnimatorSet = new AnimatorSet();
            mAnimatorSet.playTogether(anim1, anim3);
            mAnimatorSet.start();
        } else {
            mAnimatorSet = new AnimatorSet();
            mAnimatorSet.playTogether(anim1);
            mAnimatorSet.start();
        }

        if (mMenuStateListener != null) {
            mMenuStateListener.onShowMenu();
        }
    }

    public void closeDrawer() {
        if (mDrawerView == null) return;

        mDrawerState = STATE_DRAWER_CLOSED;

        if (mAnimatorSet != null) {
            mAnimatorSet.cancel();
        }

        // The duration is proportional to the translationY.
        final float ty1 = ViewCompat.getTranslationY(mDrawerView);
        final int duration1 = (int) Math.max(
            ANIMATION_DURATION_CLOSE * Math.abs(ty1) / mDrawerView.getHeight(),
            ANIMATION_DURATION_CLOSE / 2.f);

        ObjectAnimator anim1 = ObjectAnimator.ofFloat(mDrawerView, "translationY", 0);
        anim1.setDuration(duration1);
        anim1.setInterpolator(new AccelerateDecelerateInterpolator());
        anim1.addUpdateListener(onAnimatingDrawer());

        // TODO: Animation the translucent overlay.

        if (mTouchingChild != null && mTouchingChild == mContentView) {
            final float ty3 = mContentView.getTranslationY();
            final int duration3 = (int) Math.max(
                ANIMATION_DURATION_CLOSE * ty3 / mContentView.getHeight(),
                ANIMATION_DURATION_CLOSE / 2.f);

            ObjectAnimator anim3 = ObjectAnimator.ofFloat(mContentView, "translationY", 0);
            anim3.setDuration(duration3);
            anim3.setInterpolator(new AccelerateDecelerateInterpolator());

            mAnimatorSet = new AnimatorSet();
            mAnimatorSet.playTogether(anim1, anim3);
            mAnimatorSet.start();
        } else {
            mAnimatorSet = new AnimatorSet();
            mAnimatorSet.playTogether(anim1);
            mAnimatorSet.start();
        }

        if (mMenuStateListener != null) {
            mMenuStateListener.onHideMenu();
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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    protected boolean drawChild(Canvas canvas,
                                View child,
                                long drawingTime) {
        if (child == mContentView) {
            // The content view.
            boolean result = super.drawChild(canvas, child, drawingTime);

            // Draw the cover  paint.
//            if (mDrawerState != STATE_DRAWER_OPENING) {
                final int h = mDrawerView.getHeight();
                final float ty = mDrawerView.getTranslationY();
                final int alpha = (int) Math.max(0, COVER_FADE_PAINT_ALPHA * ty / h);
                mCoveredFadeRect.top = getTop() + (int) (ty);
                mCoveredFadePaint.setAlpha(alpha);
                canvas.drawRect(mCoveredFadeRect, mCoveredFadePaint);
//            }

            return result;
        } else {
            return super.drawChild(canvas, child, drawingTime);
        }
    }

    private ValueAnimator.AnimatorUpdateListener onAnimatingDrawer () {
        return new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidate();
            }
        };
    }

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

        for (int i = childCount - 1; i >= 0; --i) {
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

    private void cancelSelfTouchEvent(MotionEvent event) {
        final MotionEvent canceledEvent = MotionEvent.obtain(event);

        canceledEvent.setAction(MotionEvent.ACTION_CANCEL);
        onTouchEvent(canceledEvent);
        canceledEvent.recycle();
    }

    private void cancelChildrenTouchEvent(MotionEvent event) {
        Log.d("xyz", "    cancelChildrenTouchEvent()");
        final MotionEvent canceledEvent = MotionEvent.obtain(event);

        canceledEvent.setAction(MotionEvent.ACTION_CANCEL);
        super.dispatchTouchEvent(canceledEvent);
        canceledEvent.recycle();

        // We cheat the event so that mTouchInitY will be
        // updated for calculating the translationY correctly.
        event.setAction(MotionEvent.ACTION_DOWN);
    }

    private boolean ifOpenDrawer() {
        if (mDrawerView == null) return false;

        switch (mDrawerState) {
            case STATE_DRAWER_OPENING: {
                final float threshold = 0.35f * mDrawerView.getHeight();

                return mDrawerView != null &&
                       ViewCompat.getTranslationY(mDrawerView) > threshold;
            }
            case STATE_DRAWER_CLOSING: {
                if (mTouchingChild == mDrawerView) {
                    final float threshold = 0.8f * mDrawerView.getHeight();

                    return mDrawerView != null &&
                           ViewCompat.getTranslationY(mDrawerView) > threshold;
                } else {
                    return false;
                }
            }
        }

        return false;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {

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
