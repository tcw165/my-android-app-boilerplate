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

package com.my.boilerplate.data;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * A store is a data layer which observes the {@link ContentProvider} and
 * provides APIs of observable to the view-model.
 *
 *    .-----------.   .-----------.
 *    | ViewModel |   | ViewModel |   ...
 *    '-----------'   '-----------'
 *           ^             ^
 *            \            |             observes
 *           .----------------------.
 *           |  Store (Data Layer)  |
 *           '----------------------'
 *                      ^
 *                      |                observes
 *            .-------------------.
 *            |  ContentProvider  |
 *            '-------------------'
 */
public abstract class AbstractContentProviderStore<T> {

    private static final String TAG = AbstractContentProviderStore.class.getSimpleName();

    @NonNull
    protected final ContentResolver mContentResolver;

    @NonNull
    protected final ContentObserver mContentObserver;

    public AbstractContentProviderStore(ContentResolver contentResolver) {
        mContentResolver = contentResolver;
        mContentObserver = createContentObserver();
    }

    final public ContentResolver getContentResolver() {
        return mContentResolver;
    }

    final public ContentObserver getContentObserver() {
        return mContentObserver;
    }

    public void subscribeToContentResolver() {
        mContentResolver.registerContentObserver(
            getContentUri(), true, mContentObserver);
    }

    public void unsubscribeToContentResolver() {
        mContentResolver.unregisterContentObserver(mContentObserver);
    }

    @NonNull
    protected static Handler createHandler(String name) {
        HandlerThread handlerThread = new HandlerThread(name);
        handlerThread.start();
        return new Handler(handlerThread.getLooper());
    }

    protected void insertOrUpdate(T item, Uri uri) {
        Log.v(TAG, "insertOrUpdate to " + uri);
        ContentValues values = getContentValuesForItem(item);
        Log.v(TAG, "values(" + values + ")");
        if (mContentResolver.update(uri, values, null, null) == 0) {
            final Uri resultUri = mContentResolver.insert(uri, values);
            Log.v(TAG, "Inserted at " + resultUri);
        } else {
            Log.v(TAG, "Updated at " + uri);
        }
    }

    @NonNull
    protected List<T> queryList(Uri uri) {
        Cursor cursor = mContentResolver.query(uri,
                                               getProjection(), null, null, null);
        List<T> list = new ArrayList<>();

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                list.add(read(cursor));
            }
            while (cursor.moveToNext()) {
                list.add(read(cursor));
            }
            cursor.close();
        }
        if (list.size() == 0) {
            Log.v(TAG, "Could not find with uri: " + uri);
        }
        return list;
    }

    @Nullable
    protected T queryOne(Uri uri) {
        final List<T> queryResults = queryList(uri);

        if (queryResults.size() == 0) {
            return null;
        } else if (queryResults.size() > 1) {
            Log.w(TAG, "Multiple items found in a query for a single item");
        }

        return queryResults.get(0);
    }

    @NonNull
    protected abstract Uri getContentUri();

    @NonNull
    protected abstract ContentObserver createContentObserver();

    @NonNull
    protected abstract String[] getProjection();

    protected abstract T read(Cursor cursor);

    @NonNull
    protected abstract ContentValues getContentValuesForItem(T item);
}
