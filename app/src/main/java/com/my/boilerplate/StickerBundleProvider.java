package com.my.boilerplate;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
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

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;

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

    // Uri matcher.
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ACTION_BUNDLE,
             ACTION_BUNDLE_ALL})
    public @interface URI_ACTION {
        /** enum **/
    }

    public static final int ACTION_BUNDLE = (1 << 1);
    public static final int ACTION_BUNDLE_ALL = (1 << 2);

    static {
        // "${authority}/bundle/sticker"
        sUriMatcher.addURI(AUTHORITY, PATH_STICKER,
                           ACTION_BUNDLE_ALL);
        // "${authority}/bundle/sticker/${numeric_id}"
        sUriMatcher.addURI(AUTHORITY, PATH_STICKER + "/#",
                           ACTION_BUNDLE);
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

        // Close any database.
        mSqlHelper.close();
    }

    @Override
    public Cursor query(@NotNull Uri uri,
                        String[] projection,
                        String selection,
                        String[] selectionArgs,
                        String sortOrder) {
        try {
            final SQLiteDatabase db = mSqlHelper.getReadableDatabase();

            switch (sUriMatcher.match(uri)) {
                case ACTION_BUNDLE_ALL: {
                    return db.query(StickerBundle.Entry.TABLE_NAME,
                                    projection,
                                    selection, selectionArgs,
                                    null, null,
                                    sortOrder);
                }
                case ACTION_BUNDLE: {
                    long bundleId = ContentUris.parseId(uri);
                    return db.query(StickerBundle.Entry.TABLE_NAME,
                                    projection,
                                    StickerBundle.Entry._ID + " = " + bundleId, null,
                                    null, null,
                                    sortOrder);
                }
                default:
                    return null;
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
                case ACTION_BUNDLE_ALL: {
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
                case ACTION_BUNDLE: {
                    return 0;
                }
                default:
                    return 0;
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
                case ACTION_BUNDLE_ALL: {
                    // TODO: Bulk insertion?
                    final long newId = db.insert(StickerBundle.Entry.TABLE_NAME,
                                                 null,
                                                 values);
                    final Uri newUri = Uri.parse(
                        String.format("%s/%s?%s=%s",
                                      uri.toString(),
                                      newId,
                                      Const.DB_ACTION_KEY,
                                      Const.DB_ACTION_VALUE_INSERT));

                    // Notify the subscribers.
                    mResolver.notifyChange(newUri, null);

                    return newUri;
                }
                default:
                    return null;
            }
        } catch (SQLiteCantOpenDatabaseException ignored) {
            // DO NOTHING.
        }
        return null;
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
     * @return {@link #ACTION_BUNDLE} |
     * {@link #ACTION_BUNDLE_ALL}
     */
    @URI_ACTION
    public static int getUriAction(Uri uri) {
        @URI_ACTION int action = sUriMatcher.match(uri);
        return action;
    }

    public static Uri getBundleUri() {
        return Uri.parse(SCHEME + AUTHORITY + PATH_STICKER);
    }

    public static Uri getBundleUri(long id) {
        return Uri.parse(SCHEME + AUTHORITY + PATH_STICKER + "/" + Long.toString(id));
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
            StickerBundle.Entry.COL_PRICE + " REAL," +
            StickerBundle.Entry.COL_TITLE + " TEXT," +
            StickerBundle.Entry.COL_DESC + " TEXT," +
            StickerBundle.Entry.COL_DOWNLOAD_URL + " TEXT," +
            StickerBundle.Entry.COL_INSTALL_REQUIREMENT + " TEXT," +
            StickerBundle.Entry.COL_BUNDLE_ITEM_LIST + " TEXT" +
            StickerBundle.Entry.COL_PROMOTION + " TEXT)";

        private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + StickerBundle.Entry.TABLE_NAME;

        private static final String DB_NAME = "StickerBundle.db";

        private WeakReference<Context> mContext;

        StickerBundleDbHelper(Context context) {
            // TODO: Better version control.
            super(context,
                  DB_NAME,
                  null,
                  StickerBundleConfig
                      .getLatestConfig(context)
                      .getVersion());

            mContext = new WeakReference<>(context);
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
