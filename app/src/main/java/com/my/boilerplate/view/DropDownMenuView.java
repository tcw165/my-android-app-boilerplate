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

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.my.boilerplate.R;

public class DropDownMenuView extends FrameLayout implements INavMenu {

    protected boolean mIsShowing;
    protected int mFixedHeight;

    protected View mMenu;
    protected ImageView mBackground;

    protected OnClickListener mOnClickMenuItemListener;
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
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    public void setOnMenuStateChangeListener(OnMenuStateChange listener) {
        mOnMenuStateChangeListener = listener;
    }

    public void setOnClickMenuItemListener(OnClickListener listener) {
        if (listener == null) return;

        mOnClickMenuItemListener = listener;

        mMenu.findViewById(R.id.menu_my_collages)
             .setOnClickListener(mOnClickMenuItemListener);
        mMenu.findViewById(R.id.menu_store)
             .setOnClickListener(mOnClickMenuItemListener);
        mMenu.findViewById(R.id.menu_settings)
             .setOnClickListener(mOnClickMenuItemListener);
        mMenu.findViewById(R.id.menu_explore)
             .setOnClickListener(mOnClickMenuItemListener);
        mMenu.findViewById(R.id.menu_activity)
             .setOnClickListener(mOnClickMenuItemListener);
        mMenu.findViewById(R.id.menu_profile)
             .setOnClickListener(mOnClickMenuItemListener);
    }

    public boolean isShowing() {
        return mIsShowing;
    }

    public void showWithAnimation() {
        // TODO: Update the toolbar icon.
//        if (mBtnNavMenu != null) {
//            mBtnNavMenu.setSelected(true);
//            getActivity().invalidateOptionsMenu();
//        }

        // Update the status.
        mIsShowing = true;
        mMenu.setClickable(true);
        mBackground.setClickable(true);

        // Notify the listener.
        if (mOnMenuStateChangeListener != null) {
            mOnMenuStateChangeListener.onShowMenu();
        }

        ViewCompat
            .animate(mMenu)
            .translationY(0.f)
            .setDuration(300)
            .setInterpolator(new AccelerateDecelerateInterpolator())
            .start();
        ViewCompat
            .animate(mBackground)
            .alpha(1.f)
            .setDuration(300)
            .setInterpolator(new AccelerateDecelerateInterpolator())
            .start();
    }

    public void hideWithAnimation() {
        // TODO: Update the toolbar icon.

        // Update the status.
        mIsShowing = false;
        mMenu.setClickable(false);
        mBackground.setClickable(false);

        // Notify the listener.
        if (mOnMenuStateChangeListener != null) {
            mOnMenuStateChangeListener.onHideMenu();
        }

        ViewCompat
            .animate(mMenu)
            .translationY(-mMenu.getHeight())
            .setDuration(200)
            .setInterpolator(new AccelerateInterpolator())
            .start();
        ViewCompat
            .animate(mBackground)
            .alpha(0.f)
            .setDuration(200)
            .setInterpolator(new AccelerateInterpolator())
            .start();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    protected void initAttrs(Context context, AttributeSet attrs) {
//        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ArrowNavToolbar);
//
//        try {
//            switch (a.getInt(R.styleable.ArrowNavToolbar_titleGravity, 0)) {
//                case 0:
//                    mTitleGravity = Gravity.CENTER;
//                    break;
//                case 1:
//                    mTitleGravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
//                    break;
//                case 2:
//                    mTitleGravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
//                    break;
//                default:
//                    throw new IllegalArgumentException("Unsupported titleGravity value.");
//            }
//        } finally {
//            a.recycle();
//        }
    }

    protected void initView(Context context) {
        // The main layout.
        inflate(context, R.layout.drop_down_menu, this);

        mIsShowing = false;

        mMenu = findViewById(R.id.menu);
        mFixedHeight =  mMenu.getLayoutParams().height;
        mBackground = (ImageView) findViewById(R.id.menu_background);
        mBackground.setOnClickListener(onClickBackground());

        // Setup the menu items.
        setupIconAndCaption(mMenu.findViewById(R.id.menu_my_collages), 0, 0);
        setupIconAndCaption(mMenu.findViewById(R.id.menu_store), 0, 0);
        setupIconAndCaption(mMenu.findViewById(R.id.menu_settings), 0, 0);
        setupIconAndCaption(mMenu.findViewById(R.id.menu_explore), 0, 0);
        setupIconAndCaption(mMenu.findViewById(R.id.menu_activity), 0, 0);
        setupIconAndCaption(mMenu.findViewById(R.id.menu_profile), 0, 0);
    }

    protected void setupIconAndCaption(View view, @DrawableRes int icon, @StringRes int str) {
        if (icon != 0) {
            ((ImageView) view.findViewById(R.id.icon)).setImageResource(icon);
        }
        if (str != 0) {
            ((TextView) view.findViewById(R.id.name)).setText(str);
        }
    }

    protected OnClickListener onClickBackground() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideWithAnimation();
            }
        };
    }
}
