package com.my.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.view.animation.DecelerateInterpolator;

@CoordinatorLayout.DefaultBehavior(ElasticNestedScrollView.DrawerBehavior.class)
public class ElasticNestedScrollView extends NestedScrollView {

    public ElasticNestedScrollView(Context context) {
        this(context, null);
    }

    public ElasticNestedScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    /**
     * The view with this behavior will hide itself if the anchor view is
     * receiving a scroll-down event; will show up if the anchor view is
     * receiving a scroll-up event.
     */
    public static class DrawerBehavior
        extends CoordinatorLayout.Behavior<View> {

        // Configurable attributes.
        float mDragStopDistance = 422f;
        float mDragElasticity = 0.95f;

        // State.
        float mTotalDrag = 0;
        boolean mIsDraggingUp;
        boolean mIsDraggingDown;

        /**
         * Show the banner when ty < SHOW_THRESHOLD * height.
         */
        static final float SHOW_THRESHOLD = 0.5f;

        // Animator.
        AnimatorSet mAnimSet;
        Animator.AnimatorListener mAnimListener = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        };
        ValueAnimator.AnimatorUpdateListener mCoverUpdater = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
//                final int h = mElasticScrollView.getHeight();
//                final float ty = Math.abs(mElasticScrollView.getTranslationY());
//                final int alpha = (int) Math.max(0, COVER_FADE_PAINT_ALPHA * (h - ty) / h);
//
//                mCoveredFadePaint.setAlpha(alpha);
//                invalidate();
            }
        };

        public DrawerBehavior() {
            // DO NOTHING.
        }

        public DrawerBehavior(Context context,
                              AttributeSet attrs) {
            super(context, attrs);

            // Handle the attribute.
            final float density = context.getResources().getDisplayMetrics().density;
            mDragStopDistance = mDragStopDistance * density;
        }

        @Override
        public boolean onStartNestedScroll(CoordinatorLayout parent,
                                           View child,
                                           View directTargetChild,
                                           View target,
                                           int nestedScrollAxes) {
            Log.d("xyz", "onStartNestedScroll");
            return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
        }

        @Override
        public void onNestedPreScroll(CoordinatorLayout coordinatorLayout,
                                      View child,
                                      View target,
                                      int dx,
                                      int dy,
                                      int[] consumed) {
            Log.d("xyz", "onNestedPreScroll, dy=" + dy + "; consume[1]=" + consumed[1]);

            // If we're in a drag gesture and the user reverses up the we should
            // take those events
            if ((mIsDraggingDown && dy > 0) ||
                (mIsDraggingUp && dy < 0)) {
                dragTo(target, dy);
                consumed[1] = dy;
            }
        }

        @Override
        public void onNestedScroll(CoordinatorLayout parent,
                                   View child,
                                   View anchorView,
                                   int dxConsumed,
                                   int dyConsumed,
                                   int dxUnconsumed,
                                   int dyUnconsumed) {
            Log.d("xyz", String.format("onNestedScroll, dyConsumed=%d, dyUnconsumed=%d", dyConsumed, dyUnconsumed));

            dragTo(child, dyUnconsumed);
        }

        @Override
        public void onStopNestedScroll(CoordinatorLayout parent,
                                       View child,
                                       View anchorView) {
            Log.d("xyz", "onStopNestedScroll");

            if (Math.abs(mTotalDrag) >= mDragStopDistance) {
                Log.d("xyz", "onStopNestedScroll-> over drag stop distance");
            }
            reset(child);
        }

        ///////////////////////////////////////////////////////////////////////////
        // Protected / Private Methods ////////////////////////////////////////////

        void dragTo(View target,
                    int scroll) {
            // Ensure the animation is cancelled.
            if (mAnimSet != null) {
                mAnimSet.cancel();
                mAnimSet = null;
            }

            mTotalDrag += scroll;

            // track the direction & set the pivot point for scaling
            // don't double track i.e. if start dragging down and then reverse,
            // keep tracking as dragging down until they reach the 'natural'
            // position
            if (scroll < 0 && !mIsDraggingDown && !mIsDraggingUp) {
                mIsDraggingDown = true;
            } else if (scroll > 0 && !mIsDraggingDown && !mIsDraggingUp) {
                mIsDraggingUp = true;
            }

            // how far have we dragged relative to the distance to perform a dismiss
            // (0–1 where 1 = dismiss distance). Decreasing logarithmically as we
            // approach the limit
            float dragFraction = (float) Math.log10(1 + (Math.abs(mTotalDrag) / mDragStopDistance));

            // calculate the desired translation given the drag fraction
            float dragTo = dragFraction * mDragStopDistance * mDragElasticity;

            if (mIsDraggingUp) {
                // as we use the absolute magnitude when calculating the drag
                // fraction, need to re-apply the drag direction
                dragTo *= -1;
            }

            ViewCompat.setTranslationY(target, dragTo);

            // if we've reversed direction and gone past the settle point then clear
            // the flags to allow the list to get the scroll events & reset any
            // transforms
            if ((mIsDraggingDown && mTotalDrag >= 0)
                || (mIsDraggingUp && mTotalDrag <= 0)) {
                mTotalDrag = dragTo = dragFraction = 0f;
                mIsDraggingDown = mIsDraggingUp = false;
                ViewCompat.setTranslationY(target, 0f);
            }

//            dispatchDragCallback(
//                dragFraction, dragTo,
//                Math.min(1f, Math.abs(mTotalDrag) / mDragDismissDistance), mTotalDrag);
        }

        void reset(View target) {
            final ObjectAnimator animTy = ObjectAnimator
                .ofFloat(target, "translationY", 0);

            if (mAnimSet != null) {
                mAnimSet.cancel();
            }

            mAnimSet = new AnimatorSet();
            mAnimSet.playTogether(animTy);
            mAnimSet.setDuration(200L);
            mAnimSet.setInterpolator(new DecelerateInterpolator());
            mAnimSet.start();

            // Update the state.
            mTotalDrag = 0;
            mIsDraggingDown = false;
        }
    }
}

