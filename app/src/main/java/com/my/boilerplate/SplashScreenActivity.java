// Copyright (c) 2016-present boyw165
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

package com.my.boilerplate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;
import com.my.boilerplate.util.PrefUtil;

import java.util.concurrent.TimeUnit;

import io.fabric.sdk.android.Fabric;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Init Fabric crashlytics.
        Fabric.with(this, new Crashlytics());

//        setContentView(R.layout.activity_splash_screen);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Observable
            .just(true)
            .delay(0, TimeUnit.MILLISECONDS)
            .subscribe(new Consumer<Boolean>() {
                @Override
                public void accept(Boolean ignored) throws Exception {
                    if (TextUtils.isEmpty(
                        PrefUtil.getString(getApplicationContext(),
                                           PrefUtil.PREF_AVATAR_IMAGE_PATH))) {
                        // If not login.
                        startActivity(new Intent(SplashScreenActivity.this,
                                                 LoginActivity.class));
                    } else {
                        startActivity(new Intent(SplashScreenActivity.this,
                                                 StartActivity.class));
                    }
                    finish();
                }
            });
    }
}
