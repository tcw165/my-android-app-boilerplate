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

package com.my.core.util;

import android.app.ProgressDialog;
import android.content.Context;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

public class ViewUtil {

    private static final WeakHashMap<Context, ViewUtil> sInstancePool = new WeakHashMap<>();

    private final WeakReference<ProgressDialog> mProgress;

    /**
     * Because view stuff follows the lifecycle of the {@code Activity}. You are
     * asked to pass the {@code Context} to the static method to generate the
     * *util* instance related to the given context for you.
     */
    public static ViewUtil with(Context context) {
        if (!sInstancePool.containsKey(context)) {
            sInstancePool.put(context, new ViewUtil(context));
        }

        return sInstancePool.get(context);
    }

    ///////////////////////////////////////////////////////////////////////////

    private ViewUtil(final Context context) {
        mProgress = new WeakReference<>(new ProgressDialog(context));
    }

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods /////////////////////////////////////////////////////////

    /**
     * Show the progress bar when something is processing.
     * By default, it's cancelable.
     */
    public void showProgressBar(String message) {
        if (mProgress.get() == null) return;

        mProgress.get().setMessage(message);
        mProgress.get().show();
    }

    /**
     * Hide the progress bar when some process is done.
     */
    public void hideProgressBar() {
        if (mProgress.get() == null) return;

        mProgress.get().hide();
    }

    /**
     * Hide the progress bar when some process is done.
     */
    public void updateProgress(int progress) {
        if (mProgress.get() == null) return;

        mProgress.get().setProgress(progress);
    }

    /**
     * Indicate the progress bar is cancelable.
     */
    public ViewUtil setProgressBarCancelable(final boolean b) {
        if (mProgress.get() != null) {
            mProgress.get().setCancelable(b);
        }
        return this;
    }

    /**
     * Indicate the style of progress bar.
     */
    public ViewUtil showAsProgressBar(final boolean b) {
        if (mProgress.get() != null && b) {
            mProgress.get().setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgress.get().setIndeterminate(false);
        }
        return this;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////
}
