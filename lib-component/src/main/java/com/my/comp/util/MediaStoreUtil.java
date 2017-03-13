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

package com.my.comp.util;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.my.comp.data.IPhoto;
import com.my.comp.data.IPhotoAlbum;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class MediaStoreUtil {

    // Albums related /////////////////////////////////////////////////////////
    private static final String[] ALBUMS_PROJECTION = {
        MediaStore.Images.Media.DATA,
        MediaStore.Images.Media.BUCKET_ID,
        MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
        String.format("COUNT(%s) AS _COUNT", MediaStore.Images.Media.BUCKET_ID),
        "CASE " +
        "    WHEN LOWER(" + MediaStore.Images.Media.BUCKET_DISPLAY_NAME + ") = 'camera' THEN 0" +
        "    WHEN LOWER(" + MediaStore.Images.Media.BUCKET_DISPLAY_NAME + ") = 'instagram' THEN 1" +
        "    WHEN LOWER(" + MediaStore.Images.Media.BUCKET_DISPLAY_NAME + ") = 'download'" +
        "    OR LOWER(" + MediaStore.Images.Media.BUCKET_DISPLAY_NAME + ") = 'downloads' THEN 2" +
        "    WHEN LOWER(" + MediaStore.Images.Media.BUCKET_DISPLAY_NAME + ") = 'screenshot'" +
        "    OR LOWER(" + MediaStore.Images.Media.BUCKET_DISPLAY_NAME + ") = 'screenshots' THEN 3" +
        "    WHEN LOWER(" + MediaStore.Images.Media.BUCKET_DISPLAY_NAME + ") = 'facebook' THEN 4" +
        "    ELSE 50 " +
        "END as IDX"
    };
    private static final String ALBUMS_SELECTION =
        String.format("NOT TRIM(%s) = '') GROUP BY (2", MediaStore.Images.Media.DATA);
    private static final String ALBUMS_SORTING_ORDER =
        "IDX, _COUNT DESC, " +
        MediaStore.Images.Media.DEFAULT_SORT_ORDER +
        ", " +
        MediaStore.Images.Media.DATE_ADDED;

    // Photos related /////////////////////////////////////////////////////////
    private static final String[] PHOTOS_PROJECTION = {
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DATA,
        MediaStore.Images.Media.WIDTH,
        MediaStore.Images.Media.HEIGHT,
        MediaStore.Images.Media.ORIENTATION,
        MediaStore.Images.Thumbnails.DATA,
        MediaStore.Images.Thumbnails.WIDTH,
        MediaStore.Images.Thumbnails.HEIGHT
    };
    private static final String PHOTOS_SELECTION =
        String.format("NOT TRIM(%s) = '' AND %s = ? ",
                      MediaStore.Images.Media.DATA,
                      MediaStore.Images.Media.BUCKET_ID);
    private static final String PHOTOS_SORTING_ORDER =
        MediaStore.Images.Media.DATE_MODIFIED + " DESC";

    /**
     * Get list of the album on the device.
     *
     * @param resolver   To query the {@link MediaStore.Images}.
     * @param validToken A token for the caller to cancel the process. True
     *                   means the process is still valid; False means the
     *                   process should be terminated.
     */
    public static List<IPhotoAlbum> getAlbums(final ContentResolver resolver,
                                              final AtomicBoolean validToken) {
        if (resolver == null || validToken == null) return null;
        // Return if it is canceled before doing anything.
        if (!validToken.get()) return null;

        Cursor cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            // projection.
            ALBUMS_PROJECTION,
            // selection.
            ALBUMS_SELECTION,
            // selection args.
            null,
            // sorting order.
            ALBUMS_SORTING_ORDER);
        if (cursor == null) return null;
        // Return if it is canceled before doing anything.
        if (!validToken.get()) return null;

        List<IPhotoAlbum> photos = new ArrayList<>();
        try {
            cursor.moveToFirst();
            int albumIdColumn = cursor.getColumnIndexOrThrow(
                MediaStore.Images.Media.BUCKET_ID);
            int thumbnailPathColumn = cursor.getColumnIndexOrThrow(
                MediaStore.Images.Media.DATA);
            int albumNameColumn = cursor.getColumnIndexOrThrow(
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
            int countColumn = cursor.getColumnIndexOrThrow("_COUNT");

            do {
                final String albumId = cursor.getString(albumIdColumn);
                final String albumName = cursor.getString(albumNameColumn);
                final String thumbnailPath = cursor.getString(thumbnailPathColumn);
                final int photoNum = cursor.getInt(countColumn);

                // If no data for Image, just skip this one
                if (TextUtils.isEmpty(albumId) ||
                    TextUtils.isEmpty(albumName) ||
                    TextUtils.isEmpty(thumbnailPath) ||
                    photoNum == 0) {
                    continue;
                }

                photos.add(new Album(albumId,
                                     albumName,
                                     thumbnailPath,
                                     photoNum));
            } while (validToken.get() && cursor.moveToNext());
        } finally {
            cursor.close();
        }

        return photos;
    }

    public static Cursor getPhotosOfAlbum(final ContentResolver resolver,
                                          final String albumId) {
        if (resolver == null ||
            albumId == null || albumId.isEmpty()) return null;

        return resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            // projection.
            PHOTOS_PROJECTION,
            // selection.
            PHOTOS_SELECTION,
            // selection args.
            new String[]{albumId},
            // sorting order.
            PHOTOS_SORTING_ORDER);
    }

    public static String getImagePath(final Cursor cursor) {
        int thumbColumn;
        try {
            thumbColumn = cursor.getColumnIndexOrThrow(
                MediaStore.Images.Thumbnails.DATA);
        } catch (IllegalArgumentException ignored) {
            Log.d("xyz", "Failed to load thumbnail, try full-sized instead.");
            thumbColumn = cursor.getColumnIndexOrThrow(
                MediaStore.Images.Media.DATA);
        }
        return cursor.getString(thumbColumn);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    private static class Album implements IPhotoAlbum {

        private String mId = null;
        private String mName = null;
        private String mFullsizePath = null;
        private String mThumbnailPath = null;

        private int mPhotoNum = 0;

        private float mWidth = 0;
        private float mHeight = 0;
        private float mThumbWidth = 0;
        private float mThumbHeight = 0;

        Album(String id,
              String name,
              String thumbPath,
              int photoNum) {
            mId = id;
            mName = name;
            mThumbnailPath = thumbPath;
            mPhotoNum = photoNum;
        }

        @Override
        public String id() {
            return mId;
        }

        @Override
        public String name() {
            return mName;
        }

        @Override
        public float width() {
            return mWidth;
        }

        @Override
        public float height() {
            return mHeight;
        }

        @Override
        public float thumbnailWidth() {
            return mThumbWidth;
        }

        @Override
        public float thumbnailHeight() {
            return mThumbHeight;
        }

        @Override
        public String thumbnailPath() {
            return mThumbnailPath;
        }

        @Override
        public String fullSizedPath() {
            return mFullsizePath;
        }

        @Override
        public int photoNum() {
            return mPhotoNum;
        }

        @Override
        public void setId(String id) {
            mId = id;
        }

        @Override
        public void setName(String name) {
            mName = name;
        }

        @Override
        public void setWidth(float width) {
            mWidth = width;
        }

        @Override
        public void setHeight(float height) {
            mHeight = height;
        }

        @Override
        public void setThumbnailWidth(float width) {
            mThumbWidth = width;
        }

        @Override
        public void setThumbnailHeight(float height) {
            mThumbHeight = height;
        }

        @Override
        public void setThumbnailPath(String path) {
            mThumbnailPath = path;
        }

        @Override
        public void setFullsizePath(String path) {
            mFullsizePath = path;
        }

        @Override
        public void setPhotoNum(int num) {
            mPhotoNum = num;
        }
    }

    static class Photo implements IPhoto {

        private String mFullSizedPath = null;
        private String mThumbnailPath = null;

        private float mWidth = 0;
        private float mHeight = 0;
        private float mThumbWidth = 0;
        private float mThumbHeight = 0;

        @Override
        public float width() {
            return mWidth;
        }

        @Override
        public float height() {
            return mHeight;
        }

        @Override
        public float thumbnailWidth() {
            return mThumbWidth;
        }

        @Override
        public float thumbnailHeight() {
            return mThumbHeight;
        }

        @Override
        public String thumbnailPath() {
            return mThumbnailPath;
        }

        @Override
        public String fullSizedPath() {
            return mFullSizedPath;
        }

        @Override
        public void setWidth(float width) {
            mWidth = width;
        }

        @Override
        public void setHeight(float height) {
            mHeight = height;
        }

        @Override
        public void setThumbnailWidth(float width) {
            mThumbWidth = width;
        }

        @Override
        public void setThumbnailHeight(float height) {
            mThumbHeight = height;
        }

        @Override
        public void setThumbnailPath(String path) {
            mThumbnailPath = path;
        }

        @Override
        public void setFullsizePath(String path) {
            mFullSizedPath = path;
        }
    }
}
