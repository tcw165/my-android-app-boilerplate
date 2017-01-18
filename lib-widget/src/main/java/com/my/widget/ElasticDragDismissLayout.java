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

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link FrameLayout} which responds to nested scrolls to create drag-
 * dismissable layouts. Applies an elasticity factor to reduce movement
 * as you approach the given dismiss distance.
 * Optionally also scales down content during drag.
 * <br/>
 * The first child must be {@link NestedScrollingChild}.
 * <br/>
 * Attributes:
 * <br/>
 * {@link R.styleable#ElasticDragDismissLayout_dragDismissDistance}
 * <br/>
 * {@link R.styleable#ElasticDragDismissLayout_dragDismissFraction}
 * <br/>
 * {@link R.styleable#ElasticDragDismissLayout_dragDismissScale}
 * <br/>
 * {@link R.styleable#ElasticDragDismissLayout_dragElasticity}
 * <br/>
 */
public class ElasticDragDismissLayout
    extends CoordinatorLayout
    implements NestedScrollingParent {

    public static final int CLOSE_BY_DRAG = 0;
    public static final int CLOSE_BY_BACK_PRESSED = 1;
    public static final int CLOSE_BY_COVER_PRESSED = 2;

    // Configurable attributes.
    float mDragDismissDistance = 114f;
    float mDragDismissFraction = -1f;
    float mDragDismissScale = 0.9f;
    boolean mShouldScale = false;
    float mDragElasticity = 0.8f;

    // State
    float mTotalDrag;
    int mClosedBy = -1;
    boolean mIsOpened = false;
    boolean mDraggingDown = false;
    boolean mDraggingUp = false;

    // Rendering related.
    Paint mCoveredFadePaint;
    Rect mCoveredFadeRect;
    static final int COVER_FADE_PAINT_ALPHA = (int) (0.33 * 0xFF);

    // Views related.
    View mMovableChildView;
    List<DragDismissCallback> mCallbacks;

    // Animator.
    AnimatorSet mAnimSet;
    AnimatorUpdateListener mCoverUpdater = new AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            final int h = mMovableChildView.getHeight();
            final float ty = Math.abs(mMovableChildView.getTranslationY());
            final int alpha = (int) Math.max(0, COVER_FADE_PAINT_ALPHA * (h - ty) / h);

            mCoveredFadePaint.setAlpha(alpha);
            Log.d("xyz", "update cover alpha=" + alpha);
            invalidate();
        }
    };

    public ElasticDragDismissLayout(Context context,
                                    AttributeSet attrs) {
        super(context, attrs);

        final float density = context.getResources().getDisplayMetrics().density;
        final TypedArray array = getContext().obtainStyledAttributes(
            attrs, R.styleable.ElasticDragDismissLayout, 0, 0);

        // Init configurable attributes.
        if (array.hasValue(R.styleable.ElasticDragDismissLayout_dragDismissDistance)) {
            mDragDismissDistance = array.getDimensionPixelSize(
                R.styleable.ElasticDragDismissLayout_dragDismissDistance,
                (int) (mDragDismissDistance * density));
        } else if (array.hasValue(R.styleable.ElasticDragDismissLayout_dragDismissFraction)) {
            mDragDismissFraction = array.getFloat(
                R.styleable.ElasticDragDismissLayout_dragDismissFraction,
                mDragDismissFraction);
        }
        if (array.hasValue(R.styleable.ElasticDragDismissLayout_dragDismissScale)) {
            mDragDismissScale = array.getFloat(
                R.styleable.ElasticDragDismissLayout_dragDismissScale,
                mDragDismissScale);
            mShouldScale = mDragDismissScale != 1f;
        }
        if (array.hasValue(R.styleable.ElasticDragDismissLayout_dragElasticity)) {
            mDragElasticity = array.getFloat(
                R.styleable.ElasticDragDismissLayout_dragElasticity,
                mDragElasticity);
        }
        array.recycle();

        mCoveredFadePaint = new Paint();
        mCoveredFadePaint.setColor(Color.BLACK);
        mCoveredFadePaint.setAlpha(0);
        mCoveredFadeRect = new Rect();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setOnClickListener(OnClickCoverToClose());
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        setOnClickListener(null);
    }

    @Override
    public boolean onStartNestedScroll(View child,
                                       View target,
                                       int nestedScrollAxes) {
        if (mAnimSet != null) {
            mAnimSet.cancel();
        }
        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedPreScroll(View target,
                                  int dx,
                                  int dy,
                                  int[] consumed) {
        // If we're in a drag gesture and the user reverses up the we should
        // take those events
        if (mDraggingDown && dy > 0 || mDraggingUp && dy < 0) {
            Log.d("xyz", "  onNestedPreScroll");
            dragScale(dy);
            consumed[1] = dy;
        }
        super.onNestedPreScroll(target, dx, dy, consumed);
    }

    @Override
    public void onNestedScroll(View target,
                               int dxConsumed,
                               int dyConsumed,
                               int dxUnconsumed,
                               int dyUnconsumed) {
        Log.d("xyz", "    onNestedScroll");
        dragScale(dyUnconsumed);
        super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
    }

    @Override
    public void onStopNestedScroll(View child) {
        Log.d("xyz", "onStopNestedScroll");
        if (Math.abs(mTotalDrag) >= mDragDismissDistance) {
            close(CLOSE_BY_DRAG);
        } else {
            open();
        }
        super.onStopNestedScroll(child);
    }

    public void addListener(DragDismissCallback listener) {
        if (mCallbacks == null) {
            mCallbacks = new ArrayList<>();
        }
        mCallbacks.add(listener);
    }

    public void removeListener(DragDismissCallback listener) {
        if (mCallbacks != null && mCallbacks.size() > 0) {
            mCallbacks.remove(listener);
        }
    }

    public void removeAllListeners() {
        while (!mCallbacks.isEmpty()) {
            removeListener(mCallbacks.get(0));
        }
    }

    public boolean isOpened() {
        return mIsOpened;
    }

    /**
     * Open the layout with sliding up animation.
     */
    public void open() {
        // TODO: If the backport of the transition library is promising, then
        // TODO: I need to use it instead.
        final ObjectAnimator animTy = ObjectAnimator
            .ofFloat(mMovableChildView,
                     "translationY",
                     0);
        // Cover updater.
        animTy.addUpdateListener(mCoverUpdater);
        final ObjectAnimator animSx = ObjectAnimator
            .ofFloat(mMovableChildView,
                     "scaleX",
                     1.f);
        final ObjectAnimator animSy = ObjectAnimator
            .ofFloat(mMovableChildView,
                     "scaleY",
                     1.f);

        if (mAnimSet != null) {
            mAnimSet.cancel();
        }
        mAnimSet = new AnimatorSet();
        mAnimSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                // DO NOTHING.
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animTy.removeUpdateListener(mCoverUpdater);
                mAnimSet.removeListener(this);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                animTy.removeUpdateListener(mCoverUpdater);
                mAnimSet.removeListener(this);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // DO NOTHING.
            }
        });
        mAnimSet.playTogether(animTy, animSx, animSy);
        mAnimSet.setDuration(300L);
        mAnimSet.setInterpolator(new DecelerateInterpolator());
        mAnimSet.start();

        // Update the state.
        mTotalDrag = 0;
        mIsOpened = true;
        mDraggingDown = mDraggingUp = false;
        dispatchDragCallback(0f, 0f, 0f, 0f);
    }

    /**
     * Post call of the {@link #open()}.
     */
    public void postOpen() {
        getViewTreeObserver()
            .addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeGlobalOnLayoutListener(this);
                if (!isOpened()) {
                    open();
                }
            }
        });
    }

    /**
     * Close the layout with a vertical sliding animation. It calls
     * {@link DragDismissCallback#onBackPressedDismissed()} when the animation
     * ends.
     */
    public void close() {
        close(CLOSE_BY_BACK_PRESSED);
    }

    /**
     * Close the layout with a vertical sliding animation.
     *
     * @param closeByGesture Value of {@link #CLOSE_BY_BACK_PRESSED},
     *                       {@link #CLOSE_BY_COVER_PRESSED} or
     *                       {@link #CLOSE_BY_DRAG}.
     *                       Will call
     *                       {@link DragDismissCallback#onBackPressedDismissed()},
     *                       {@link DragDismissCallback#onCoverPressedDismissed()}
     *                       and
     *                       {@link DragDismissCallback#onDragDismissed()}
     *                       respectively.
     */
    public void close(int closeByGesture) {
        // TODO: If the backport of the transition library is promising, then
        // TODO: I need to use it instead.
        final ObjectAnimator animTy = ObjectAnimator
            .ofFloat(mMovableChildView,
                     "translationY",
                     (mTotalDrag <= 0 ?
                         mMovableChildView.getMeasuredHeight() :
                         -mMovableChildView.getMeasuredHeight()));
        // Cover updater.
        animTy.addUpdateListener(mCoverUpdater);

        if (mAnimSet != null) {
            mAnimSet.cancel();
        }
        mAnimSet = new AnimatorSet();
        mAnimSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                // DO NOTHING.
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animTy.removeUpdateListener(mCoverUpdater);
                mAnimSet.removeListener(this);

                dispatchDismissCallback();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                animTy.removeUpdateListener(mCoverUpdater);
                mAnimSet.removeListener(this);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // DO NOTHING.
            }
        });
        mAnimSet.playTogether(animTy);
        mAnimSet.setDuration(200L);
        mAnimSet.setInterpolator(new AccelerateInterpolator());
        mAnimSet.start();

        // Update the state.
        mTotalDrag = 0;
        mIsOpened = true;
        mClosedBy = closeByGesture;
        mDraggingDown = mDraggingUp = false;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    @Override
    protected void onFinishInflate() {
        // Called before #onMeasure().
        if (getChildCount() > 1) {
            throw new IllegalStateException("One child view is accepted!");
        }

        super.onFinishInflate();

        // Ensure the movable child list.
        mMovableChildView = getChildAt(0);

        // Very Important: setting this property to true to make the
        // view fill the visible area!
        if (mMovableChildView instanceof NestedScrollView) {
            NestedScrollView child = (NestedScrollView) mMovableChildView;
            child.setFillViewport(true);
        } else if (!(mMovableChildView instanceof NestedScrollingChild)) {
            throw new IllegalStateException(
                "The only child view must implement NestedScrollingChild.");
        }
    }

    @Override
    protected void onSizeChanged(int w,
                                 int h,
                                 int oldW,
                                 int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        if (mDragDismissFraction > 0f) {
            mDragDismissDistance = h * mDragDismissFraction;
        }

        // Set translation Y of the movable child view.
        if (!isInEditMode()) {
            ViewCompat.setTranslationY(mMovableChildView,
                                       mMovableChildView.getMeasuredHeight());
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec,
                             int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // TODO: Support drag padding.
    }

    @Override
    protected void onLayout(boolean changed,
                            int left,
                            int top,
                            int right,
                            int bottom) {
        // Layout children.
        super.onLayout(changed, left, top, right, bottom);

        // Set the cover boundary.
        if (changed) {
            mCoveredFadeRect.set(getLeft(),
                                 getTop(),
                                 getRight(),
                                 getBottom());
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        // FIXME: Figure out more proper place to draw the cover.
        // FIXME: Somehow the mBackground is not working.
        canvas.drawRect(mCoveredFadeRect, mCoveredFadePaint);

        super.dispatchDraw(canvas);
    }

    OnClickListener OnClickCoverToClose() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                close(CLOSE_BY_COVER_PRESSED);
            }
        };
    }

    void dragScale(int scroll) {
        if (scroll == 0) return;

        // Ensure the animation is cancelled.
        if (mAnimSet != null) {
            mAnimSet.cancel();
            mAnimSet = null;
        }

        mTotalDrag += scroll;

        // track the direction & set the pivot point for scaling
        // don't double track i.e. if start dragging down and then reverse,
        // keep tracking as dragging down until they reach the 'natural' position
        if (scroll < 0 && !mDraggingUp && !mDraggingDown) {
            mDraggingDown = true;
            if (mShouldScale) {
                ViewCompat.setPivotY(mMovableChildView, mMovableChildView.getHeight());
                ViewCompat.setPivotX(mMovableChildView, mMovableChildView.getWidth() / 2);
            }
        } else if (scroll > 0 && !mDraggingDown && !mDraggingUp) {
            mDraggingUp = true;
            if (mShouldScale) {
                ViewCompat.setPivotY(mMovableChildView, 0f);
                ViewCompat.setPivotX(mMovableChildView, mMovableChildView.getWidth() / 2);
            }
        }
        // how far have we dragged relative to the distance to perform a dismiss
        // (0–1 where 1 = dismiss distance). Decreasing logarithmically as we
        // approach the limit
        float dragFraction = (float) Math.log10(1 + (Math.abs(mTotalDrag) / mDragDismissDistance));

        // calculate the desired translation given the drag fraction
        float dragTo = dragFraction * mDragDismissDistance * mDragElasticity;

        if (mDraggingUp) {
            // as we use the absolute magnitude when calculating the drag
            // fraction, need to re-apply the drag direction
            dragTo *= -1;
        }

//        // Clamp dragTo to positive value (dragging down only).
//        dragTo = Math.max(0, dragTo);

        ViewCompat.setTranslationY(mMovableChildView, dragTo);

        if (mShouldScale) {
            final float scale = 1 - ((1 - mDragDismissScale) * dragFraction);
            ViewCompat.setScaleX(mMovableChildView, scale);
            ViewCompat.setScaleY(mMovableChildView, scale);
        }

        // if we've reversed direction and gone past the settle point then clear
        // the flags to allow the list to get the scroll events & reset any
        // transforms
        if ((mDraggingDown && mTotalDrag >= 0)
            || (mDraggingUp && mTotalDrag <= 0)) {
            mTotalDrag = dragTo = dragFraction = 0f;
            mDraggingDown = mDraggingUp = false;
            ViewCompat.setTranslationY(mMovableChildView, 0f);
            ViewCompat.setScaleX(mMovableChildView, 1f);
            ViewCompat.setScaleY(mMovableChildView, 1f);
        }

//        // Update the cover alpha.
//        final int h = mMovableChildView.getHeight();
//        final float ty = Math.abs(mMovableChildView.getTranslationY());
//        final int alpha = (int) Math.max(0, COVER_FADE_PAINT_ALPHA * (h - ty) / h);
//        mCoveredFadePaint.setAlpha(alpha);
//        invalidate();

        dispatchDragCallback(
            dragFraction, dragTo,
            Math.min(1f, Math.abs(mTotalDrag) / mDragDismissDistance), mTotalDrag);
    }

    void dispatchDragCallback(float elasticOffset,
                              float elasticOffsetPixels,
                              float rawOffset,
                              float rawOffsetPixels) {
        if (mCallbacks != null && !mCallbacks.isEmpty()) {
            for (DragDismissCallback callback : mCallbacks) {
                callback.onDrag(elasticOffset, elasticOffsetPixels,
                                rawOffset, rawOffsetPixels);
            }
        }
    }

    void dispatchDismissCallback() {
        if (mCallbacks != null && !mCallbacks.isEmpty()) {
            for (DragDismissCallback callback : mCallbacks) {
                switch (mClosedBy) {
                    case CLOSE_BY_BACK_PRESSED:
                        callback.onBackPressedDismissed();
                        break;
                    case CLOSE_BY_COVER_PRESSED:
                        callback.onCoverPressedDismissed();
                        break;
                    case CLOSE_BY_DRAG:
                    default:
                        callback.onDragDismissed();
                        break;
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    public interface DragDismissCallback {
        /**
         * Called for each drag event.
         *
         * @param elasticOffset       Indicating the drag offset with elasticity
         *                            applied i.e. may exceed 1.
         * @param elasticOffsetPixels The elastically scaled drag distance in
         *                            pixels.
         * @param rawOffset           Value from [0, 1] indicating the raw drag
         *                            offset i.e. without elasticity applied. A
         *                            value of 1 indicates that the dismiss
         *                            distance has been reached.
         * @param rawOffsetPixels     The raw distance the user has dragged.
         */
        void onDrag(float elasticOffset,
                    float elasticOffsetPixels,
                    float rawOffset,
                    float rawOffsetPixels);

        /**
         * Called when dragging is released and has exceeded the threshold
         * dismiss distance.
         */
        void onDragDismissed();

        /**
         * Dismissed by tapping on the back button.
         */
        void onBackPressedDismissed();

        /**
         * Dismissed by tapping on the cover.
         */
        void onCoverPressedDismissed();
    }

    /**
     * An {@link DragDismissCallback} which fades system chrome (i.e.
     * status bar and navigation bar) whilst elastic drags are performed and
     * {@link Activity#finishAfterTransition() finishes} the activity when drag
     * dismissed.
     */
    public static class SystemChromeFader implements DragDismissCallback {

        final Activity activity;
//        final int statusBarAlpha;
//        final int navBarAlpha;
//        final boolean fadeNavBar;

        public SystemChromeFader(Activity activity) {
            this.activity = activity;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            statusBarAlpha = Color.alpha(activity.getWindow().getStatusBarColor());
//            navBarAlpha = Color.alpha(activity.getWindow().getNavigationBarColor());
//            fadeNavBar = ViewUtils.isNavBarOnBottom(activity);
            } else {
//            statusBarAlpha = Color.alpha(activity.getWindow().getStatusBarColor());
//            navBarAlpha = Color.alpha(activity.getWindow().getNavigationBarColor());
//            fadeNavBar = ViewUtils.isNavBarOnBottom(activity);
            }
        }

        @Override
        public void onDrag(float elasticOffset,
                           float elasticOffsetPixels,
                           float rawOffset,
                           float rawOffsetPixels) {
//            if (elasticOffsetPixels > 0) {
//                // dragging downward, fade the status bar in proportion
//                activity.getWindow().setStatusBarColor(ColorUtils.modifyAlpha(activity.getWindow()
//                                                                                      .getStatusBarColor(), (int) ((1f - rawOffset) * statusBarAlpha)));
//            } else if (elasticOffsetPixels == 0) {
//                // reset
//                activity.getWindow().setStatusBarColor(ColorUtils.modifyAlpha(
//                    activity.getWindow().getStatusBarColor(), statusBarAlpha));
//                activity.getWindow().setNavigationBarColor(ColorUtils.modifyAlpha(
//                    activity.getWindow().getNavigationBarColor(), navBarAlpha));
//            } else if (fadeNavBar) {
//                // dragging upward, fade the navigation bar in proportion
//                activity.getWindow().setNavigationBarColor(
//                    ColorUtils.modifyAlpha(activity.getWindow().getNavigationBarColor(),
//                                           (int) ((1f - rawOffset) * navBarAlpha)));
//            }
            // Dummy callback.
        }

        public void onDragDismissed() {
            ActivityCompat.finishAfterTransition(activity);
        }

        @Override
        public void onBackPressedDismissed() {
            ActivityCompat.finishAfterTransition(activity);
        }

        @Override
        public void onCoverPressedDismissed() {
            ActivityCompat.finishAfterTransition(activity);
        }
    }
}
