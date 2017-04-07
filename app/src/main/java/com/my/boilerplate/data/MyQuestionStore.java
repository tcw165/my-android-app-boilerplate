package com.my.boilerplate.data;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.my.boilerplate.Const;
import com.my.boilerplate.MyQuestionsProvider;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class MyQuestionStore extends AbstractContentProviderStore<MyQuestion> {

    private static MyQuestionStore sInstance;

    // External reference.
    @SuppressWarnings("unused")
    WeakReference<Context> mContext;

    public static MyQuestionStore with(final Context context) {
        if (sInstance == null) {
            sInstance = new MyQuestionStore(context);
        }
        return sInstance;
    }

    public Observable<Boolean> add(final MyQuestion question) {
        return Observable
            .fromCallable(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    final Uri uri = mResolver.insert(
                        MyQuestionsProvider.getAllUri(),
                        question.toContentValues());

                    return uri != null;
                }
            })
            .subscribeOn(Schedulers.io());
    }

    public Observable<List<MyQuestion>> getAll() {
        return Observable
            .fromCallable(new Callable<List<MyQuestion>>() {
                @Override
                public List<MyQuestion> call() throws Exception {
                    final List<MyQuestion> questions = new ArrayList<>();
                    final Cursor cursor = mResolver.query(
                        MyQuestionsProvider.getAllUri(),
                        // projection
                        null,
                        // selection, selection args
                        null, null,
                        // sort order
                        MyQuestion.Entry._ID + " DESC");
                    if (cursor == null) return questions;

                    if (cursor.moveToFirst()) {
                        do {
                            // Inflate the JSON.
                            final int dataCol = cursor.getColumnIndex(MyQuestion.Entry.COL_DATA);
                            final String data = cursor.getString(dataCol);
                            final MyQuestion.JsonWrap wrap = new Gson().fromJson(
                                data, MyQuestion.JsonWrap.class);

                            questions.add(new MyQuestion(wrap));
                        } while (cursor.moveToNext());
                    }

                    cursor.close();

                    return questions;
                }
            })
            .subscribeOn(Schedulers.io());
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    /**
     * The private constructor because it is a singleton.
     */
    private MyQuestionStore(@NonNull final Context context) {
        super(context.getContentResolver());

        mContext = new WeakReference<>(context);
    }

    @NonNull
    @Override
    protected Uri getRegistrationUri() {
        return MyQuestionsProvider.getAllUri();
    }

    @NonNull
    @Override
    protected ContentObserver createContentObserver() {
        return new ContentObserver(createHandler(
            MyQuestionStore.class.getSimpleName())) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                super.onChange(selfChange, uri);

                // Notify the observers by URI.
                final String action = uri.getQueryParameter(Const.DB_ACTION_KEY);
                if (!TextUtils.isEmpty(action)) {
                    if (action.equals(Const.DB_ACTION_VALUE_INSERT)) {
                        // TODO
                    } else if (action.equals(Const.DB_ACTION_VALUE_DELETE)) {
                        // TODO
                    } else if (action.equals(Const.DB_ACTION_VALUE_UPDATE)) {
                        // TODO
                    }
                }
            }
        };
    }

//    @Override
//    protected MyQuestion read(Cursor cursor) {
//        final int jsonIdx = cursor.getColumnIndex("json");
//        final String json = cursor.getString(jsonIdx);
//        return new Gson().fromJson(json, MyQuestion.class);
//    }
//
//    @NonNull
//    @Override
//    protected ContentValues getDefaultContentValuesForItem(MyQuestion item) {
//        final String json = new Gson().toJson(item);
//        final ContentValues values = new ContentValues();
//
//        values.put("json", json);
//
//        return values;
//    }
}
