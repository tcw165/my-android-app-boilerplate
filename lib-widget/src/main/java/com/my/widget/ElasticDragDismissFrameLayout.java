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

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.NestedScrollingParent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

//import io.plaidapp.util.AnimUtils;
//import io.plaidapp.util.ColorUtils;
//import io.plaidapp.util.ViewUtils;

/**
 * A {@link FrameLayout} which responds to nested scrolls to create drag-dismissable layouts.
 * Applies an elasticity factor to reduce movement as you approach the given dismiss distance.
 * Optionally also scales down content during drag.
 */
public class ElasticDragDismissFrameLayout
    extends FrameLayout
    implements NestedScrollingParent {

    // configurable attribs
    private float mDragDismissDistance = Float.MAX_VALUE;
    private float mDragDismissFraction = -1f;
    private float mDragDismissScale = 1f;
    private boolean mShouldScale = false;
    private float mDragElasticity = 0.8f;

    // state
    private float mTotalDrag;
    private boolean mDraggingDown = false;
    private boolean mDraggingUp = false;

    private List<ElasticDragDismissCallback> mCallbacks;

    public ElasticDragDismissFrameLayout(Context context) {
        this(context, null);
    }

    public ElasticDragDismissFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        final TypedArray array = getContext().obtainStyledAttributes(
            attrs, R.styleable.ElasticDragDismissFrameLayout, 0, 0);

        if (array.hasValue(R.styleable.ElasticDragDismissFrameLayout_dragDismissDistance)) {
            mDragDismissDistance = array.getDimensionPixelSize(
                R.styleable.ElasticDragDismissFrameLayout_dragDismissDistance,
                0);
        } else if (array.hasValue(R.styleable.ElasticDragDismissFrameLayout_dragDismissFraction)) {
            mDragDismissFraction = array.getFloat(
                R.styleable.ElasticDragDismissFrameLayout_dragDismissFraction,
                mDragDismissFraction);
        }
        if (array.hasValue(R.styleable.ElasticDragDismissFrameLayout_dragDismissScale)) {
            mDragDismissScale = array.getFloat(
                R.styleable.ElasticDragDismissFrameLayout_dragDismissScale,
                mDragDismissScale);
            mShouldScale = mDragDismissScale != 1f;
        }
        if (array.hasValue(R.styleable.ElasticDragDismissFrameLayout_dragElasticity)) {
            mDragElasticity = array.getFloat(
                R.styleable.ElasticDragDismissFrameLayout_dragElasticity,
                mDragElasticity);
        }
        array.recycle();
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return (nestedScrollAxes & View.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        // if we're in a drag gesture and the user reverses up the we should take those events
        if (mDraggingDown && dy > 0 || mDraggingUp && dy < 0) {
            dragScale(dy);
            consumed[1] = dy;
        }
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed,
                               int dxUnconsumed, int dyUnconsumed) {
        dragScale(dyUnconsumed);
    }

    @Override
    public void onStopNestedScroll(View child) {
        if (Math.abs(mTotalDrag) >= mDragDismissDistance) {
            dispatchDismissCallback();
        } else { // settle back to natural position
            animate()
                .translationY(0f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(200L)
//                    .setInterpolator(AnimUtils.getFastOutSlowInInterpolator(getContext()))
                .setListener(null)
                .start();
            mTotalDrag = 0;
            mDraggingDown = mDraggingUp = false;
            dispatchDragCallback(0f, 0f, 0f, 0f);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mDragDismissFraction > 0f) {
            mDragDismissDistance = h * mDragDismissFraction;
        }
    }

    public void addListener(ElasticDragDismissCallback listener) {
        if (mCallbacks == null) {
            mCallbacks = new ArrayList<>();
        }
        mCallbacks.add(listener);
    }

    public void removeListener(ElasticDragDismissCallback listener) {
        if (mCallbacks != null && mCallbacks.size() > 0) {
            mCallbacks.remove(listener);
        }
    }

    private void dragScale(int scroll) {
        if (scroll == 0) return;

        mTotalDrag += scroll;

        // track the direction & set the pivot point for scaling
        // don't double track i.e. if start dragging down and then reverse, keep tracking as
        // dragging down until they reach the 'natural' position
        if (scroll < 0 && !mDraggingUp && !mDraggingDown) {
            mDraggingDown = true;
            if (mShouldScale) setPivotY(getHeight());
        } else if (scroll > 0 && !mDraggingDown && !mDraggingUp) {
            mDraggingUp = true;
            if (mShouldScale) setPivotY(0f);
        }
        // how far have we dragged relative to the distance to perform a dismiss
        // (0–1 where 1 = dismiss distance). Decreasing logarithmically as we approach the limit
        float dragFraction = (float) Math.log10(1 + (Math.abs(mTotalDrag) / mDragDismissDistance));

        // calculate the desired translation given the drag fraction
        float dragTo = dragFraction * mDragDismissDistance * mDragElasticity;

        if (mDraggingUp) {
            // as we use the absolute magnitude when calculating the drag fraction, need to
            // re-apply the drag direction
            dragTo *= -1;
        }
        setTranslationY(dragTo);

        if (mShouldScale) {
            final float scale = 1 - ((1 - mDragDismissScale) * dragFraction);
            setScaleX(scale);
            setScaleY(scale);
        }

        // if we've reversed direction and gone past the settle point then clear the flags to
        // allow the list to get the scroll events & reset any transforms
        if ((mDraggingDown && mTotalDrag >= 0)
            || (mDraggingUp && mTotalDrag <= 0)) {
            mTotalDrag = dragTo = dragFraction = 0;
            mDraggingDown = mDraggingUp = false;
            setTranslationY(0f);
            setScaleX(1f);
            setScaleY(1f);
        }
        dispatchDragCallback(dragFraction, dragTo,
                             Math.min(1f, Math.abs(mTotalDrag) / mDragDismissDistance), mTotalDrag);
    }

    private void dispatchDragCallback(float elasticOffset, float elasticOffsetPixels,
                                      float rawOffset, float rawOffsetPixels) {
        if (mCallbacks != null && !mCallbacks.isEmpty()) {
            for (ElasticDragDismissCallback callback : mCallbacks) {
                callback.onDrag(elasticOffset, elasticOffsetPixels,
                                rawOffset, rawOffsetPixels);
            }
        }
    }

    private void dispatchDismissCallback() {
        if (mCallbacks != null && !mCallbacks.isEmpty()) {
            for (ElasticDragDismissCallback callback : mCallbacks) {
                callback.onDragDismissed();
            }
        }
    }

//    /**
//     * An {@link ElasticDragDismissCallback} which fades system chrome (i.e. status bar and
//     * navigation bar) whilst elastic drags are performed and
//     * {@link Activity#finishAfterTransition() finishes} the activity when drag dismissed.
//     */
//    public static class SystemChromeFader extends ElasticDragDismissCallback {
//
//        private final Activity activity;
//        private final int statusBarAlpha;
//        private final int navBarAlpha;
//        private final boolean fadeNavBar;
//
//        public SystemChromeFader(Activity activity) {
//            this.activity = activity;
//            statusBarAlpha = Color.alpha(activity.getWindow().getStatusBarColor());
//            navBarAlpha = Color.alpha(activity.getWindow().getNavigationBarColor());
//            // FIXME: What is this?
//            fadeNavBar = ViewUtils.isNavBarOnBottom(activity);
//        }
//
//        @Override
//        public void onDrag(float elasticOffset, float elasticOffsetPixels,
//                           float rawOffset, float rawOffsetPixels) {
//            if (elasticOffsetPixels > 0) {
//                // dragging downward, fade the status bar in proportion
////                activity.getWindow().setStatusBarColor(ColorUtils.modifyAlpha(activity.getWindow()
////                        .getStatusBarColor(), (int) ((1f - rawOffset) * statusBarAlpha)));
//            } else if (elasticOffsetPixels == 0) {
//                // reset
////                activity.getWindow().setStatusBarColor(ColorUtils.modifyAlpha(
////                        activity.getWindow().getStatusBarColor(), statusBarAlpha));
////                activity.getWindow().setNavigationBarColor(ColorUtils.modifyAlpha(
////                        activity.getWindow().getNavigationBarColor(), navBarAlpha));
//            } else if (fadeNavBar) {
//                // dragging upward, fade the navigation bar in proportion
////                activity.getWindow().setNavigationBarColor(
////                        ColorUtils.modifyAlpha(activity.getWindow().getNavigationBarColor(),
////                                (int) ((1f - rawOffset) * navBarAlpha)));
//            }
//        }
//
//        public void onDragDismissed() {
//            // FIXME: API>21
//            activity.finishAfterTransition();
//        }
//    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    public interface ElasticDragDismissCallback {
        /**
         * Called for each drag event.
         *
         * @param elasticOffset       Indicating the drag offset with elasticity applied i.e. may
         *                            exceed 1.
         * @param elasticOffsetPixels The elastically scaled drag distance in pixels.
         * @param rawOffset           Value from [0, 1] indicating the raw drag offset i.e.
         *                            without elasticity applied. A value of 1 indicates that the
         *                            dismiss distance has been reached.
         * @param rawOffsetPixels     The raw distance the user has dragged
         */
        void onDrag(float elasticOffset,
                    float elasticOffsetPixels,
                    float rawOffset,
                    float rawOffsetPixels);

        /**
         * Called when dragging is released and has exceeded the threshold dismiss distance.
         */
        void onDragDismissed();
    }
}
