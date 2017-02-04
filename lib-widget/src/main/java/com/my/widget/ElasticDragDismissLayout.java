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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link CoordinatorLayout} which responds to nested scrolls to create drag-
 * dismissable layouts. Applies an elasticity factor to reduce movement as you
 * approach the given dismiss distance.
 * Optionally also scales down content during drag.
 * <br/>
 * Attributes:
 * see {@link ElasticDragLayout}
 */
public class ElasticDragDismissLayout extends ElasticDragLayout {

    /**
     * See {@link #dispatchDismissCallback(int, float)}
     */
    public static final int CLOSED_BY_DRAG_OVER = 0;
    /**
     * See {@link #dispatchDismissCallback(int, float)}
     */
    public static final int CLOSED_BY_BACK_PRESSED = 1;
    /**
     * See {@link #dispatchDismissCallback(int, float)}
     */
    public static final int CLOSED_BY_COVER_PRESSED = 2;

    // State
    boolean mIsOpened;

    // Background.
    Paint mBgFadePaint;
    Rect mBgFadeRect;
    static final int BG_FADE_PAINT_ALPHA = (int) (0.33 * 0xFF);

    // Callbacks.
    List<OnDragDismissCallback> mDismissCallbacks;

    // Animation.
    AnimatorUpdateListener mBgFadeUpdater = new AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            if (!(mElasticScrollView instanceof NestedScrollingChild)) return;

            final int h = mElasticScrollView.getHeight();
            final float ty = Math.abs(mElasticScrollView.getTranslationY());
            final int alpha = (int) Math.max(0, BG_FADE_PAINT_ALPHA * (h - ty) / h);

            mBgFadePaint.setAlpha(alpha);
//            Log.d("xyz", "bg alpha=" + mBgFadePaint.getAlpha());

