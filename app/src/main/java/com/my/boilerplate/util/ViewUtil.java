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

package com.my.boilerplate.util;

import android.app.ProgressDialog;
import android.content.Context;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

public class ViewUtil {

    private static final WeakHashMap<Context, ViewUtil> sInstancePool = new WeakHashMap<>();

    private final WeakReference<Context> mContext;
    private final WeakReference<ProgressDialog> mProgress;

    public static ViewUtil with(Context context) {
        if (!sInstancePool.containsKey(context)) {
            sInstancePool.put(context, new ViewUtil(context));
        }

        return sInstancePool.get(context);
    }

    ///////////////////////////////////////////////////////////////////////////

    ViewUtil(final Context context) {
        mContext = new WeakReference<>(context);
        mProgress = new WeakReference<>(new ProgressDialog(mContext.get()));
    }

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods /////////////////////////////////////////////////////////

    public void showProgressBar(String message) {
        if (mProgress.get() == null) return;

        mProgress.get().setMessage(message);
        mProgress.get().show();
    }

    public void hideProgressBar() {
        if (mProgress.get() == null) return;

        mProgress.get().hide();
    }

    public ViewUtil setCancelable(final boolean b) {
        if (mProgress.get() != null) {
            mProgress.get().setCancelable(b);
        }
        return this;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////
}
