package com.my.widget;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;

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

        int totalDrag;

        /**
         * Show the banner when ty < SHOW_THRESHOLD * height.
         */
        protected static final float SHOW_THRESHOLD = 0.5f;

        public DrawerBehavior() {
        }

        public DrawerBehavior(Context context, AttributeSet attrs) {
            super(context, attrs);
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

            float ty = ViewCompat.getTranslationY(child);
            if (ty > 0 && dy > 0) {
                ty = Math.max(ty - dy, 0);
                ViewCompat.setTranslationY(child, ty);
                totalDrag += -dy;
                consumed[1] = dy;
            }
//            if (mDraggingDown && dy > 0 || mDraggingUp && dy < 0) {
//                Log.d("xyz", String.format("onNestedPreScroll, dy=%d", dy));
//                dragScale(dy);
//                consumed[1] = dy;
//            }
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
//            if (dyConsumed > 0) {
//                child.scroll(dyConsumed);
//            } else {
//                child.scroll(dyUnconsumed);
//            }

//            totalDrag += -dyUnconsumed;
//
////            int ty = (int) ViewCompat.getTranslationY(child) - dyUnconsumed;
//            ViewCompat.setTranslationY(child, totalDrag);
        }

        @Override
        public void onStopNestedScroll(CoordinatorLayout parent,
                                       View child,
                                       View anchorView) {
            Log.d("xyz", "onStopNestedScroll");
            totalDrag = 0;
//            // Check if the translation is over a threshold, hide it or show it.
//            float ty = Math.abs(child.getScrollTargetView().getTranslationY());
//            Log.d("xyz", String.format("stop the nested scroll, ty=%f", ty));
//            float height = child.getScrollTargetHeight();
//            // When the anchor view is scrolling up.
//            if ((height - ty) / height > SHOW_THRESHOLD) {
//                // Show it.
//                child.showWithAnimation();
//            } else {
//                // Hide it.
//                child.hideWithAnimation();
//            }
        }
    }
}

