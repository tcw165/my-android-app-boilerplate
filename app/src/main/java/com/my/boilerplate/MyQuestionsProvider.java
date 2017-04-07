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
import android.support.annotation.NonNull;
import android.util.Log;

import com.my.boilerplate.data.MyQuestion;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;

public class MyQuestionsProvider extends ContentProvider {

    // Uri.
    public static final String SCHEME = "content://";
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider";
    public static final String PATH_QUESTION = "/question";

    // Uri matcher.
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ACTION_QUESTION,
             ACTION_QUESTION_ALL})
    public @interface URI_ACTION {
        /** enum **/
    }

    public static final int ACTION_QUESTION = (1 << 1);
    public static final int ACTION_QUESTION_ALL = (1 << 2);

    static {
        // "${authority}/question"
        sUriMatcher.addURI(AUTHORITY, PATH_QUESTION,
                           ACTION_QUESTION_ALL);
        // "${authority}/question/${numeric_id}"
        sUriMatcher.addURI(AUTHORITY, PATH_QUESTION + "/#",
                           ACTION_QUESTION);
    }

    // Helper.
    // TODO: Replace with realm.
    SQLiteOpenHelper mSqlHelper;
    ContentResolver mResolver;

    @Override
    public boolean onCreate() {
        if (getContext() == null) return false;

        Log.d(Const.TAG, "MyQuestionProvider#onCreate() is called, " +
                         "running in the" + Looper.myLooper() + "thread");

        mSqlHelper = new QuestionDbHelper(getContext());
        mResolver = getContext().getContentResolver();

        return true;
    }

    @Override
    public void shutdown() {
        super.shutdown();

        Log.d(Const.TAG, "MyQuestionProvider#shutdown() is called, " +
                         "running in the" + Looper.myLooper() + "thread");

        // Close any database.
        mSqlHelper.close();
    }

    @Override
    public Cursor query(@NonNull Uri uri,
                        String[] projection,
                        String selection,
                        String[] selectionArgs,
                        String sortOrder) {
        try {
            final SQLiteDatabase db = mSqlHelper.getReadableDatabase();

            switch (sUriMatcher.match(uri)) {
                case ACTION_QUESTION_ALL: {
                    return db.query(MyQuestion.Entry.TABLE_NAME,
                                    projection,
                                    selection, selectionArgs,
                                    null, null,
                                    sortOrder);
                }
//                case ACTION_QUESTION: {
//                    long bundleId = ContentUris.parseId(uri);
//                    return db.query(MyQuestion.Entry.TABLE_NAME,
//                                    projection,
//                                    MyQuestion.Entry._ID + " = " + bundleId, null,
//                                    null, null,
//                                    sortOrder);
//                }
                default:
                    return null;
            }
        } catch (SQLiteCantOpenDatabaseException ex) {
            // DO NOTHING.
        }

        return null;
    }

    @Override
    public int update(@NonNull Uri uri,
                      ContentValues values,
                      String whereClause,
                      String[] whereArgs) {
        try {
            switch (sUriMatcher.match(uri)) {
                case ACTION_QUESTION_ALL: {
//                    mMyQuestions = prepareMyQuestionListIfNecessary();
//                    if (mMyQuestions == null ||
//                        mMyQuestions.isEmpty()) {
//                        return 0;
//                    } else {
//                        // Notify the observers.
//                        mResolver.notifyChange(uri, null);
//                        return mMyQuestions.size();
//                    }
                    return 0;
                }
                case ACTION_QUESTION: {
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
    public Uri insert(@NonNull Uri uri,
                      ContentValues values) {
        try {
            final SQLiteDatabase db = mSqlHelper.getWritableDatabase();

            switch (sUriMatcher.match(uri)) {
                case ACTION_QUESTION_ALL: {
                    // TODO: Bulk insertion?
                    final long newId = db.insert(MyQuestion.Entry.TABLE_NAME,
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
    public int delete(@NonNull Uri uri,
                      String selection,
                      String[] selectionArgs) {
        return 0;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

//    /**
//     * @return {@link #ACTION_QUESTION} |
//     * {@link #ACTION_QUESTION_ALL}
//     */
//    @URI_ACTION
//    public static int getUriAction(Uri uri) {
//        @URI_ACTION int action = sUriMatcher.match(uri);
//        return action;
//    }

    public static Uri getAllUri() {
        return Uri.parse(SCHEME + AUTHORITY + PATH_QUESTION);
    }

//    public static Uri getBundleUri(long id) {
//        return Uri.parse(SCHEME + AUTHORITY + PATH_QUESTION + "/" + Long.toString(id));
//    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    private static class QuestionDbHelper extends SQLiteOpenHelper {

        private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + MyQuestion.Entry.TABLE_NAME + " (" +
            MyQuestion.Entry._ID + " INTEGER PRIMARY KEY," +
            MyQuestion.Entry.COL_DATA + " TEXT)";

        private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + MyQuestion.Entry.TABLE_NAME;

        private static final String DB_NAME = "questions.db";

        private WeakReference<Context> mContext;

        QuestionDbHelper(Context context) {
            // TODO: Better version control.
            super(context, DB_NAME, null, 1);

            mContext = new WeakReference<>(context);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(SQL_CREATE_ENTRIES);
            } catch (Throwable any) {
                Log.d(Const.TAG, any.toString());
            }
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
