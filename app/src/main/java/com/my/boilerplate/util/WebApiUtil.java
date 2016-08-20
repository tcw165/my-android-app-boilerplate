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

package com.my.boilerplate.util;

import android.content.Context;

import com.my.boilerplate.json.JsonWhatever;
import com.my.boilerplate.net.IWhateverApiService;
import com.my.boilerplate.view.IProgressBarView;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

public class WebApiUtil {

    private static final String TAG = WebApiUtil.class.getSimpleName();

    public static final String BASE_URL = "http://cook-buddy.herokuapp.com/";

    private static final WeakHashMap<Context, WebApiUtil> sInstancePool = new WeakHashMap<>();

    private final WeakReference<Context> mContext;
    private IWhateverApiService mService;

    public static WebApiUtil with(final Context context) {
        if (!sInstancePool.containsKey(context)) {
            sInstancePool.put(context, new WebApiUtil(context));
        }

        return sInstancePool.get(context);
    }

    /**
     * Return the HTTP service and you probably would manually activate/deactivate
     * the progress bar.
     */
    public IWhateverApiService api() {
        return mService;
    }

    /**
     * Get certain JSON from the server and show the progress bar for you.
     */
    public Observable<JsonWhatever> getJsonWhatever() {
        return mService
            .getJsonWhatever()
            .subscribeOn(Schedulers.io())
            .doOnSubscribe(new Action0() {
                @Override
                public void call() {
                    showProgressBar();
                }
            })
            .observeOn(AndroidSchedulers.mainThread())
            .doOnCompleted(new Action0() {
                @Override
                public void call() {
                    hideProgressBar();
                }
            })
            .observeOn(AndroidSchedulers.mainThread());
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    private WebApiUtil(final Context context) {
        OkHttpClient httpClient = new OkHttpClient();

        // TODO: Timeout?

        // TODO: Interceptor?
//        httpClient.networkInterceptors().add(new Interceptor() {
//            public com.squareup.okhttp.Response intercept(Chain chain) throws IOException {
//                Request.Builder builder = chain.request().newBuilder();
//
//                System.out.println("Adding headers:" + headers);
//                for (Map.Entry<String, String> entry : headers.entrySet()) {
//                    builder.addHeader(entry.getKey(), entry.getValue());
//                }
//
//                return chain.proceed(builder.build());
//            }
//        });

        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build();

        mContext = new WeakReference<>(context);
        mService = retrofit.create(IWhateverApiService.class);
    }

    private void showProgressBar() {
        if (mContext.get() == null ||
            !(mContext.get() instanceof IProgressBarView)) return;

        ((IProgressBarView) mContext.get()).showProgressBar();
    }

    private void hideProgressBar() {
        if (mContext.get() == null ||
            !(mContext.get() instanceof IProgressBarView)) return;

        ((IProgressBarView) mContext.get()).hideProgressBar();
    }
}
