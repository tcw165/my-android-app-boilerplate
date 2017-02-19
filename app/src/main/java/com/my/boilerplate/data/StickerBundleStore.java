package com.my.boilerplate.data;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.my.boilerplate.StickerBundleProvider;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class StickerBundleStore extends AbstractContentProviderStore<StickerBundle> {

    private static StickerBundleStore sInstance;

    // Rx subjects.
    final Subject<Cursor> mBundlesSub = PublishSubject.create();

    // External reference.
    @SuppressWarnings("unused")
    private WeakReference<Context> mContext;

    // Network.
    private OkHttpClient mHttpClient;

    public static StickerBundleStore with(final Context context) {
        if (sInstance == null) {
            sInstance = new StickerBundleStore(context);
        }
        return sInstance;
    }

    public synchronized boolean getBundleListFromServerSync()
        throws IOException {
        // TODO: Complete it.
        final Request request = new Request.Builder()
            // See R.raw.sticker_bundle_config_v${number}.json
            .url(StickerBundleConfig
                     .getConfig(mContext.get())
                     .getUrlOfBundleList())
            .build();
        final Response response = mHttpClient.newCall(request).execute();

        // TODO: Try to update the present table only.
        // Convert to structure we recognized and insert to the database.
        final List<StickerBundle> bundles = new Gson()
            .fromJson(response.body().charStream(),
                      StickerBundleWrap.class)
            .bundles();
        for (StickerBundle bundle : bundles) {
            mResolver.insert(
                StickerBundleProvider.getBundleInsertionUri(),
                StickerBundleProvider.bundleToContentValues(bundle));
        }

        return true;
    }

    @SuppressWarnings("unused")
    public Observable<Boolean> getBundleListFromServerAsync() {
        return Observable
            .create(new ObservableOnSubscribe<Boolean>() {
                @Override
                public void subscribe(ObservableEmitter<Boolean> e)
                    throws Exception {
                    // TODO: Support interrupts.
                    e.onNext(getBundleListFromServerSync());
                    e.onComplete();

                }
            })
            .subscribeOn(Schedulers.io());
    }

    public Cursor getBundlesSync() {
        // TODO: Complete it.
        final Uri uri = StickerBundleProvider.getBundleListUri();
        Cursor cursor = mResolver.query(
            uri, null, null, null, null);

        if (cursor == null) {
            // Using "update" command to download the bundle list
            // from the server.
            if (mResolver.update(uri, null, null, null) > 0) {
                cursor = mResolver.query(
                    uri, null, null, null, null);
            }
        }
        return null;
    }

    public Observable<Cursor> getBundlesAsync() {
        return Observable
            .create(new ObservableOnSubscribe<Cursor>() {
                @Override
                public void subscribe(ObservableEmitter<Cursor> e)
                    throws Exception {
                    e.onNext(getBundlesSync());
                    e.onComplete();
                }
            })
            .subscribeOn(Schedulers.io());
    }

    public StickerBundle getBundleByIdSync(String id) {
        // TODO: Check if it is downloaded. If not, download it after fulfilling
        // TODO: the requirements.
        return null;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    /**
     * The private constructor because it is a singleton.
     */
    private StickerBundleStore(@NotNull final Context context) {
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
        return StickerBundleProvider.getBundlePrefixUri();
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
                switch (StickerBundleProvider.getUriAction(uri)) {
                    case StickerBundleProvider.ACTION_QUERY_STICKER_BUNDLE_ALL: {
//                        mBundlesSub.onNext(cursor);
                        break;
                    }
                    case StickerBundleProvider.ACTION_QUERY_STICKER_BUNDLE: {
                        // TODO: Complete it.
                        break;
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
