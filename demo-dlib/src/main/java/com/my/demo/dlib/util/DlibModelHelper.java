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

package com.my.demo.dlib.util;

import android.os.Environment;
import android.util.Log;

import com.my.core.util.FileUtil;
import com.my.demo.dlib.data.IDlibFaceModelService;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class DlibModelHelper {

    private static final String BASE_URL = "http://dlib.net/files/";
    private static final String FACE68_ZIP_FILE = "shape_predictor_68_face_landmarks.dat.bz2";
    private static final String FACE68_FILE = "shape_predictor_68_face_landmarks.dat";

    private static DlibModelHelper sSingleton = null;

    public static DlibModelHelper getService() {
        if (sSingleton == null) {
            synchronized (DlibModelHelper.class) {
                if (sSingleton == null) {
                    sSingleton = new DlibModelHelper();
                }
            }
        }

        return sSingleton;
    }

    public Observable<File> downloadFace68Model(final String dirName) {
        // TODO: Skip downloading if the file is present (MD5).
        final File dir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS + "/" + dirName);
        final File downloadFile = new File(dir, FACE68_ZIP_FILE);
        final File unpackFile = new File(dir, FACE68_FILE);

        if (unpackFile.exists()) {
            return Observable
                .just(unpackFile);
        } else {
            return mApiServ
                .getFace68Model(FACE68_ZIP_FILE)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(new Function<ResponseBody, File>() {
                    @Override
                    public File apply(ResponseBody body)
                        throws Exception {
                        // Prepare the folder.
                        if (dir.mkdirs() || dir.isDirectory()) {

                            // Save the raw file from the internet.
                            if (!downloadFile.exists() && downloadFile.createNewFile()) {
                                FileUtil.copy(
                                    body.byteStream(),
                                    new FileOutputStream(downloadFile));
                            }
                            Log.d("xyz", "Archived model is downloaded.");

                            // Unpack the bz2 file.
                            final BZip2CompressorInputStream bzIs =
                                new BZip2CompressorInputStream(
                                    new BufferedInputStream(
                                        new FileInputStream(downloadFile)));
                            FileUtil.copy(bzIs, new FileOutputStream(unpackFile));
                            Log.d("xyz", "Unpack the archived model.");

                            // Delete the raw file.
                            if (downloadFile.delete()) {
                                Log.d("xyz", "Remove the archived model.");
                            }

                            return unpackFile;
                        } else {
                            throw new IOException(
                                String.format("Cannot download %s", FACE68_FILE));
                        }
                    }
                });
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    private final OkHttpClient mHttpClient;
    private final IDlibFaceModelService mApiServ;

    private DlibModelHelper() {
        mHttpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

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

        final Retrofit factory = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(mHttpClient)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build();

        // FIXME: Might introduce the memory leak.
        mApiServ = factory.create(IDlibFaceModelService.class);
    }
}
