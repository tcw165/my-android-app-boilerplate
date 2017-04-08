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

public class GridBorderlessSpacingDecoration extends RecyclerView.ItemDecoration {

    private int mSizeGridSpacingPx;
    private int mSpanCount;

    private boolean mNeedLeftSpacing = false;

    /**
     * The grid item decoration for horizontal orientation.
     *
     * @param gridSpacingPx The inset in pixel.
     * @param spanCount      Number of items per span.
     */
    public GridBorderlessSpacingDecoration(int gridSpacingPx,
                                           int spanCount) {
        mSizeGridSpacingPx = gridSpacingPx;
        mSpanCount = spanCount;
    }

    @Override
    public void getItemOffsets(Rect outRect,
                               View view,
                               RecyclerView parent,
                               RecyclerView.State state) {
        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
        final int frameWidth = parent.getWidth() / mSpanCount;
        final int itemWidth = (int) ((parent.getWidth() - (float) mSizeGridSpacingPx * (mSpanCount - 1)) / mSpanCount);
        final int itemSpacing = frameWidth - itemWidth;
        final int position = params.getViewAdapterPosition();

        if (position < mSpanCount) {
            outRect.top = 0;
        } else {
            outRect.top = mSizeGridSpacingPx;
        }

        if (position % mSpanCount == 0) {
            // Left most one.
            outRect.left = 0;
            outRect.right = itemSpacing;
            mNeedLeftSpacing = true;
        } else if ((position + 1) % mSpanCount == 0) {
            // Right most one.
            mNeedLeftSpacing = false;
            outRect.right = 0;
            outRect.left = itemSpacing;
        } else if (mNeedLeftSpacing) {
            mNeedLeftSpacing = false;
            outRect.left = mSizeGridSpacingPx - itemSpacing;
            if ((position + 2) % mSpanCount == 0) {
                outRect.right = mSizeGridSpacingPx - itemSpacing;
            } else {
                outRect.right = mSizeGridSpacingPx / 2;
            }
        } else if ((position + 2) % mSpanCount == 0) {
            mNeedLeftSpacing = false;
            outRect.left = mSizeGridSpacingPx / 2;
            outRect.right = mSizeGridSpacingPx - itemSpacing;
        } else {
            mNeedLeftSpacing = false;
            outRect.left = mSizeGridSpacingPx / 2;
            outRect.right = mSizeGridSpacingPx / 2;
        }
        outRect.bottom = 0;
    }
}
