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

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.my.boilerplate.util.ListUtil;

import java.util.ArrayList;
import java.util.List;

public class StickerBundle {

    public long id;
    public int count;
    public float price;
    public String name;
    public String title;
    public String description;
    public String coverUrl;
    public List<BundleItem> bundleItemList;
    public String downloadUrl;
    public InstallRequirement installRequirement;
    public Promotion promotion;
    public String installedFilePath;

    public StickerBundle(RemoteJson json) {
        if (json == null) {
            throw new IllegalArgumentException("The given Json is null");
        }

        id = -1;
        name = json.name;
        title = json.title;
        description = json.description;
        coverUrl = json.coverUrl;
        bundleItemList = new ArrayList<>();
        for (String thumbUrl : json.thumbnails) {
            bundleItemList.add(new BundleItem(thumbUrl, null, null));
        }
        downloadUrl = json.downloadUrl;
        installRequirement = new InstallRequirement(json.installRequirement);
        promotion = new Promotion(json.promotion);
        installedFilePath = null;
    }

    public StickerBundle(Cursor cursor) {
        if (cursor == null) return;

        final Gson gson = new Gson();
        int index;

        // TODO: Complete it.
        // ID.
        index = cursor.getColumnIndex(StickerBundle.Entry._ID);
        if (index != -1) {
            id = cursor.getLong(index);
        }
        // Name.
        index = cursor.getColumnIndex(StickerBundle.Entry.COL_NAME);
        if (index != -1) {
            name = cursor.getString(index);
        }
        // Cover.
        index = cursor.getColumnIndex(StickerBundle.Entry.COL_COVER);
        if (index != -1) {
            coverUrl = cursor.getString(index);
        }
        // Title.
        index = cursor.getColumnIndex(StickerBundle.Entry.COL_TITLE);
        if (index != -1) {
            title = cursor.getString(index);
        }
        // Description.
        index = cursor.getColumnIndex(StickerBundle.Entry.COL_DESC);
        if (index != -1) {
            description = cursor.getString(index);
        }
        // Bundle item list.
        index = cursor.getColumnIndex(Entry.COL_BUNDLE_ITEM_LIST);
        if (index != -1) {
            bundleItemList = ListUtil.fromJson(cursor.getString(index), BundleItem.class);
        }
        // Download url.
        index = cursor.getColumnIndex(StickerBundle.Entry.COL_DOWNLOAD_URL);
        if (index != -1) {
            downloadUrl = cursor.getString(index);
        }
        // Installed file path.
        index = cursor.getColumnIndex(StickerBundle.Entry.COL_INSTALLED_FILE_PATH);
        if (index != -1) {
            installedFilePath = cursor.getString(index);
        }
        // Install requirement.
        index = cursor.getColumnIndex(Entry.COL_INSTALL_REQUIREMENT);
        if (index != -1) {
            installRequirement = gson.fromJson(
                cursor.getString(index), InstallRequirement.class);
        }
        // Promotion.
        index = cursor.getColumnIndex(Entry.COL_PROMOTION);
        if (index != -1) {
            promotion = gson.fromJson(
                cursor.getString(index), Promotion.class);
        }
    }

