package com.my.boilerplate;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Looper;
import android.support.annotation.IntDef;
import android.util.Log;

import com.my.boilerplate.data.StickerBundle;
import com.my.boilerplate.data.StickerBundleConfig;
import com.my.boilerplate.util.StringUtil;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * It provides a URI to access the bundles. e.g. Sticker and background.
 * <br/><br/>
 * Rules:
 * <pre>
 * query: (constructing)
 * update: (constructing)
 * insert: (constructing)
 * delete: (constructing)
 * </pre>
 * Infrastructure:
 * <pre>
 *
 *      .----------------------------.
 *      |       UI Components:       |
 *      |        Application/        |
 *      |         Activity/          |
 *      |         Fragment/          |    (register)
 *      |         ViewModel/     .--------------------.
 *      |           ...          | Broadcast Receiver |
 *      |                        '--------------------'
 *      '----------------------------'        ^
 *                    ^                       | notify state of like install-
 *          (pub/sub) |                       | ation, downloading, etc.
 *                    v                       |
 *             .-------------.      .-----------------------.
 *             | BundleStore |----->| LocalBroadcastManager |
 *             '-------------'      '-----------------------'
 *                    ^
 * (query/update/...) |
 *                    v
 *         .-----------------------.
 *         | StickerBundleProvider |
 *         '-----------------------'
 *           /      |
 *          /       |  (facilitate from helpers)
 *         v        v
 *     Local DB, Mem DB, ...
 *
 * StickerBundleProvider is responsible for storing the data in efficient way.
 * StickerBundleStore is responsible for simplifying the interface between the provider
 * and the UI components.
 * </pre>
 */
public class StickerBundleProvider extends ContentProvider {

    // Uri.
    public static final String SCHEME = "content://";
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider";
    public static final String PATH_STICKER = "/bundle/sticker";
    public static final String PATH_QUERY_STICKER = PATH_STICKER + "/query/#";
    public static final String PATH_QUERY_STICKER_ALL = PATH_STICKER + "/query";
    public static final String PATH_INSERT_STICKER = PATH_STICKER + "/insert";
    public static final String PATH_UPDATE_STICKER = PATH_STICKER + "/update";
    public static final String PATH_DELETE_STICKER = PATH_STICKER + "/delete";
    public static final String PATH_DELETE_STICKER_ALL = PATH_STICKER + "/delete/#";

