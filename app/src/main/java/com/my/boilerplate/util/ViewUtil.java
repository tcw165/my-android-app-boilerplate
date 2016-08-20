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

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

public class ViewUtil {

    public static final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 0;

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

    public void showProgressBar(String message) {
        if (mProgress.get() == null) return;

        mProgress.get().setMessage(message);
        mProgress.get().show();
    }

    public void hideProgressBar() {
        if (mProgress.get() == null) return;

        mProgress.get().hide();
    }

    ///////////////////////////////////////////////////////////////////////////

    public void checkPermission() {
        Activity thisActivity = (Activity) mContext.get();
        int permissionCheck = ContextCompat.checkSelfPermission(
            thisActivity,
            Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) return;

        // Should we show an explanation?
        if (ActivityCompat.shouldShowRequestPermissionRationale(
            thisActivity,
            Manifest.permission.ACCESS_FINE_LOCATION)) {

            // Show an expanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.

        } else {

            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(
                thisActivity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSIONS_ACCESS_FINE_LOCATION);

            // MY_PERMISSIONS_ACCESS_FINE_LOCATION is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    private ViewUtil(final Context context) {
        mContext = new WeakReference<>(context);

        mProgress = new WeakReference<>(new ProgressDialog(mContext.get()));
        mProgress.get().setCancelable(false);
    }
}
