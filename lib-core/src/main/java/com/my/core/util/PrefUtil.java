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

package com.my.core.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefUtil {

    public static long getLong(final Context context,
                                 final String key) {
        final SharedPreferences prefs = getPrefs(context);

        return prefs.getLong(key, -1);
    }

    public static void setLong(final Context context,
                               final String key,
                               final long value) {
        final SharedPreferences prefs = getPrefs(context);

        prefs.edit()
             .putLong(key, value)
             .apply();
    }

    public static String getString(final Context context,
                                   final String key) {
        final SharedPreferences prefs = getPrefs(context);

        return prefs.getString(key, null);
    }

    public static void setString(final Context context,
                                 final String key,
                                 final String value) {
        final SharedPreferences prefs = getPrefs(context);

        prefs.edit()
             .putString(key, value)
             .apply();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    private static SharedPreferences getPrefs(final Context context) {
        return context.getSharedPreferences(
            context.getApplicationContext().getPackageName(), 0);
    }
}