    public ContentValues toContentValues() {
        final ContentValues values = new ContentValues();

        values.put(Entry.COL_NAME, name);
        values.put(Entry.COL_PRICE, price);
        values.put(Entry.COL_COVER, coverUrl);
        values.put(Entry.COL_TITLE, title);
        values.put(Entry.COL_DESC, description);
        values.put(Entry.COL_BUNDLE_ITEM_LIST, ListUtil.toJson(bundleItemList, BundleItem.class));
        values.put(Entry.COL_DOWNLOAD_URL, downloadUrl);
        values.put(Entry.COL_INSTALLED_FILE_PATH, installedFilePath);
        values.put(Entry.COL_INSTALL_REQUIREMENT, installRequirement.toJson());
        values.put(Entry.COL_PROMOTION, promotion.toJson());

        return values;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    /**
     * The database entries.
     */
    public static class Entry implements BaseColumns {

        // V1.
        public static final String TABLE_NAME = "sticker_bundle";

        public static final String COL_NAME = "name";
        public static final String COL_COVER = "cover";
        public static final String COL_PRICE = "price";
        public static final String COL_TITLE = "title";
        public static final String COL_DESC = "description";
        public static final String COL_BUNDLE_ITEM_LIST = "item_list";
        public static final String COL_DOWNLOAD_URL = "download_url";
        public static final String COL_INSTALLED_FILE_PATH = "installed_filepath";
        public static final String COL_INSTALL_REQUIREMENT = "install_requirement";
        public static final String COL_PROMOTION = "promotion";
    }

    /**
     * The JSON format from the server, which is separate to the client format.
     */
    public static class RemoteJson {
        @SerializedName("count")
        public int count = 0;

        @SerializedName("price")
        public float price = 0.f;

        @SerializedName("bundle_name")
        public String name = null;

        @SerializedName("title")
        public String title = null;

        @SerializedName("description")
        public String description = null;

        @SerializedName("install_source_url")
        public String downloadUrl = null;

        @SerializedName("install_requirement")
        public InstallRequirement installRequirement = null;

        @SerializedName("thumbnail")
        public String coverUrl = null;

        @SerializedName("thumbnails")
        public List<String> thumbnails = null;

        @SerializedName("promotion_info")
        public Promotion promotion = null;

        public static class InstallRequirement {
            @SerializedName("type")
            public String type;
        }

        public static class Promotion {

            @SerializedName("banner_url")
            public String bannerUrl;

            @SerializedName("promotion_id")
            public String promotionId;

            @SerializedName("click_url")
            public String clickUrl;
        }
    }

    /**
     * A bundle is a set of items.
     * <br/>
     * Note: This is client format.
     */
    public static class BundleItem {

        @SerializedName("image_thumb_url")
        public String imageThumbUrl;

        @SerializedName("image_thumb_filepath")
        public String imageThumbFilePath;

        @SerializedName("image_filepath")
        public String imageFilePath;

        public BundleItem(String imageThumbUrl,
                          String imageThumbFilePath,
                          String imageFilePath) {
            this.imageThumbUrl = imageThumbUrl;
            this.imageThumbFilePath = imageThumbFilePath;
            this.imageFilePath = imageFilePath;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            BundleItem that = (BundleItem) o;

            if (imageThumbUrl != null ? !imageThumbUrl.equals(that.imageThumbUrl) : that.imageThumbUrl != null)
                return false;
            if (imageThumbFilePath != null ? !imageThumbFilePath
                .equals(that.imageThumbFilePath) : that.imageThumbFilePath != null) return false;
            return imageFilePath != null ? imageFilePath.equals(that.imageFilePath) : that.imageFilePath == null;

        }

        @Override
        public int hashCode() {
            int result = imageThumbUrl != null ? imageThumbUrl.hashCode() : 0;
            result = 31 * result + (imageThumbFilePath != null ? imageThumbFilePath.hashCode() : 0);
            result = 31 * result + (imageFilePath != null ? imageFilePath.hashCode() : 0);
            return result;
        }
    }

    /**
     * The requirement to be fulfilled before being installed.
     * <br/>
     * Note: This is client format.
     */
    public static class InstallRequirement {

        @SerializedName("type")
        public String type;

        @SerializedName("is_fulfilled")
        public boolean isFulfilled;

        @SerializedName("due_date")
        public String dueDate;

        InstallRequirement(RemoteJson.InstallRequirement obj) {
            type = obj.type;
            isFulfilled = false;
            dueDate = null;
        }

        String toJson() {
            return new Gson().toJson(this, InstallRequirement.class);
        }
    }

    /**
     * The promotion is for driving users to other place.
     * <br/>
     * Note: This is client format.
     */
    public static class Promotion {

        @SerializedName("banner_image_url")
        public String bannerImageUrl;

        @SerializedName("promotion_id")
        public String promotionId;

        @SerializedName("on_click_to_open_url")
        public String onClickToOpenUrl;

        Promotion(RemoteJson.Promotion remoteObj) {
            bannerImageUrl = remoteObj.bannerUrl;
            promotionId = remoteObj.promotionId;
            onClickToOpenUrl = remoteObj.clickUrl;
        }

        String toJson() {
            return new Gson().toJson(this, RemoteJson.Promotion.class);
        }
    }
}
