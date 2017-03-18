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
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.widget.Checkable;

/**
 * For displaying photos in a photo picker.
 */
public class CheckableImageView
    extends AppCompatImageView
    implements Checkable {

    /**
     * Enable the checkable mode and change the UI.
     * <br/>
     * By default is false.
     */
    private boolean mIsCheckable;
    /**
     * Check the view like checkbox.
     * <br/>
     * By default is false.
     */
    private boolean mIsChecked;
    /**
     * The value of width over height. Zero indicates uncertain aspect-ratio.
     * The {@link #mFixedDimension} is valid when the aspect-ratio is not
     * zero.
     * <br/>
     * By default is 0.
     */
    private float mAspectRatio;
    /**
     * 0 indicates the fixed dimension is width;
     * <br/>
     * 1 indicates the fixed dimension is height;
     * <br/>
     * By default is 0.
     */
    private int mFixedDimension;

    // TODO: Make them drawable.
    // Sub-views.
    private Drawable mBorderDrawable;
    private Drawable mCheckboxDrawable;

    public CheckableImageView(Context context) {
        this(context, null);
    }

    public CheckableImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CheckableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // Determine the attributes.
        final TypedArray array = context.obtainStyledAttributes(
            attrs, R.styleable.CheckableView, 0, 0);
        mIsChecked = array.getBoolean(R.styleable.CheckableView_isChecked, false);
        mIsCheckable = array.getBoolean(R.styleable.CheckableView_checkable, false);
        mAspectRatio = array.getFloat(R.styleable.CheckableView_aspectRatio, 0.f);
        mFixedDimension = array.getInt(R.styleable.CheckableView_fixedDimension, 0);
        array.recycle();

        // For the IDE preview, show the checkable state.
        if (isInEditMode()) {
            mIsCheckable = true;
        }

        // The border drawable.
        mBorderDrawable = ContextCompat.getDrawable(
            context, R.drawable.bg_bordered_rect_55c3c5);
        // The checkbox drawable.
        mCheckboxDrawable = ContextCompat.getDrawable(
            context, R.drawable.icon_checkmark_55c3c5);
    }

    @SuppressWarnings("unused")
    public void setCheckable(boolean checkable) {
        final boolean isChanged = mIsCheckable != checkable;

        mIsCheckable = checkable;

        // With reference to the design, no UI change when it is checkable.
        // Update the border and checkbox.
        if (!checkable) {
            invalidate();
        }
    }

    @SuppressWarnings("unused")
    public boolean isCheckable() {
        return mIsCheckable;
    }

    @Override
    public void setChecked(boolean checked) {
        if (!mIsCheckable) return;

        mIsChecked = checked;

        // Update the border and checkbox.
        invalidate();
    }

    @Override
    public boolean isChecked() {
        return mIsCheckable && mIsChecked;
    }

    @Override
    public void toggle() {
        if (!mIsCheckable) return;

        setChecked(!isChecked());
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    @Override
    protected void onMeasure(int widthSpec,
                             int heightSpec) {
        if (mAspectRatio == 0 || mFixedDimension >= 2) {
            super.onMeasure(widthSpec, heightSpec);
        } else {
            // We ignored padding or many other cases.
            int width = MeasureSpec.getSize(widthSpec);
            int height = MeasureSpec.getSize(heightSpec);
            // Determine the fixed dimension and calculate the other.
            // See {@link R.styleable#CheckableCardView_fixedDimension}
            if (mFixedDimension == 0) {
                // Fixed width.
                height = (int) (width / mAspectRatio);
            } else if (mFixedDimension == 1) {
                // Fixed height.
                width = (int) (height * mAspectRatio);
            }

            // Setting MeasureSpec.EXACTLY is necessary to fix the size.
            super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        }
    }

    @Override
    protected void onLayout(boolean changed,
                            int left,
                            int top,
                            int right,
                            int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        final float density = getContext().getResources().getDisplayMetrics().density;

        // Ensure the border size.
        mBorderDrawable.setBounds(getPaddingLeft(),
                                  getPaddingTop(),
                                  getWidth() - getPaddingRight(),
                                  getHeight() - getPaddingBottom());
        // Ensure the checkbox size.
        final int size = (int) (density * 24);
        mCheckboxDrawable.setBounds(getWidth() - getPaddingRight() - size,
                                    getPaddingTop(),
                                    getWidth() - getPaddingRight(),
                                    getPaddingTop() + size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw the border and the checkbox.
        if (isChecked()) {
            if (getImageMatrix() == null &&
                getPaddingLeft() == 0 && getPaddingTop() == 0) {

                mBorderDrawable.draw(canvas);
            } else {
                final int saveCount = canvas.getSaveCount();
                canvas.save();

                if (getCropToPadding()) {
                    final int scrollX = getScrollX();
                    final int scrollY = getScrollY();
                    canvas.clipRect(scrollX + getPaddingLeft(),
                                    scrollY + getPaddingTop(),
                                    scrollX + getRight() - getLeft() - getPaddingRight(),
                                    scrollY + getBottom() - getTop() - getPaddingBottom());
                }

                canvas.translate(getPaddingLeft(), getPaddingTop());
                if (getImageMatrix() != null) {
                    canvas.concat(getImageMatrix());
                }
                mBorderDrawable.draw(canvas);
                mCheckboxDrawable.draw(canvas);
                canvas.restoreToCount(saveCount);
            }
        }
    }
}