            invalidate();
        }
    };

    public ElasticDragDismissLayout(Context context,
                                    AttributeSet attrs) {
        super(context, attrs);

        mBgFadePaint = new Paint();
        mBgFadePaint.setColor(Color.BLACK);
        mBgFadePaint.setAlpha(0);
        mBgFadeRect = new Rect();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setOnClickListener(OnClickBgToDismiss());
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        setOnClickListener(null);
    }

    public boolean isOpened() {
        return mIsOpened;
    }

    /**
     * Open the layout with sliding up animation.
     */
    public void open() {
        if (!(mElasticScrollView instanceof NestedScrollingChild)) return;

        // TODO: If the backport of the transition library is promising, then
        // TODO: I need to use it instead.
        final ObjectAnimator animTy = ObjectAnimator
            .ofFloat(mElasticScrollView, "translationY", 0);
        // Cover updater.
        animTy.addUpdateListener(mBgFadeUpdater);
        final ObjectAnimator animSx = ObjectAnimator
            .ofFloat(mElasticScrollView, "scaleX", 1.f);
        final ObjectAnimator animSy = ObjectAnimator
            .ofFloat(mElasticScrollView, "scaleY", 1.f);
        // TODO: Handle scroll-view header and footer.

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
                animTy.removeUpdateListener(mBgFadeUpdater);
                mAnimSet.removeListener(this);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                animTy.removeUpdateListener(mBgFadeUpdater);
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
        mIsOpened = true;
    }

    /**
     * Post call of the {@link #open()}.
     */
    public void postOpen() {
        getViewTreeObserver()
            .addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (!getViewTreeObserver().isAlive()) return;

                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    if (!isOpened()) {
                        open();
                    }
                }
            });
    }

    /**
     * Close the layout with a vertical sliding animation. It calls
     * {@link OnDragDismissCallback#onDismissByBackPressed()} when the animation
     * ends.
     */
    public void close() {
        close(CLOSED_BY_BACK_PRESSED);
    }

    /**
     * Close the layout with a vertical sliding animation.
     *
     * @param closeByGesture Value of {@link #CLOSED_BY_BACK_PRESSED},
     *                       {@link #CLOSED_BY_COVER_PRESSED} or
     *                       {@link #CLOSED_BY_DRAG_OVER}.
     *                       Will call
     *                       {@link OnDragDismissCallback#onDismissByBackPressed()},
     *                       {@link OnDragDismissCallback#onDismissByBgPressed()}
     *                       and
     *                       {@link OnDragDismissCallback#onDismissByDragOver(float)}
     *                       respectively.
     */
    public void close(final int closeByGesture) {
        if (!(mElasticScrollView instanceof NestedScrollingChild)) {
            dispatchDismissCallback(closeByGesture, 0);
            return;
        }

        final float totalDrag = mTotalDrag;
        // TODO: If the backport of the transition library is promising, then
        // TODO: I need to use it instead.
        final ObjectAnimator animTy = ObjectAnimator
            .ofFloat(mElasticScrollView,
                     "translationY",
                     (totalDrag <= 0 ?
                         mElasticScrollView.getMeasuredHeight() :
                         -mElasticScrollView.getMeasuredHeight()));
        // Cover updater.
        animTy.addUpdateListener(mBgFadeUpdater);
        // TODO: Handle scroll-view header and footer.

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
                animTy.removeUpdateListener(mBgFadeUpdater);
                mAnimSet.removeListener(this);

                // Dispatch the dismiss callback here when the animation
                // finishes.
                dispatchDismissCallback(closeByGesture, totalDrag);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                animTy.removeUpdateListener(mBgFadeUpdater);
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
        mIsOpened = false;
    }

    public void addOnDragDismissListener(OnDragDismissCallback listener) {
        if (mDismissCallbacks == null) {
            mDismissCallbacks = new ArrayList<>();
        }
        mDismissCallbacks.add(listener);
    }

    public void removeOnDragDismissListener(OnDragDismissCallback listener) {
        if (mDismissCallbacks != null && mDismissCallbacks.size() > 0) {
            mDismissCallbacks.remove(listener);
        }
    }

    public void removeAllOnDragDismissListeners() {
        while (!mDismissCallbacks.isEmpty()) {
            removeOnDragDismissListener(mDismissCallbacks.get(0));
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    @Override
    protected void onLayout(boolean changed,
                            int left,
                            int top,
                            int right,
                            int bottom) {
        // Layout children.
        super.onLayout(changed, left, top, right, bottom);

        // Set translation Y of the movable child view.
        if (!isInEditMode() &&
            mElasticScrollView instanceof NestedScrollingChild &&
            changed && !isOpened()) {
            ViewCompat.setTranslationY(mElasticScrollView,
                                       mElasticScrollView.getMeasuredHeight());
        }
        // TODO: Handle scroll-view header and footer.

        // Set the cover boundary.
        if (changed) {
            mBgFadeRect.set(getLeft(),
                            getTop(),
                            getRight(),
                            getBottom());
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        // FIXME: Figure out more proper place to draw the cover.
        // FIXME: Somehow the mBackground is not working.
        canvas.drawRect(mBgFadeRect, mBgFadePaint);

        super.dispatchDraw(canvas);
    }

    @Override
    protected void onDragOver(float totalDrag) {
        close(CLOSED_BY_DRAG_OVER);
    }

    @Override
    protected void onDragCancel() {
        open();
    }

    OnClickListener OnClickBgToDismiss() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                close(CLOSED_BY_COVER_PRESSED);
            }
        };
    }

    void dispatchDismissCallback(int gesture,
                                 float totalDrag) {
        if (mDismissCallbacks != null && !mDismissCallbacks.isEmpty()) {
            for (OnDragDismissCallback callback : mDismissCallbacks) {
                switch (gesture) {
                    case CLOSED_BY_BACK_PRESSED:
                        callback.onDismissByBackPressed();
                        break;
                    case CLOSED_BY_COVER_PRESSED:
                        callback.onDismissByBgPressed();
                        break;
                    case CLOSED_BY_DRAG_OVER:
                    default:
                        callback.onDismissByDragOver(totalDrag);
                        break;
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    public interface OnDragDismissCallback {
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
        void onDismissByDragOver(float totalScroll);

        /**
         * Dismissed by tapping on the back button.
         */
        void onDismissByBackPressed();

        /**
         * Dismissed by tapping on the background area.
         */
        void onDismissByBgPressed();
    }

    /**
     * An {@link OnDragDismissCallback} which fades system chrome (i.e.
     * status bar and navigation bar) whilst elastic drags are performed and
     * {@link Activity#finishAfterTransition() finishes} the activity when drag
     * dismissed.
     */
    public static class SystemChromeFader implements OnDragDismissCallback {

        public final Activity activity;
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

        public void onDismissByDragOver(float totalScroll) {
            ActivityCompat.finishAfterTransition(activity);
        }

        @Override
        public void onDismissByBackPressed() {
            ActivityCompat.finishAfterTransition(activity);
        }

        @Override
        public void onDismissByBgPressed() {
            ActivityCompat.finishAfterTransition(activity);
        }
    }
}