    // Uri matcher.
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ACTION_INSERT_STICKER_BUNDLE,
             ACTION_UPDATE_STICKER_BUNDLE,
             ACTION_DELETE_STICKER_BUNDLE,
             ACTION_DELETE_STICKER_BUNDLE_ALL,
             ACTION_QUERY_STICKER_BUNDLE_ALL,
             ACTION_QUERY_STICKER_BUNDLE})
    public @interface URI_ACTION {
    }

    public static final int ACTION_INSERT_STICKER_BUNDLE = (1 << 1);
    public static final int ACTION_UPDATE_STICKER_BUNDLE = (1 << 2);
    public static final int ACTION_DELETE_STICKER_BUNDLE = (1 << 3);
    public static final int ACTION_DELETE_STICKER_BUNDLE_ALL = (1 << 4);
    public static final int ACTION_QUERY_STICKER_BUNDLE_ALL = (1 << 5);
    public static final int ACTION_QUERY_STICKER_BUNDLE = (1 << 6);

    static {
        // "${authority}/bundle/sticker/insert"
        sUriMatcher.addURI(AUTHORITY, PATH_INSERT_STICKER,
                           ACTION_INSERT_STICKER_BUNDLE);
        // "${authority}/bundle/sticker/insert/${numeric_id}"
        sUriMatcher.addURI(AUTHORITY, PATH_INSERT_STICKER + "/#",
                           ACTION_INSERT_STICKER_BUNDLE);
        // "${authority}/bundle/sticker/update/${numeric_id}"
        sUriMatcher.addURI(AUTHORITY, PATH_UPDATE_STICKER + "/#",
                           ACTION_UPDATE_STICKER_BUNDLE);
        // "${authority}/bundle/sticker/delete/${numeric_id}"
        sUriMatcher.addURI(AUTHORITY, PATH_DELETE_STICKER + "/#",
                           ACTION_DELETE_STICKER_BUNDLE);
        // "${authority}/bundle/sticker/delete"
        sUriMatcher.addURI(AUTHORITY, PATH_DELETE_STICKER_ALL,
                           ACTION_DELETE_STICKER_BUNDLE_ALL);
        // "${authority}/bundle/sticker/query"
        sUriMatcher.addURI(AUTHORITY, PATH_QUERY_STICKER_ALL,
                           ACTION_QUERY_STICKER_BUNDLE_ALL);
        // "${authority}/bundle/sticker/query/${numeric_id}"
        sUriMatcher.addURI(AUTHORITY, PATH_QUERY_STICKER,
                           ACTION_QUERY_STICKER_BUNDLE);
    }

    // Helper.
    // TODO: Replace with realm.
    SQLiteOpenHelper mSqlHelper;
    ContentResolver mResolver;

    @Override
    public boolean onCreate() {
        if (getContext() == null) return false;

        Log.d(Const.TAG, "StickerBundleProvider#onCreate() is called, " +
                         "running in the" + Looper.myLooper() + "thread");

        mSqlHelper = new StickerBundleDbHelper(getContext());
        mResolver = getContext().getContentResolver();

        return true;
    }

    @Override
    public void shutdown() {
        super.shutdown();

        Log.d(Const.TAG, "StickerBundleProvider#shutdown() is called, " +
                         "running in the" + Looper.myLooper() + "thread");
//        // Close database returned by {@link SQLiteOpenHelper.getReadableDatabase}.
//        mSqlHelper.close();
    }

    @Override
    public Cursor query(@NotNull Uri uri,
                        String[] projection,
                        String selection,
                        String[] selectionArgs,
                        String sortOrder) {
        try {
            switch (sUriMatcher.match(uri)) {
                case ACTION_QUERY_STICKER_BUNDLE_ALL: {
//                    if (mStickerBundles == null ||
//                        mStickerBundles.isEmpty()) {
//                        // TODO: If the memory bundle is empty, deserialize it.
//                        final File bundlesFile = new File(mConfig.nameOfRootDir,
//                                                          mConfig.nameOfBundleListFile);
//                        if (bundlesFile.exists()) {
//                            mStickerBundles = parseIapBundleList(bundlesFile);
//                        }
//                    }
//
//                    // Create a cursor pointing to the first item in the data
//                    // stored in the memory.
//                    return toCursor(mStickerBundles);
                    return null;
                }
                case ACTION_QUERY_STICKER_BUNDLE: {
                    return null;
                }
            }
        } catch (SQLiteCantOpenDatabaseException ex) {
            // DO NOTHING.
        }

        return null;
    }

    @Override
    public int update(@NotNull Uri uri,
                      ContentValues values,
                      String whereClause,
                      String[] whereArgs) {
        try {
            switch (sUriMatcher.match(uri)) {
                case ACTION_QUERY_STICKER_BUNDLE_ALL: {
//                    mStickerBundles = prepareStickerBundleListIfNecessary();
//                    if (mStickerBundles == null ||
//                        mStickerBundles.isEmpty()) {
//                        return 0;
//                    } else {
//                        // Notify the observers.
//                        mResolver.notifyChange(uri, null);
//                        return mStickerBundles.size();
//                    }
                    return 0;
                }
                case ACTION_QUERY_STICKER_BUNDLE: {
                    return 0;
                }
            }
        } catch (SQLiteCantOpenDatabaseException ex) {
            // DO NOTHING.
        }

        return 0;
    }

    @Override
    public Uri insert(@NotNull Uri uri,
                      ContentValues values) {
        try {
            final SQLiteDatabase db = mSqlHelper.getWritableDatabase();

            switch (sUriMatcher.match(uri)) {
                case ACTION_INSERT_STICKER_BUNDLE: {
                    final long id = db.insert(StickerBundle.Entry.TABLE_NAME,
                                              null,
                                              values);
                    final Uri newUri = getBundleInsertionUriById(id);

                    // Notify the subscribers.
                    mResolver.notifyChange(newUri, null);

                    return newUri;
                }
            }
        } catch (SQLiteCantOpenDatabaseException ignored) {
            // DO NOTHING.
        }
        return uri;
    }

    @Override
    public int delete(@NotNull Uri uri,
                      String selection,
                      String[] selectionArgs) {
        return 0;
    }

    @Override
    public String getType(@NotNull Uri uri) {
        return null;
    }

    /**
     * @return {@link #ACTION_QUERY_STICKER_BUNDLE_ALL} |
     * {@link #ACTION_QUERY_STICKER_BUNDLE}
     */
    @URI_ACTION
    public static int getUriAction(Uri uri) {
        @URI_ACTION int action = sUriMatcher.match(uri);
        return action;
    }

    public static Uri getBundlePrefixUri() {
        return Uri.parse(SCHEME + AUTHORITY + PATH_STICKER);
    }

    public static Uri getBundleListUri() {
        return Uri.parse(SCHEME + AUTHORITY + PATH_QUERY_STICKER_ALL);
    }

    public static Uri getBundleInsertionUri() {
        return Uri.parse(SCHEME + AUTHORITY + PATH_INSERT_STICKER);
    }

    public static Uri getBundleInsertionUriById(long id) {
        return Uri.parse(SCHEME + AUTHORITY + PATH_INSERT_STICKER + "/" + id);
    }

    public static ContentValues bundleToContentValues(StickerBundle bundle) {
        ContentValues values = new ContentValues();

        // TODO: The uniform database for different version?
        values.put(StickerBundle.Entry.COL_NAME, bundle.name);
        values.put(StickerBundle.Entry.COL_PRICE, bundle.price);
        values.put(StickerBundle.Entry.COL_COUNT, bundle.count);
        values.put(StickerBundle.Entry.COL_COVER, bundle.coverUrl);
        values.put(StickerBundle.Entry.COL_TITLE, bundle.title);
        values.put(StickerBundle.Entry.COL_DESC, bundle.description);
        values.put(StickerBundle.Entry.COL_INSTALL_URL, bundle.installSourceUrl);
        values.put(StickerBundle.Entry.COL_STICKER_THUMBS,
                   StringUtil.join(StringUtil.COMMA, bundle.stickerThumbnails));
        // TODO: Need install_requirement and promotion_info

        return values;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    private static class StickerBundleDbHelper extends SQLiteOpenHelper {

        private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + StickerBundle.Entry.TABLE_NAME + " (" +
            StickerBundle.Entry._ID + " INTEGER PRIMARY KEY," +
            StickerBundle.Entry.COL_NAME + " TEXT," +
            StickerBundle.Entry.COL_COUNT + " INTEGER," +
            StickerBundle.Entry.COL_PRICE + " REAL," +
            StickerBundle.Entry.COL_TITLE + " TEXT," +
            StickerBundle.Entry.COL_DESC + " TEXT," +
            StickerBundle.Entry.COL_INSTALL_URL + " TEXT," +
            StickerBundle.Entry.COL_STICKER_THUMBS + " TEXT" +
            // TODO: Storing as byte[] is probably better?
            StickerBundle.Entry.COL_EXTRA_JSON + " TEXT)";

        private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + StickerBundle.Entry.TABLE_NAME;

        private static final String DB_NAME = "StickerBundle.db";

        StickerBundleDbHelper(Context context) {
            // TODO: Better version control.
            super(context,
                  DB_NAME,
                  null,
                  StickerBundleConfig
                      .getConfig(context)
                      .getVersion());
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db,
                              int oldVersion,
                              int newVersion) {
            // This database is only a cache for online data, so its upgrade policy
            // is to simply to discard the data and start over.
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }

        @Override
        public void onDowngrade(SQLiteDatabase db,
                                int oldVersion,
                                int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }
}
