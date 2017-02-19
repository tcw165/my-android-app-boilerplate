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

package com.my.boilerplate.data;

import android.provider.BaseColumns;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class StickerBundle {

    @SerializedName("count")
    public int count;

    @SerializedName("price")
    public float price;

    @SerializedName("bundle_name")
    public String name;

    @SerializedName("title")
    public String title;

    @SerializedName("description")
    public String description;

    @SerializedName("install_source_url")
    public String installSourceUrl;

    @SerializedName("thumbnail")
    public String coverUrl;

    @SerializedName("thumbnails")
    public List<String> stickerThumbnails;

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    public static class Entry implements BaseColumns {

        public static final String TABLE_NAME = "sticker_bundle";

        public static final String COL_NAME = "name";
        public static final String COL_COVER = "cover";
        public static final String COL_COUNT = "count";
        public static final String COL_PRICE = "price";
        public static final String COL_TITLE = "title";
        public static final String COL_DESC = "description";
        public static final String COL_INSTALL_URL = "install_source_url";
        public static final String COL_STICKER_THUMBS = "sticker_thumbnails";
        // For complicated/nested/extensible data.
        public static final String COL_EXTRA_JSON = "extra_json";
    }
}
