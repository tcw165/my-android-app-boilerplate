// Copyright (c) 2016 boyw165
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
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.my.boilerplate.R;

import java.util.ArrayList;
import java.util.List;

@CoordinatorLayout.DefaultBehavior(DropDownMenuView.DrawerBehavior.class)
public class DropDownMenuView extends FrameLayout implements INavMenu {

    protected boolean mIsShowing;

    protected int mMenuBgColor;
    protected int mDownArrowBgColor;
    protected float mMenuHeight;
    protected float mDownArrowHeight;
    protected AnimatorSet mAnimatorSet;

    protected View mMenu;
    protected List<SquaredMenuItemView> mMenuItems;
    protected ImageView mOverlayBackground;
    protected View mDownArrowContainer;
    protected View mDownArrowStart;
    protected View mDownArrowEnd;

    /**
     * This is drop-down menu, it consumes the unconsumed dy from the anchor
     * view. It also animates the anchor view.
     */
    protected View mAnchorView;

    protected OnMenuStateChange mOnMenuStateChangeListener;

    public DropDownMenuView(Context context) {
        super(context);

        initView(context);
    }

    public DropDownMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initAttrs(context, attrs);
        initView(context);
    }

    @Override
    public void setOnMenuStateChangeListener(OnMenuStateChange listener) {
        mOnMenuStateChangeListener = listener;
    }

    public void setOnClickMenuItemListener(OnClickListener listener) {
        if (listener == null) return;

        for (int i = 0; i < mMenuItems.size(); ++i) {
            mMenuItems.get(i).setOnClickListener(listener);
        }
    }

    public void setHasNotification(int itemPosition, boolean hasNoti) {
        if (itemPosition < 0 || itemPosition >= mMenuItems.size()) return;

        SquaredMenuItemView menuItem = mMenuItems.get(itemPosition);
        menuItem.setHasNotificationBadge(hasNoti);
    }

    public boolean isShowing() {
        return mIsShowing;
    }

    public void showWithAnimation() {
        // Update the status.
        mIsShowing = true;
        mMenu.setClickable(true);
        mOverlayBackground.setClickable(true);

        // Notify the listener.
        if (mOnMenuStateChangeListener != null) {
            mOnMenuStateChangeListener.onShowMenu();
        }

        if (mAnimatorSet != null) {
            mAnimatorSet.cancel();
        }

        // The duration is proportional to the translationY.
        float ty = ViewCompat.getTranslationY(mMenu);
        int BASE_DURATION = 300;
        int duration = (int) ((float) BASE_DURATION * Math.abs(ty) / getScrollTargetHeight());

        ObjectAnimator menuFgAnim = ObjectAnimator.ofFloat(mMenu, "translationY", 0.f);
        menuFgAnim.setDuration(duration);
        menuFgAnim.setInterpolator(new AccelerateDecelerateInterpolator());

        ObjectAnimator downArrowAnim = ObjectAnimator.ofFloat(mDownArrowContainer, "alpha", 0.f);
        downArrowAnim.setDuration(duration);
        downArrowAnim.setInterpolator(new AccelerateDecelerateInterpolator());

        ObjectAnimator menuBgAnim = ObjectAnimator.ofFloat(mOverlayBackground, "alpha", 1.f);
        menuBgAnim.setDuration(BASE_DURATION);
        menuBgAnim.setInterpolator(new AccelerateDecelerateInterpolator());

        // Animate the anchor view.
        ObjectAnimator anchorViewAnim = ObjectAnimator.ofFloat(mAnchorView, "translationY", 0.f);
        anchorViewAnim.setDuration(BASE_DURATION);
        anchorViewAnim.setInterpolator(new AccelerateDecelerateInterpolator());

        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.playTogether(menuFgAnim, downArrowAnim, menuBgAnim, anchorViewAnim);
        mAnimatorSet.start();
    }

    public void hideWithAnimation() {
        // TODO: Update the toolbar icon.

        // Update the status.
        mIsShowing = false;
        mMenu.setClickable(false);
        mOverlayBackground.setClickable(false);

        // Notify the listener.
        if (mOnMenuStateChangeListener != null) {
            mOnMenuStateChangeListener.onHideMenu();
        }

        if (mAnimatorSet != null) {
            mAnimatorSet.cancel();
        }

        // The duration is proportional to the translationY.
        float ty = ViewCompat.getTranslationY(mMenu);
        int BASE_DURATION = 200;
        int duration = (int) ((float) BASE_DURATION * (getScrollTargetHeight() + 0.5 * ty) / getScrollTargetHeight());

        ObjectAnimator menuFgAnim = ObjectAnimator.ofFloat(mMenu, "translationY", -mMenuHeight);
        menuFgAnim.setDuration(duration);
        menuFgAnim.setInterpolator(new AccelerateDecelerateInterpolator());

        ObjectAnimator downArrowAnim = ObjectAnimator.ofFloat(mDownArrowContainer, "alpha", 0.f);
        downArrowAnim.setDuration(duration);
        downArrowAnim.setInterpolator(new AccelerateDecelerateInterpolator());

        ObjectAnimator menuBgAnim = ObjectAnimator.ofFloat(mOverlayBackground, "alpha", 0.f);
        menuBgAnim.setDuration(BASE_DURATION);
        menuBgAnim.setInterpolator(new AccelerateDecelerateInterpolator());

        // Animate the anchor view.
        ObjectAnimator anchorViewAnim = ObjectAnimator.ofFloat(mAnchorView, "translationY", 0.f);
        anchorViewAnim.setDuration(duration);
        anchorViewAnim.setInterpolator(new AccelerateDecelerateInterpolator());

        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.playTogether(menuFgAnim, downArrowAnim, menuBgAnim, anchorViewAnim);
        mAnimatorSet.start();
    }

    void setAnchorView(View anchorView) {
        mAnchorView = anchorView;
    }

    /**
     * Translate the target view with delta y pixels.
     *
     * @param deltaTy The delta translation y.
     */
    void scroll(float deltaTy) {
        // Cancel the animation.
        if (mAnimatorSet != null) {
            mAnimatorSet.cancel();
        }

        // Constrain the value.
        float ty = constraintTranslationY(mMenu.getTranslationY() - deltaTy);
        float appearingHeight = getScrollTargetHeight() + ty;

        ViewCompat.setTranslationY(mMenu, ty);
        // Force to show the down-arrow container.
        ViewCompat.setAlpha(mDownArrowContainer, 1.f);
        ViewCompat.setAlpha(mDownArrowEnd, appearingHeight / getScrollTargetHeight());
        // Animate the anchor view.
        ViewCompat.setTranslationY(mAnchorView, appearingHeight);
    }

    float constraintTranslationY(float ty) {
        // The minimum translation y of the target view.
        final float minOffset = -getScrollTargetHeight();
        // The maximum translation y of the target view.
        final float maxOffset = 0;

        if (ty < minOffset) {
            ty = minOffset;
        } else if (ty > maxOffset) {
            ty = maxOffset;
        }

        return ty;
    }

    View getScrollTargetView() {
        return mMenu;
    }

    float getScrollTargetHeight() {
        return mMenuHeight;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    protected void initAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DropDownMenuView);

        try {
            mMenuBgColor = a.getColor(R.styleable.DropDownMenuView_menuBackgroundColor, Color.TRANSPARENT);
            mDownArrowBgColor = a.getColor(R.styleable.DropDownMenuView_downArrowBackgroundColor, Color.TRANSPARENT);
        } finally {
            a.recycle();
        }
    }

    protected void initView(Context context) {
        // The main layout.
        inflate(context, R.layout.drop_down_menu, this);

        mIsShowing = false;

        mMenu = findViewById(R.id.menu_container);
        mMenu.findViewById(R.id.menu_body).setBackgroundColor(mMenuBgColor);
        // The menu height is determined when the onLayout() is called.
        mMenu.addOnLayoutChangeListener(onMenuLayoutChange());

        mDownArrowContainer = findViewById(R.id.down_arrow_container);
        mDownArrowContainer.setBackgroundColor(mDownArrowBgColor);
        mDownArrowStart = findViewById(R.id.down_arrow_start);
        mDownArrowEnd = findViewById(R.id.down_arrow_end);
        mDownArrowHeight = mDownArrowContainer.getLayoutParams().height;

        mOverlayBackground = (ImageView) findViewById(R.id.menu_overlay_background);
        mOverlayBackground.setOnClickListener(onClickBackground());
        // This is necessary because setting the onClick listener will enable
        // it.
        mOverlayBackground.setClickable(false);

        // Setup the menu items.
        mMenuItems = new ArrayList<>();
        mMenuItems.add((SquaredMenuItemView) mMenu.findViewById(R.id.menu_1));
        mMenuItems.add((SquaredMenuItemView) mMenu.findViewById(R.id.menu_2));
        mMenuItems.add((SquaredMenuItemView) mMenu.findViewById(R.id.menu_3));
        mMenuItems.add((SquaredMenuItemView) mMenu.findViewById(R.id.menu_4));
        mMenuItems.add((SquaredMenuItemView) mMenu.findViewById(R.id.menu_5));
        mMenuItems.add((SquaredMenuItemView) mMenu.findViewById(R.id.menu_6));
    }

    protected OnClickListener onClickBackground() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideWithAnimation();
            }
        };
    }

    protected OnLayoutChangeListener onMenuLayoutChange() {
        return new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v,
                                       int left,
                                       int top,
                                       int right,
                                       int bottom,
                                       int oldLeft,
                                       int oldTop,
                                       int oldRight,
                                       int oldBottom) {
                mMenuHeight = bottom - top;
                ViewCompat.setTranslationY(mMenu, -mMenuHeight);
            }
        };
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    /**
     * The view with this behavior will hide itself if the anchor view is
     * receiving a scroll-down event; will show up if the anchor view is
     * receiving a scroll-up event.
     */
    public static class DrawerBehavior
        extends CoordinatorLayout.Behavior<DropDownMenuView> {

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
                                           DropDownMenuView child,
                                           View directTargetChild,
                                           View target,
                                           int nestedScrollAxes) {
            if (layoutDependsOn(parent, child, directTargetChild)) {
                Log.d("xyz", "start the nested scroll.");
            }
            return layoutDependsOn(parent, child, directTargetChild);
        }

        @Override
        public void onNestedScroll(CoordinatorLayout parent,
                                   DropDownMenuView child,
                                   View anchorView,
                                   int dxConsumed,
                                   int dyConsumed,
                                   int dxUnconsumed,
                                   int dyUnconsumed) {
            Log.d("xyz", String.format("dyConsumed=%d, dyUnconsumed=%d", dyConsumed, dyUnconsumed));
            if (dyConsumed > 0) {
                child.scroll(dyConsumed);
            } else {
                child.scroll(dyUnconsumed);
            }
        }

        @Override
        public void onStopNestedScroll(CoordinatorLayout parent,
                                       DropDownMenuView child,
                                       View anchorView) {
            // Check if the translation is over a threshold, hide it or show it.
            float ty = Math.abs(child.getScrollTargetView().getTranslationY());
            Log.d("xyz", String.format("stop the nested scroll, ty=%f", ty));
            float height = child.getScrollTargetHeight();
            // When the anchor view is scrolling up.
            if ((height - ty) / height > SHOW_THRESHOLD) {
                // Show it.
                child.showWithAnimation();
            } else {
                // Hide it.
                child.hideWithAnimation();
            }
        }

        @Override
        public boolean layoutDependsOn(CoordinatorLayout parent,
                                       DropDownMenuView child,
                                       View anchorView) {
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) child.getLayoutParams();

            // Check the anchor attribute.
            if (params != null &&
                anchorView.getId() == params.getAnchorId()) {
                // Set the anchor view.
                child.setAnchorView(anchorView);
                return true;
            } else {
                return false;
            }
        }
    }
}
