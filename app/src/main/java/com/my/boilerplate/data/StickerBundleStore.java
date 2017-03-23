package com.my.boilerplate.data;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.my.boilerplate.Const;
import com.my.boilerplate.StickerBundleProvider;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class StickerBundleStore extends AbstractContentProviderStore<StickerBundle> {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({QUERY_LV_FULL,
             QUERY_LV_PART_SMALL,
             QUERY_LV_PART_MEDIUM,
             QUERY_LV_PART_BIG})
    public @interface QUERY_LEVEL {
        // enum alternative
    }

    public static final int QUERY_LV_FULL = (1 << 1);
    public static final int QUERY_LV_PART_SMALL = (1 << 2);
    public static final int QUERY_LV_PART_MEDIUM = (1 << 3);
    public static final int QUERY_LV_PART_BIG = (1 << 4);

    private static StickerBundleStore sInstance;

    // Rx subjects.
    final Subject<Cursor> mBundlesSub = PublishSubject.create();

    // External reference.
    @SuppressWarnings("unused")
    WeakReference<Context> mContext;

    // Network.
    OkHttpClient mHttpClient;

    public static StickerBundleStore with(final Context context) {
        if (sInstance == null) {
            sInstance = new StickerBundleStore(context);
        }
        return sInstance;
    }

    @SuppressWarnings("unused")
    public Observable<List<StickerBundle>> getBundleListFromServer() {
        return Observable
            .create(new ObservableOnSubscribe<List<StickerBundle>>() {
                @Override
                public void subscribe(ObservableEmitter<List<StickerBundle>> e)
                    throws Exception {
                    // TODO: Support interrupts.
                    try {
                        final Request request = new Request.Builder()
                            // See R.raw.sticker_bundle_config_v${number}.json
                            .url(StickerBundleConfig
                                     .getLatestConfig(mContext.get())
                                     .getUrlOfBundleList())
                            .build();
                        final Response response = mHttpClient.newCall(request).execute();

                        // TODO: Try to update the present table only.
                        // Deserialize the response to remote format.
                        final List<StickerBundle.RemoteJson> remoteBundles = new Gson()
                            .fromJson(response.body().charStream(),
                                      StickerBundleWrap.class).bundles;
                        final List<StickerBundle> clientBundles = new ArrayList<>();
                        for (StickerBundle.RemoteJson remoteBundle : remoteBundles) {
                            // Convert remote format to client format.
                            final StickerBundle clientBundle = new StickerBundle(remoteBundle);
                            clientBundles.add(clientBundle);
                            mResolver.insert(
                                StickerBundleProvider.getBundleUri(),
                                clientBundle.toContentValues());
                        }

                        e.onNext(clientBundles);
                    } catch (IOException ignored) {
                        e.onNext(null);
                    }

                    e.onComplete();
                }
            })
            .subscribeOn(Schedulers.io());
    }

    @SuppressWarnings("unused")
    public Observable<Boolean> isBundleListPresent() {
        return getBundleList(QUERY_LV_PART_SMALL)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .flatMap(new Function<Cursor, ObservableSource<Boolean>>() {
                @Override
                public ObservableSource<Boolean> apply(Cursor cursor)
                    throws Exception {
                    try {
                        return Observable.just(cursor.getCount() > 0);
                    } catch (Throwable ignored) {
                        return Observable.just(false);
                    } finally {
                        cursor.close();
                    }
                }
            });
    }

    @SuppressWarnings("unused")
    public Observable<Cursor> getBundleList(@QUERY_LEVEL final int level) {
        return Observable
            .create(new ObservableOnSubscribe<Cursor>() {
                @Override
                public void subscribe(ObservableEmitter<Cursor> e)
                    throws Exception {
                    switch (level) {
                        case QUERY_LV_PART_SMALL:
                            e.onNext(mResolver.query(
                                StickerBundleProvider.getBundleUri(),
                                // projection
                                new String[]{StickerBundle.Entry._ID,
                                             StickerBundle.Entry.COL_NAME,
                                             StickerBundle.Entry.COL_COVER,
                                             StickerBundle.Entry.COL_PRICE,
                                             StickerBundle.Entry.COL_TITLE,
                                             StickerBundle.Entry.COL_BUNDLE_ITEM_LIST},
                                // selection, selection args
                                null, null,
                                // sort order
                                StickerBundle.Entry._ID + " DESC"));
                        case QUERY_LV_PART_MEDIUM:
                        case QUERY_LV_PART_BIG:
                            e.onNext(mResolver.query(
                                StickerBundleProvider.getBundleUri(),
                                // projection
                                new String[]{StickerBundle.Entry._ID,
                                             StickerBundle.Entry.COL_NAME,
                                             StickerBundle.Entry.COL_COVER,
                                             StickerBundle.Entry.COL_PRICE,
                                             StickerBundle.Entry.COL_TITLE,
                                             StickerBundle.Entry.COL_BUNDLE_ITEM_LIST,
                                             StickerBundle.Entry.COL_DOWNLOAD_URL,
                                             StickerBundle.Entry.COL_INSTALL_REQUIREMENT,
                                             StickerBundle.Entry.COL_PROMOTION},
                                // selection, selection args
                                null, null,
                                // sort order
                                StickerBundle.Entry._ID + " DESC"));
                        case QUERY_LV_FULL:
                        default:
                            e.onNext(mResolver.query(
                                StickerBundleProvider.getBundleUri(),
                                // projection
                                null,
                                // selection, selection args
                                null, null,
                                // sort order
                                StickerBundle.Entry._ID + " DESC"));
                    }

                    e.onComplete();
                }
            })
            .subscribeOn(Schedulers.io());
    }

    public Observable<StickerBundle> getBundle(final long id) {
        return Observable
            .create(new ObservableOnSubscribe<StickerBundle>() {
                @Override
                public void subscribe(ObservableEmitter<StickerBundle> e)
                    throws Exception {
                    // TODO: Check if it is downloaded. If not, download it after fulfilling
                    // TODO: the requirements.
                    final Cursor bundleCursor = mResolver.query(
                        StickerBundleProvider.getBundleUri(id),
                        // projection
                        null,
                        // selection, selection args
                        null, null,
                        // sort order
                        StickerBundle.Entry._ID + " DESC");
                    if (bundleCursor != null && bundleCursor.getCount() > 0) {
                        // We care about the first one only and it should be the only one
                        // too.
                        bundleCursor.moveToFirst();
                        final StickerBundle bundle = new StickerBundle(bundleCursor);
                        // Close cursor.
                        bundleCursor.close();

                        e.onNext(bundle);
                    } else {
                        e.onNext(null);
                    }

                    e.onComplete();
                }
            });
    }

    public Observable<StickerBundle> installBundle(final long id) {
        return getBundle(id)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .flatMap(new Function<StickerBundle, ObservableSource<StickerBundle>>() {
                @Override
                public ObservableSource<StickerBundle> apply(StickerBundle bundle)
                    throws Exception {
                    if (bundle == null) return null;

                    final Request request = new Request.Builder()
                        // See R.raw.sticker_bundle_config_v${number}.json
                        .url(bundle.downloadUrl)
                        .build();
                    final Response response = mHttpClient.newCall(request).execute();

                    // TODO: Save file and unzip it.
//                    File file = mContext.get().getExternalFilesDir()

                    // TODO: Try to update the present table only.
                    // Deserialize the response to remote format.

                    return null;
                }
            });
//             Observable
//            .create(new ObservableOnSubscribe<StickerBundle>() {
//                @Override
//                public void subscribe(ObservableEmitter<StickerBundle> e)
//                    throws Exception {
//                    // TODO:
////                    e.onNext(installBundle(id));
//                    e.onComplete();
//                }
//            });
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    /**
     * The private constructor because it is a singleton.
     */
    private StickerBundleStore(@NonNull final Context context) {
        super(context.getContentResolver());

        mContext = new WeakReference<>(context);
        // Network.
        mHttpClient = new OkHttpClient.Builder()
            .connectTimeout(3000, TimeUnit.MILLISECONDS)
            .readTimeout(3000, TimeUnit.MILLISECONDS)
            .writeTimeout(10000, TimeUnit.MILLISECONDS)
            .build();
    }

    @NonNull
    @Override
    protected Uri getRegistrationUri() {
        return StickerBundleProvider.getBundleUri();
    }

    @NonNull
    @Override
    protected ContentObserver createContentObserver() {
        return new ContentObserver(createHandler(
            StickerBundleStore.class.getSimpleName())) {
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
//    protected StickerBundle read(Cursor cursor) {
//        final int jsonIdx = cursor.getColumnIndex("json");
//        final String json = cursor.getString(jsonIdx);
//        return new Gson().fromJson(json, StickerBundle.class);
//    }
//
//    @NonNull
//    @Override
//    protected ContentValues getDefaultContentValuesForItem(StickerBundle item) {
//        final String json = new Gson().toJson(item);
//        final ContentValues values = new ContentValues();
//
//        values.put("json", json);
//
//        return values;
//    }
}
