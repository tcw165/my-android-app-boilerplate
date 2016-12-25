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

package com.my.boilerplate.util;

import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ListView;
import android.widget.ScrollView;

public class ScrollViewUtil {

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
            ScrollView scrollView = (ScrollView) view;

            if (dy > 0) {
                // Slide the thumb down to scroll up the list.
                return scrollView.getScrollY() == 0;
            } else {
                // Slide the thumb up to scroll down the list.
                View child = scrollView.getChildAt(0);
                MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams();

                return child.getHeight() - scrollView.getScrollY() ==
                       scrollView.getHeight() - (params.topMargin + params.bottomMargin);
            }
        } else if (view instanceof NestedScrollView) {
            NestedScrollView scrollView = (NestedScrollView) view;

            // TODO: Complete it.
            if (dy > 0) {
//                Log.d("xyz", String.format("  ifOverScrollingVertically for NestedScrollView, getScrollY()=%d", scrollView.getScrollY()));
                // Slide the thumb down to scroll up the list.
//                return scrollView.getFirstVisiblePosition() * firstChild.getHeight() - firstChild.getTop();
                return scrollView.getScrollY() == 0;
            } else {
//                Log.d("xyz", "  ifOverScrollingVertically for NestedScrollView if dy < 0 is always false");
                // Slide the thumb up to scroll down the list.
                return false;
            }
        } else if (view instanceof ListView) {
            ListView listView = (ListView) view;

//            return listView.canScrollList(ListView.SCROLL_AXIS_VERTICAL);
            if (dy > 0) {
//                Log.d("xyz", String.format("ScrollViewUtil#ifOverScrollingVertically, getScrollY()=%d", listView.getScrollY()));
                // Slide the thumb down to scroll up the list.
                return listView.getScrollY() == 0;
            } else {
                // Slide the thumb up to scroll down the list.
                return false;
            }
        } else if (view instanceof RecyclerView) {
            RecyclerView recyclerView = (RecyclerView) view;

            // TODO: Complete it.
            if (dy > 0) {
                // Slide the thumb down to scroll up the list.
                return true;
            } else {
                // Slide the thumb up to scroll down the list.
                return false;
            }
        }
        // TODO: Support more scrollable view.

        return false;
    }
}
