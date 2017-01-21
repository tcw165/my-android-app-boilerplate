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
import android.content.Context;
import android.graphics.Canvas;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;
import java.util.Collection;

/**
 * ...
 * <br/>
 * Attributes:
 * <br/>
 * {@link R.styleable#ElasticDragDismissLayout_dragDismissDistance}
 * <br/>
 * {@link R.styleable#ElasticDragDismissLayout_dragDismissFraction}
 * <br/>
 * {@link R.styleable#ElasticDragDismissLayout_dragScale}
 * <br/>
 * {@link R.styleable#ElasticDragDismissLayout_dragElasticity}
 * <br/>
 */
public class ElasticDragMenuLayout
    extends ElasticDragDismissLayout {

    // State.
    boolean mIsMenuOpened;

    // View.
    View mMenuView;

    public ElasticDragMenuLayout(Context context,
                                 AttributeSet attrs) {
        super(context, attrs);

        // Always opened.
        mIsOpened = true;
        // Disable the drag-scale.
        mShouldScale = false;
    }

    @Override
    public boolean onStartNestedScroll(View child,
                                       View target,
                                       int nestedScrollAxes) {
        return super.onStartNestedScroll(child, target, nestedScrollAxes);
    }

    @Override
    public void onNestedPreScroll(View target,
                                  int dx,
                                  int dy,
                                  int[] consumed) {
        super.onNestedPreScroll(target, dx, dy, consumed);
    }

    @Override
    public void onNestedScroll(View target,
                               int dxConsumed,
                               int dyConsumed,
                               int dxUnconsumed,
                               int dyUnconsumed) {
        super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
    }

    /**
     * ...
     */
    @Override
    public void open() {
        resetScrollView(false);
    }

    /**
     * ...
     */
    @Override
    public void close(int closeByGesture) {
        // Ensure the gesture.
        mClosedBy = CLOSE_BY_DRAG;
        resetScrollView(false);
    }

    public boolean isMenuOpened() {
        return mIsMenuOpened;
    }

    public void openMenu() {
        mIsMenuOpened = true;
        resetScrollView(true);
    }

    public void closeMenu() {
        mIsMenuOpened = false;
        resetScrollView(false);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // TODO: Use layout params to configure it.
        // Ensure the movable child list.
//        mElasticScrollView = getChildAt(0);
        // TODO: Use layout params to configure it.
        mMenuView = getChildAt(getChildCount() - 1);

        if (mMenuView != null && !isInEditMode()) {
            ViewCompat.setAlpha(mMenuView, 0f);
        }
    }

    @Override
    protected void onSizeChanged(int w,
                                 int h,
                                 int oldW,
                                 int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);

        // Override the ty.
        ViewCompat.setTranslationY(mElasticScrollView, 0);
    }

    @Override
    protected boolean drawChild(Canvas canvas,
                                View child,
                                long drawingTime) {
        // TODO: Draw the cover before drawing the menu.
        if (isMenuOpened()) {
            // DO NOTHING.
        }
        return super.drawChild(canvas, child, drawingTime);
    }

    @Override
    void dispatchDismissCallback(float totalScroll) {
        if (mCallbacks != null && !mCallbacks.isEmpty()) {
            for (DragDismissCallback callback : mCallbacks) {
                callback.onDragDismissed(totalScroll);
            }
        }
    }

    void resetScrollView(boolean forceOpenMenu) {
        final float totalDrag = mTotalDrag;
        final boolean isOpenMenu = forceOpenMenu || Math.abs(mTotalDrag) > mDragDismissDistance;
        // TODO: If the backport of the transition library is promising, then
        // TODO: I need to use it instead.
        final Collection<Animator> anims = new ArrayList<>();
        anims.add(ObjectAnimator
                      .ofFloat(mElasticScrollView,
                               "translationY",
                               0f)
                      .setDuration(250L));
        anims.add(ObjectAnimator
                      .ofFloat(mMenuView,
                               "alpha",
                               isOpenMenu ? 1f : 0f)
                      .setDuration(300L));

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
                mAnimSet.removeListener(this);

                if (isOpenMenu) {
                    dispatchDismissCallback(totalDrag);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mAnimSet.removeListener(this);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // DO NOTHING.
            }
        });
        mAnimSet.playTogether(anims);
        mAnimSet.setInterpolator(new DecelerateInterpolator());
        mAnimSet.start();

        // Update the state.
        mTotalDrag = 0;
        mIsOpened = true;
        mIsMenuOpened = isOpenMenu;
        mDraggingDown = mDraggingUp = false;
        dispatchDragCallback(0f, 0f, 0f, 0f);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////
}
