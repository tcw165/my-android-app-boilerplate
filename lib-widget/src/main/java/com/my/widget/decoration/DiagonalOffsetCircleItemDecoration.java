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

package com.my.widget.decoration;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

/**
 * The decoration for the circle item. The rows would be shifted forward like
 * diagonally if the orientation is {@link LinearLayout#HORIZONTAL}. The column
 * would be shifted forward a bit if the orientation is
 * {@link LinearLayout#VERTICAL}.
 * <br/>
 * For example:
 * <pre>
 * Given orientation is {@link LinearLayout#HORIZONTAL}:
 *   + + + + + + ....
 *    + + + + + + ...
 *   + + + + + + ....
 *
 * Given orientation is {@link LinearLayout#VERTICAL}:
 *   .--.      .--.
 *   |  | .--. |  |
 *   '--' |  | '--'
 *   .--. '--' .--.
 *   |  |      |  |
 *   '--' .--. '--'
 *   .--. |  | .--.
 *   |  | '--' |  |
 *   '--'      '--'
 *    :    :    :
 *    :    :    :
 * </pre>
 */
public class DiagonalOffsetCircleItemDecoration extends RecyclerView.ItemDecoration {

    final private int mOrientation;
    final private int mSpanCount;
    final private int mPaddingPx;
    final private int mHalfPaddingPx;

    private int mItemSize;

    /**
     * @param orientation {@link LinearLayout#HORIZONTAL} |
     *                    {@link LinearLayout#VERTICAL}
     * @param spanCount   If orientation is vertical, spanCount is number of
     *                    columns. If orientation is horizontal, spanCount is
     *                    number of rows.
     * @param paddingPx   The padding (in pixel) in between items.
     */
    public DiagonalOffsetCircleItemDecoration(int orientation,
                                              int spanCount,
                                              int paddingPx) {
        mOrientation = orientation;
        mPaddingPx = paddingPx;
        mHalfPaddingPx = paddingPx / 2;
        mSpanCount = spanCount;
    }

    @Override
    public void getItemOffsets(Rect outRect,
                               View view,
                               RecyclerView parent,
                               RecyclerView.State state) {
        if (mOrientation == LinearLayout.HORIZONTAL) {
            // Horizontal.
            getHorizontalItemOffsets(outRect, view, parent, state);
        } else if (mOrientation == LinearLayout.VERTICAL) {
            // Vertical.
            getVerticalItemOffsets(outRect, view, parent, state);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    private void getHorizontalItemOffsets(Rect outRect,
                                          View view,
                                          RecyclerView parent,
                                          RecyclerView.State state) {
        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
        final int position = params.getViewAdapterPosition();
        final int count = parent.getAdapter().getItemCount();

        // Ensure the item size.
        if (mItemSize == 0) {
            final int totalHeight = parent.getHeight() - parent.getPaddingTop() - parent.getPaddingBottom();
            mItemSize = (totalHeight - (mSpanCount + 1) * mPaddingPx) / mSpanCount;
        }

        if (position < mSpanCount) {
            // Group left most.
            //  +  | + + + + + ....
            //   + |  + + + + + ...
            //  +  | + + + + + ....

            // Left-right.
            if (position % 2 == 0) {
                outRect.left = mPaddingPx;
                outRect.right = mHalfPaddingPx;
            } else {
                outRect.left = mPaddingPx + mItemSize / 2;
                outRect.right = mHalfPaddingPx;
            }

            // Top-down.
            if (position == 0) {
                // Top most one.
                outRect.top = mPaddingPx;
                outRect.bottom = 0;
            } else if (position == mSpanCount - 1) {
                // Bottom most one.
                outRect.top = 0;
                outRect.bottom = mPaddingPx;
            } else {
                outRect.top = 0;
                outRect.bottom = 0;
            }
        } else if (position >= mSpanCount * ((count - 1) / mSpanCount)) {
            // Group right most.
            //  ... + + + + +  | +
            //  .... + + + + + |  +
            //  ... + + + + +  | +

            final int spanPosition = position % mSpanCount;

            // Left-right.
            outRect.left = mHalfPaddingPx;
            outRect.right = mPaddingPx;

            // Top-down.
            if (spanPosition == 0) {
                // Top most ones.
                outRect.top = mPaddingPx;
                outRect.bottom = 0;
            } else if (spanPosition == mSpanCount - 1) {
                // Bottom most ones.
                outRect.top = 0;
                outRect.bottom = mPaddingPx;
            } else {
                outRect.top = 0;
                outRect.bottom = 0;
            }
        } else {
            // Group in the middle.
            //  +  | ... + + + + + .... | +
            //   + | .... + + + + + ... |  +
            //  +  | ... + + + + + .... | +

            final int spanPosition = position % mSpanCount;

            // Left-right.
            outRect.left = mHalfPaddingPx;
            outRect.right = mHalfPaddingPx;

            // Top-down.
            if (spanPosition == 0) {
                // Top most ones.
                outRect.top = mPaddingPx;
                outRect.bottom = 0;
            } else if (spanPosition == mSpanCount - 1) {
                // Bottom most ones.
                outRect.top = 0;
                outRect.bottom = mPaddingPx;
            } else {
                outRect.top = 0;
                outRect.bottom = 0;
            }
        }
    }

    private void getVerticalItemOffsets(Rect outRect,
                                        View view,
                                        RecyclerView parent,
                                        RecyclerView.State state) {
        // TODO: Support vertical orientation.
    }
}

