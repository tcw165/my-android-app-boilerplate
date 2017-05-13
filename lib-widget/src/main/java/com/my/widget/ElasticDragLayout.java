// Copyright (c) 2017-present boyw165
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

package com.my.widget;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * A {@link CoordinatorLayout} which responds to nested scrolls to create drag-
 * dismissable layouts. Applies an elasticity factor to reduce movement as you
 * approach the given dismiss distance.
 * Optionally also scales down content during drag.
 * <br/>
 * Note: Add {@link R.styleable#ElasticDragLayout_elastic_elasticScrollView} attribute
 * to the child view you want it to be dragged elastically in the layout file.
 * <br/> <br/>
 * Attributes for itself:
 * <ul>
 * <li>
 * {@link R.styleable#ElasticDragLayout_elastic_dragOverMaxDistance}
 * The over dragging distance
 * </li>
 * </ul>
 * <ul>
 * <li>
 * {@link R.styleable#ElasticDragLayout_elastic_dragScale}
 * The distance that the {@link OnElasticDragCallback#onDragOver(float)}
 * callback is called when over dragging.
 * </li>
 * </ul>
 * <ul>
 * <li>
 * {@link R.styleable#ElasticDragLayout_elastic_dragElasticity}
 * The scale factor when over dragging.
 * </li>
 * </ul>
 * <br/> <br/>
 * Attributes for child views:
 * <ul>
 * <li>
 * {@link R.styleable#ElasticDragLayout_elastic_elasticScrollView}
 * The child view with this attribute is a elastic-draggable view.
 * </li>
 * </ul>
 * <ul>
 * <li>
 * {@link R.styleable#ElasticDragLayout_elastic_elasticScrollViewHeader}
 * </li>
 * </ul>
 * <ul>
 * <li>
 * {@link R.styleable#ElasticDragLayout_elastic_elasticScrollViewFooter}
 * </li>
 * </ul>
 * <br/>
 */
public class ElasticDragLayout extends CoordinatorLayout {

    // Configurable attributes.
    /**
     * The over dragging distance.
     * <br/>
     * Attribute:
     * <br/>
     * See {@link R.styleable#ElasticDragLayout_elastic_dragOverMaxDistance}.
     */
    protected float mDragOverMaxDistance = 244f;
    /**
     * The distance that the {@link OnElasticDragCallback#onDragOver(float)}
     * callback is called when over dragging.
     * <br/>
     * Attribute:
     * <br/>
     * See {@link R.styleable#ElasticDragLayout_elastic_dragOverDistance}.
     */
    protected float mDragOverDistance = 56f;
    /**
     * The scale factor when over dragging.
     * <br/>
     * Attribute:
     * <br/>
     * See {@link R.styleable#ElasticDragLayout_elastic_dragScale}.
     */
    protected float mDragScale = 1.f;
    protected boolean mShouldScale = false;
    /**
     * The elasticity factor of over dragging.
     * <br/>
     * Attribute:
     * <br/>
     * See {@link R.styleable#ElasticDragLayout_elastic_dragElasticity}.
     */
    protected float mDragElasticity = 0.8f;

    public static final int OVER_DRAG_TWO_WAYS = 0;
    public static final int OVER_DRAG_DOWN_ONLY = 1;
    public static final int OVER_DRAG_UP_ONLY = 2;
    /**
     * The flag controlling the behavior of the over drag.
     * <br/>
     * Attribute:
     * <br/>
     */
    // TODO: Add attribute
    protected int mDragDirection = OVER_DRAG_TWO_WAYS;
//    protected int mDragDirection = OVER_DRAG_DOWN_ONLY;
//    protected int mDragDirection = OVER_DRAG_UP_ONLY;

    public static final int OVER_DRAG_HORIZONTAL = 1;
    public static final int OVER_DRAG_HORIZONTAL_START_ONLY = 1 << 1;
    public static final int OVER_DRAG_HORIZONTAL_END_ONLY = 1 << 2;
    public static final int OVER_DRAG_VERTICAL = 1 << 3;
    public static final int OVER_DRAG_VERTICAL_START_ONLY = 1 << 4;
    public static final int OVER_DRAG_VERTICAL_END_ONLY = 1 << 5;
    // TODO: Replace mDragDirection
    /**
     * The flag controlling the over drag orientation.
     * <br/>
     * Attribute:
     * <br/>
     * See {@link R.styleable#ElasticDragLayout_elastic_OverDragOrientation}.
     */
    protected int mOverDragOrientation = OVER_DRAG_VERTICAL;

    // State
    protected float mTotalDrag;
    protected boolean mDraggingEnd = false;
    protected boolean mDraggingStart = false;

    // Views related.
    protected View mElasticScrollView;
    protected View mElasticScrollViewHeader;
    protected View mElasticScrollViewFooter;
    protected List<OnElasticDragCallback> mCallbacks;

    // Animator.
    protected AnimatorSet mAnimSet;

    public ElasticDragLayout(Context context,
                             AttributeSet attrs) {
        super(context, attrs);

        final float density = context.getResources().getDisplayMetrics().density;
        final TypedArray array = getContext().obtainStyledAttributes(
            attrs, R.styleable.ElasticDragLayout, 0, 0);

        // Init configurable attributes.
        if (array.hasValue(R.styleable.ElasticDragLayout_elastic_dragOverMaxDistance)) {
            mDragOverMaxDistance = array.getDimensionPixelSize(
                R.styleable.ElasticDragLayout_elastic_dragOverMaxDistance,
                (int) (mDragOverMaxDistance * density));
        }
        if (array.hasValue(R.styleable.ElasticDragLayout_elastic_dragOverDistance)) {
            mDragOverDistance = array.getDimensionPixelSize(
                R.styleable.ElasticDragLayout_elastic_dragOverDistance,
                (int) (mDragOverDistance * density));
        }
        if (array.hasValue(R.styleable.ElasticDragLayout_elastic_dragScale)) {
            mDragScale = array.getFloat(
                R.styleable.ElasticDragLayout_elastic_dragScale,
                mDragScale);
            mShouldScale = mDragScale != 1f;
        }
        if (array.hasValue(R.styleable.ElasticDragLayout_elastic_dragElasticity)) {
            mDragElasticity = array.getFloat(
                R.styleable.ElasticDragLayout_elastic_dragElasticity,
                mDragElasticity);
        }
        if (array.hasValue(R.styleable.ElasticDragLayout_elastic_OverDragOrientation)) {
            mOverDragOrientation = array.getInt(
                R.styleable.ElasticDragLayout_elastic_OverDragOrientation,
                OVER_DRAG_VERTICAL);
        }
        array.recycle();
    }

    @Override
    public boolean onStartNestedScroll(View child,
                                       View target,
                                       int nestedScrollAxes) {
        // Ensure the animation is cancelled.
        if (mAnimSet != null) {
            mAnimSet.cancel();
            mAnimSet = null;
        }

        if ((mOverDragOrientation & OVER_DRAG_HORIZONTAL) == OVER_DRAG_HORIZONTAL) {
            return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_HORIZONTAL) != 0;
        } else {
            // FIXME: Support full orientation.
            return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
        }
    }

    @Override
    public void onNestedPreScroll(View target,
                                  int dx,
                                  int dy,
                                  int[] consumed) {
        super.onNestedPreScroll(target, dx, dy, consumed);

        // If we're in a drag gesture and the user reverses up the we should
        // take those events
        if ((mOverDragOrientation & OVER_DRAG_HORIZONTAL) == OVER_DRAG_HORIZONTAL) {
            if (mDraggingEnd && dx > 0 || mDraggingStart && dx < 0) {
                dragScaleHorizontally(dx);
                consumed[0] = dx;
            }
        } else {
            // FIXME: Support full orientation.
            if (mDraggingEnd && dy > 0 || mDraggingStart && dy < 0) {
                dragScaleVertically(dy);
                consumed[1] = dy;
            }
        }
    }

    @Override
    public void onNestedScroll(View target,
                               int dxConsumed,
                               int dyConsumed,
                               int dxUnconsumed,
                               int dyUnconsumed) {
        super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);

        if ((mOverDragOrientation & OVER_DRAG_HORIZONTAL) == OVER_DRAG_HORIZONTAL) {
            dragScaleHorizontally(dxUnconsumed);
        } else {
            // FIXME: Support full orientation.
            dragScaleVertically(dyUnconsumed);
        }
    }

    @Override
    public void onStopNestedScroll(View child) {
        super.onStopNestedScroll(child);

        float totalDrag = mTotalDrag;
        if (mDragDirection == OVER_DRAG_UP_ONLY) {
            totalDrag = Math.max(0, totalDrag);
        } else if (mDragDirection == OVER_DRAG_DOWN_ONLY) {
            totalDrag = Math.min(0, totalDrag);
        }

        try {
            if (Math.abs(totalDrag) >= mDragOverDistance) {
                onDragOver(totalDrag);
            } else {
                onDragCancel();
            }
        } finally {
            // Update the state.
            mTotalDrag = 0;
            mDraggingEnd = mDraggingStart = false;
        }
    }

    @Override
    public boolean onNestedPreFling(View target,
                                    float velocityX,
                                    float velocityY) {
        Log.d("xyz", "ElasticDragLayout#onNestedPreFling(target=" + target +
                     ", velocityX=" + velocityX +
                     ", velocityY=" + velocityY);
        return super.onNestedPreFling(target, velocityX, velocityY);
    }

    @Override
    public boolean onNestedFling(View target,
                                 float velocityX,
                                 float velocityY,
                                 boolean consumed) {
        Log.d("xyz", "ElasticDragLayout#onNestedFling(target=" + target +
                     ", velocityX=" + velocityX +
                     ", velocityY=" + velocityY +
                     ", consumed=" + consumed);
        return super.onNestedFling(target, velocityX, velocityY, consumed);
    }

    @SuppressWarnings("unused")
    public void addOnElasticDragDismissListener(OnElasticDragCallback listener) {
        if (mCallbacks == null) {
            mCallbacks = new ArrayList<>();
        }
        mCallbacks.add(listener);
    }

    @SuppressWarnings("unused")
    public void removeOnElasticDragDismissListener(OnElasticDragCallback listener) {
        if (mCallbacks != null && mCallbacks.size() > 0) {
            mCallbacks.remove(listener);
        }
    }

    @SuppressWarnings("unused")
    public void removeAllOnElasticDragDismissListeners() {
        while (!mCallbacks.isEmpty()) {
            removeOnElasticDragDismissListener(mCallbacks.get(0));
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        // The CoordinatorLayout.LayoutParams's constructor taking attribute
        // as arguments is private.
        // FIXME: Remove it if the constructor is public in the new release.
        final ElasticDragLayout.LayoutParams params = new ElasticDragLayout.LayoutParams(
            super.generateLayoutParams(attrs));
        final TypedArray array = getContext().obtainStyledAttributes(
            attrs, R.styleable.ElasticDragLayout);

        try {
            // Custom attributes.
            if (array.hasValue(R.styleable.ElasticDragLayout_elastic_elasticScrollView)) {
                params.isElasticScrollView = array.getBoolean(
                    R.styleable.ElasticDragLayout_elastic_elasticScrollView, false);
            }
            if (array.hasValue(R.styleable.ElasticDragLayout_elastic_elasticScrollViewHeader)) {
                params.isElasticScrollViewHeader = array.getBoolean(
                    R.styleable.ElasticDragLayout_elastic_elasticScrollViewHeader, false);
            }
            if (array.hasValue(R.styleable.ElasticDragLayout_elastic_elasticScrollViewFooter)) {
                params.isElasticScrollViewFooter = array.getBoolean(
                    R.styleable.ElasticDragLayout_elastic_elasticScrollViewFooter, false);
            }
        } finally {
            array.recycle();
        }

        return params;
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new ElasticDragLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new ElasticDragLayout.LayoutParams(p);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // Ensure the movable child list.
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; ++i) {
            final View child = getChildAt(i);
            final LayoutParams params = (LayoutParams) child.getLayoutParams();
            if (params == null) continue;

            if (params.isElasticScrollView) {
                mElasticScrollView = child;
                // Ensure the nested-scrolling is enabled.
                ViewCompat.setNestedScrollingEnabled(child, true);
            } else if (params.isElasticScrollViewHeader) {
                mElasticScrollViewHeader = child;
                // Ensure the nested-scrolling is enabled.
                ViewCompat.setNestedScrollingEnabled(child, true);
            } else if (params.isElasticScrollViewFooter) {
                mElasticScrollViewFooter = child;
                // Ensure the nested-scrolling is enabled.
                ViewCompat.setNestedScrollingEnabled(child, true);
            }
        }

        // Very Important: setting this property to true to make the
        // view fill the visible area!
        if (mElasticScrollView instanceof NestedScrollView) {
            NestedScrollView child = (NestedScrollView) mElasticScrollView;
            child.setFillViewport(true);
        }
    }

    @Override
    protected void onSizeChanged(int w,
                                 int h,
                                 int oldW,
                                 int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);

        // Ensure the animation is cancelled.
        if (mAnimSet != null) {
            mAnimSet.cancel();
            mAnimSet = null;
        }
    }

    @Override
    protected void onLayout(boolean changed,
                            int left,
                            int top,
                            int right,
                            int bottom) {
        // Layout children.
        super.onLayout(changed, left, top, right, bottom);

        // TODO: Handle scroll-view header and footer.
    }

    /**
     * Called in {@link #onStopNestedScroll(View)} when the dragging distance
     * is less than the {@link #mDragOverMaxDistance}.
     */
    protected void onDragCancel() {
        if (!(mElasticScrollView instanceof NestedScrollingChild)) return;

        doAnimation();

        // Callbacks.
        dispatchOnDragCancelCallbacks();
    }

    /**
     * Called in {@link #onStopNestedScroll(View)} when the dragging distance
     * is greater than the {@link #mDragOverMaxDistance}.
     */
    protected void onDragOver(final float totalDrag) {
        if (!(mElasticScrollView instanceof NestedScrollingChild)) {
            dispatchOnOverDraggedCallbacks(0);
            return;
        }

        doAnimation();

        // Callbacks.
        dispatchOnOverDraggedCallbacks(totalDrag);
    }

    protected void doAnimation() {
        // TODO: If the backport of the transition library is promising, then
        // TODO: I need to use it instead.
        final ObjectAnimator animTxy;
        if ((mOverDragOrientation & OVER_DRAG_HORIZONTAL) == OVER_DRAG_HORIZONTAL) {
            animTxy = ObjectAnimator
                .ofFloat(mElasticScrollView, "translationX", 0);
        } else {
            // FIXME: Support full orientation.
            animTxy = ObjectAnimator
                .ofFloat(mElasticScrollView, "translationY", 0);
        }
        final ObjectAnimator animSx = ObjectAnimator
            .ofFloat(mElasticScrollView, "scaleX", 1.f);
        final ObjectAnimator animSy = ObjectAnimator
            .ofFloat(mElasticScrollView, "scaleY", 1.f);
        // TODO: Handle scroll-view header and footer.

        if (mAnimSet != null) {
            mAnimSet.cancel();
        }
        mAnimSet = new AnimatorSet();
        mAnimSet.playTogether(animTxy, animSx, animSy);
        mAnimSet.setDuration(300L);
        mAnimSet.setInterpolator(new DecelerateInterpolator());
        mAnimSet.start();
    }

    protected void dragScaleVertically(int scroll) {
        if (scroll == 0 ||
            !(mElasticScrollView instanceof NestedScrollingChild)) {
            return;
        }

        // Ensure the animation is cancelled.
        if (mAnimSet != null) {
            mAnimSet.cancel();
            mAnimSet = null;
        }

        mTotalDrag += scroll;
        Log.d("xyz", "dragScaleVertically: dy=" + scroll + ", totalDrag=" + mTotalDrag);

        // track the direction & set the pivot point for scaling
        // don't double track i.e. if start dragging down and then reverse,
        // keep tracking as dragging down until they reach the 'natural' position
        if (scroll < 0 && !mDraggingStart && !mDraggingEnd) {
            mDraggingEnd = true;
            if (mShouldScale) {
                ViewCompat.setPivotY(mElasticScrollView, mElasticScrollView.getHeight());
                ViewCompat.setPivotX(mElasticScrollView, mElasticScrollView.getWidth() / 2);
            }
        } else if (scroll > 0 && !mDraggingEnd && !mDraggingStart) {
            mDraggingStart = true;
            if (mShouldScale) {
                ViewCompat.setPivotY(mElasticScrollView, 0f);
                ViewCompat.setPivotX(mElasticScrollView, mElasticScrollView.getWidth() / 2);
            }
        }
        // how far have we dragged relative to the distance to perform a dismiss
        // (0–1 where 1 = dismiss distance). Decreasing logarithmically as we
        // approach the limit
        float dragFraction = (float) Math.log10(1 + (Math.abs(mTotalDrag) / mDragOverMaxDistance));

        // calculate the desired translation given the drag fraction
        float dragTo = dragFraction * mDragOverMaxDistance * mDragElasticity;

        if (mDraggingStart) {
            // as we use the absolute magnitude when calculating the drag
            // fraction, need to re-apply the drag direction
            dragTo *= -1;
        }

        // Clamp dragTo.
        if (mDragDirection == OVER_DRAG_DOWN_ONLY) {
            // To positive value (dragging down only).
            dragTo = Math.max(0, dragTo);
        } else if (mDragDirection == OVER_DRAG_UP_ONLY) {
            // To negative value (dragging up only).
            dragTo = Math.min(0, dragTo);
        }

        ViewCompat.setTranslationY(mElasticScrollView, dragTo);
        // TODO: Handle scroll-view header and footer.

        if (mShouldScale) {
            final float scale = 1 - ((1 - mDragScale) * dragFraction);
            ViewCompat.setScaleX(mElasticScrollView, scale);
            ViewCompat.setScaleY(mElasticScrollView, scale);
        }

        // if we've reversed direction and gone past the settle point then clear
        // the flags to allow the list to get the scroll events & reset any
        // transforms
        if ((mDraggingEnd && mTotalDrag >= 0)
            || (mDraggingStart && mTotalDrag <= 0)) {
            mTotalDrag = dragTo = dragFraction = 0f;
            mDraggingEnd = mDraggingStart = false;
            ViewCompat.setTranslationY(mElasticScrollView, 0f);
            ViewCompat.setScaleX(mElasticScrollView, 1f);
            ViewCompat.setScaleY(mElasticScrollView, 1f);
        }

        dispatchOnDragCallbacks(
            dragFraction, dragTo,
            Math.min(1f, Math.abs(mTotalDrag) / mDragOverMaxDistance), mTotalDrag);

        onPostDragScale(dragTo);
    }

    private void dragScaleHorizontally(int scroll) {
        if (scroll == 0 ||
            !(mElasticScrollView instanceof NestedScrollingChild)) {
            return;
        }

        // Ensure the animation is cancelled.
        if (mAnimSet != null) {
            mAnimSet.cancel();
            mAnimSet = null;
        }

        mTotalDrag += scroll;
        Log.d("xyz", "dragScaleVertically: dy=" + scroll + ", totalDrag=" + mTotalDrag);

        // track the direction & set the pivot point for scaling
        // don't double track i.e. if start dragging down and then reverse,
        // keep tracking as dragging down until they reach the 'natural' position
        if (scroll < 0 && !mDraggingStart && !mDraggingEnd) {
            mDraggingEnd = true;
            if (mShouldScale) {
                ViewCompat.setPivotY(mElasticScrollView, mElasticScrollView.getHeight());
                ViewCompat.setPivotX(mElasticScrollView, mElasticScrollView.getWidth() / 2);
            }
        } else if (scroll > 0 && !mDraggingEnd && !mDraggingStart) {
            mDraggingStart = true;
            if (mShouldScale) {
                ViewCompat.setPivotY(mElasticScrollView, 0f);
                ViewCompat.setPivotX(mElasticScrollView, mElasticScrollView.getWidth() / 2);
            }
        }
        // how far have we dragged relative to the distance to perform a dismiss
        // (0–1 where 1 = dismiss distance). Decreasing logarithmically as we
        // approach the limit
        float dragFraction = (float) Math.log10(1 + (Math.abs(mTotalDrag) / mDragOverMaxDistance));

        // calculate the desired translation given the drag fraction
        float dragTo = dragFraction * mDragOverMaxDistance * mDragElasticity;

        if (mDraggingStart) {
            // as we use the absolute magnitude when calculating the drag
            // fraction, need to re-apply the drag direction
            dragTo *= -1;
        }

//        // Clamp dragTo.
//        if (mDragDirection == OVER_DRAG_DOWN_ONLY) {
//            // To positive value (dragging down only).
//            dragTo = Math.max(0, dragTo);
//        } else if (mDragDirection == OVER_DRAG_UP_ONLY) {
//            // To negative value (dragging up only).
//            dragTo = Math.min(0, dragTo);
//        }

        ViewCompat.setTranslationX(mElasticScrollView, dragTo);
        // TODO: Handle scroll-view header and footer.

        if (mShouldScale) {
            final float scale = 1 - ((1 - mDragScale) * dragFraction);
            ViewCompat.setScaleX(mElasticScrollView, scale);
            ViewCompat.setScaleY(mElasticScrollView, scale);
        }

        // if we've reversed direction and gone past the settle point then clear
        // the flags to allow the list to get the scroll events & reset any
        // transforms
        if ((mDraggingEnd && mTotalDrag >= 0)
            || (mDraggingStart && mTotalDrag <= 0)) {
            mTotalDrag = dragTo = dragFraction = 0f;
            mDraggingEnd = mDraggingStart = false;
            ViewCompat.setTranslationX(mElasticScrollView, 0f);
            ViewCompat.setScaleX(mElasticScrollView, 1f);
            ViewCompat.setScaleY(mElasticScrollView, 1f);
        }

        dispatchOnDragCallbacks(
            dragFraction, dragTo,
            Math.min(1f, Math.abs(mTotalDrag) / mDragOverMaxDistance), mTotalDrag);

        onPostDragScale(dragTo);
    }

    /**
     * Called after {@link #dragScaleVertically(int)}. It's an interface for the child
     * class.
     */
    protected void onPostDragScale(float dragTo) {
        // Dummy implementation.
    }

    protected void dispatchOnDragCallbacks(float elasticOffsetPercent,
                                           float elasticOffsetPixels,
                                           float rawOffsetPercent,
                                           float rawOffsetPixels) {
        if (mCallbacks != null && !mCallbacks.isEmpty()) {
            for (OnElasticDragCallback callback : mCallbacks) {
                callback.onDrag(elasticOffsetPercent, elasticOffsetPixels,
                                rawOffsetPercent, rawOffsetPixels);
            }
        }
    }

    protected void dispatchOnOverDraggedCallbacks(float totalScroll) {
        if (mCallbacks != null && !mCallbacks.isEmpty()) {
            for (OnElasticDragCallback callback : mCallbacks) {
                callback.onDragOver(totalScroll);
            }
        }
    }

    protected void dispatchOnDragCancelCallbacks() {
        if (mCallbacks != null && !mCallbacks.isEmpty()) {
            for (OnElasticDragCallback callback : mCallbacks) {
                callback.onDragCancel();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    public static class LayoutParams extends CoordinatorLayout.LayoutParams {

        boolean isElasticScrollView;
        boolean isElasticScrollViewHeader;
        boolean isElasticScrollViewFooter;

        LayoutParams(int width,
                     int height) {
            super(width, height);
        }

        /**
         * A workaround constructor that copying the necessary fields from
         * CoordinatorLayout.LayoutParams to it.
         */
        LayoutParams(CoordinatorLayout.LayoutParams p) {
            super(p);

            // FIXME: Doing this is simply because the Coordinator's
            // FIXME: constructor is private.
            // FIXME: It might cause side effect like un-synchronized
            // FIXME: Remove it if the constructor is public in the new release.
            anchorGravity = p.anchorGravity;
            keyline = p.keyline;
            insetEdge = p.insetEdge;
            dodgeInsetEdges = p.dodgeInsetEdges;

            setAnchorId(p.getAnchorId());
            setBehavior(p.getBehavior());
            // Ensure null.
            p.setBehavior(null);
        }

        LayoutParams(ViewGroup.LayoutParams p) {
            super(p);
        }
    }

    public interface OnElasticDragCallback {
        /**
         * Called for each drag event.
         *
         * @param elasticOffsetPercent Indicating the drag offset with elasticity
         *                             applied i.e. may exceed 1.
         * @param elasticOffsetPixels  The elastically scaled drag distance in
         *                             pixels.
         * @param rawOffsetPercent     Value from [0, 1] indicating the raw drag
         *                             offset i.e. without elasticity applied. A
         *                             value of 1 indicates that the dismiss
         *                             distance has been reached.
         * @param rawOffsetPixels      The raw distance the user has dragged.
         */
        void onDrag(float elasticOffsetPercent,
                    float elasticOffsetPixels,
                    float rawOffsetPercent,
                    float rawOffsetPixels);

        /**
         * Called when dragging is released and has exceeded the threshold
         * dismiss distance.
         */
        void onDragOver(float totalDrag);

        /**
         * Called when dragging is released and yet exceeded the threshold
         * dismiss distance.
         */
        void onDragCancel();
    }
}
