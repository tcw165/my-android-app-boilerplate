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
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;
import java.util.Collection;

/**
 * ...
 * Attributes:
 * see {@link ElasticDragLayout}
 */
public class ElasticDragMenuLayout extends ElasticDragLayout {

    // State.
    boolean mIsMenuOpened;

    // View.
    View mMenuView;

    public ElasticDragMenuLayout(Context context,
                                 AttributeSet attrs) {
        super(context, attrs);

        // Disable the drag-scale.
        mShouldScale = false;
    }

    public boolean isMenuOpened() {
        return mIsMenuOpened;
    }

    public void openMenu() {
        doAnimation(mIsMenuOpened = true);
    }

    public void closeMenu() {
        doAnimation(mIsMenuOpened = false);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // TODO: Use layout params to configure it.
        mMenuView = getChildAt(getChildCount() - 1);

        if (mMenuView != null && !isMenuOpened() &&
            !isInEditMode()) {
            ViewCompat.setAlpha(mMenuView, 0f);
        }
    }

    @Override
    protected void onDragCancel() {
        closeMenu();
    }

    @Override
    protected void onDragOver(float totalDrag) {
        openMenu();
    }

    void doAnimation(final boolean isOpenMenu) {
        // TODO: If the backport of the transition library is promising, then
        // TODO: I need to use it instead.
        final Collection<Animator> anims = new ArrayList<>();
        anims.add(ObjectAnimator
                      .ofFloat(mElasticScrollView, "translationY", 0f)
                      .setDuration(250L));
        anims.add(ObjectAnimator
                      .ofFloat(mMenuView, "alpha", isOpenMenu ? 1f : 0f)
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
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////
}
