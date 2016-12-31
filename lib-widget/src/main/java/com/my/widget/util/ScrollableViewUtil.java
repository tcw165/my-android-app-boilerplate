// Copyright (c) 2016-present boyw165
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

package com.my.widget.util;

import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ListView;
import android.widget.ScrollView;

public class ScrollableViewUtil {

    /**
     * Return true if the given scrollable view is over scrolling in terms of
     * the given y momentum.
     * <br/>
     * Usually it is called in the {@code dispatchTouchEvent}, {@code onTouchEvent},
     * or the {@code onInterceptTouchEvent}.
     *
     * @param view the scrollable view.
     * @param dy The y momentum.
     * @return true if the view is over scrolling in the vertical direction.
     */
    public static boolean ifOverScrollingVertically(View view,
                                                    float dy) {
        if (view == null || dy == 0) return false;

        // TODO: Test it.
        if (view instanceof ScrollView) {
            final ScrollView scrollView = (ScrollView) view;
            final View child = scrollView.getChildAt(0);
            if (scrollView.getHeight() >= child.getHeight()) return true;

            if (dy > 0) {
                // Slide the thumb down to scroll up the list.
                return scrollView.getScrollY() == 0;
            } else {
                // Slide the thumb up to scroll down the list.
                MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams();

                return child.getHeight() - scrollView.getScrollY() ==
                       scrollView.getHeight() - (params.topMargin + params.bottomMargin);
            }
//        } else if (view instanceof WebView) {
//            WebView webView = (WebView) view;
//
//            // TODO: Complete it.
//            if (dy > 0) {
//                // Slide the thumb down to scroll up the list.
//                return webView.computeHorizontalScrollOffset() == 0.f;
//            } else {
//                // Slide the thumb up to scroll down the list.
//                return false;
//            }
        } else if (view instanceof NestedScrollView) {
            final NestedScrollView scrollView = (NestedScrollView) view;
            final int scrollRange = scrollView.computeVerticalScrollRange() -
                                    scrollView.computeVerticalScrollExtent();

            if (scrollRange <= 0) return true;

            if (dy > 0) {
                // Slide the thumb down to scroll up the list.
                return scrollView.computeVerticalScrollOffset() == 0.f;
            } else {
                // Slide the thumb up to scroll down the list.
                return scrollView.computeVerticalScrollOffset() == scrollRange;
            }
        } else if (view instanceof ListView) {
            final ListView listView = (ListView) view;
            if (listView.getAdapter().getCount() == 0) return true;

            // TODO: Complete it.
            if (dy > 0) {
                // Slide the thumb down to scroll up the list.
                return listView.getScrollY() == 0;
            } else {
                // Slide the thumb up to scroll down the list.
                return false;
            }
        } else if (view instanceof RecyclerView) {
            final RecyclerView recyclerView = (RecyclerView) view;
            if (recyclerView.getAdapter().getItemCount() == 0) return true;

            final int scrollRange = recyclerView.computeVerticalScrollRange() -
                                    recyclerView.computeVerticalScrollExtent();

            if (dy > 0) {
                // Slide the thumb down to scroll up the list.
                return recyclerView.computeVerticalScrollOffset() == 0.f;
            } else {
                // Slide the thumb up to scroll down the list.
                return recyclerView.computeVerticalScrollOffset() == scrollRange;
            }
        }
        // TODO: Support more scrollable view.

        // Return true for the unsupported view because the dy is non-zero.
        return true;
    }
}
