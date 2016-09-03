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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

/**
 * Reference: https://github.com/tbruyelle/RxPermissions.git
 */
public class PermUtil {

    private static final String TAG = PermUtil.class.getSimpleName();

    private static PermUtil sSingleton;

    public static PermUtil with(Activity activity) {
        if (sSingleton == null) {
            sSingleton = new PermUtil(activity.getApplicationContext());
        }

        return sSingleton;
    }

    ///////////////////////////////////////////////////////////////////////////

    private final Context mContext;

    // Contains all the current permission requests.
    // Once granted or denied, they are removed from it.
    private Map<String, PublishSubject<Permission>> mSubjects = new HashMap<>();

    PermUtil(final Context context) {
        mContext = context;
    }

    /**
     * Request permissions immediately, <b>must be invoked during initialization phase
     * of your application</b>.
     */
    public Observable<Boolean> request(final String... permissions) {
        return Observable
            .just(null)
            .compose(ensure(permissions));
    }

    /**
     * Request permissions immediately, <b>must be invoked during initialization phase
     * of your application</b>.
     */
    public Observable<Permission> requestEach(final String... permissions) {
        return Observable
            .just(null)
            .compose(ensureEach(permissions));
    }

    /**
     * Map emitted items from the source observable into {@code true} if permissions in parameters
     * are granted, or {@code false} if not.
     * <p/>
     * If one or several permissions have never been requested, invoke the related framework method
     * to ask the user if he allows the permissions.
     */
    public Observable.Transformer<Object, Boolean> ensure(final String... permissions) {
        // FIXME: Is it possible to make the transformer reusable instead of
        // FIXME: instantiating it every call.
        return new Observable.Transformer<Object, Boolean>() {
            @Override
            public Observable<Boolean> call(Observable<Object> o) {
                return request(o, permissions)
                    // Transform Observable<Permission> to Observable<Boolean>
                    .buffer(permissions.length)
                    .flatMap(new Func1<List<Permission>, Observable<Boolean>>() {
                        @Override
                        public Observable<Boolean> call(List<Permission> permissions) {
                            if (permissions.isEmpty()) {
                                // Occurs during orientation change, when the subject receives onComplete.
                                // In that case we don't want to propagate that empty list to the
                                // subscriber, only the onComplete.
                                return Observable.empty();
                            }
                            // Return true if all permissions are granted.
                            for (Permission p : permissions) {
                                if (!p.granted) {
                                    return Observable.just(false);
                                }
                            }
                            return Observable.just(true);
                        }
                    });
            }
        };
    }

    /**
     * Map emitted items from the source observable into {@link Permission} objects for each
     * permissions in parameters.
     * <p/>
     * If one or several permissions have never been requested, invoke the related framework method
     * to ask the user if he allows the permissions.
     */
    public Observable.Transformer<Object, Permission> ensureEach(final String... permissions) {
        return new Observable.Transformer<Object, Permission>() {
            @Override
            public Observable<Permission> call(Observable<Object> o) {
                return request(o, permissions);
            }
        };
    }

    /**
     * Returns true if the permission is already granted.
     * <p/>
     * Always true if SDK &lt; 23.
     */
    public boolean isGranted(String permission) {
        return ContextCompat.checkSelfPermission(mContext, permission) == PackageManager.PERMISSION_GRANTED;
    }

    void startShadowActivity(String[] permissions) {
        log("startShadowActivity " + TextUtils.join(", ", permissions));
        Intent intent = new Intent(mContext, PermissionActivity.class);
        intent.putExtra("permissions", permissions);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    void onRequestPermissionsResult(int requestCode,
                                    String permissions[],
                                    int[] grantResults) {
        for (int i = 0, size = permissions.length; i < size; i++) {
            log("onRequestPermissionsResult  " + permissions[i]);
            // Find the corresponding subject
            PublishSubject<Permission> subject = mSubjects.get(permissions[i]);
            if (subject == null) {
                // No subject found
                throw new IllegalStateException("RxPermissions.onRequestPermissionsResult invoked but didn't find the corresponding permission request.");
            }
            mSubjects.remove(permissions[i]);
            boolean granted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
            subject.onNext(new Permission(permissions[i], granted));
            subject.onCompleted();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    private Observable<Permission> request(final Observable<?> trigger,
                                           final String... permissions) {
        if (permissions == null || permissions.length == 0) {
            throw new IllegalArgumentException("RxPermissions.request/requestEach requires at least one input permission");
        }
        return oneOf(trigger, pending(permissions))
            .flatMap(new Func1<Object, Observable<Permission>>() {
                @Override
                public Observable<Permission> call(Object o) {
                    return request_(permissions);
                }
            });
    }

    private Observable<?> pending(final String... permissions) {
        for (String p : permissions) {
            if (!mSubjects.containsKey(p)) {
                return Observable.empty();
            }
        }
        return Observable.just(null);
    }

    private Observable<?> oneOf(Observable<?> trigger, Observable<?> pending) {
        if (trigger == null) {
            return Observable.just(null);
        } else {
            return Observable.merge(trigger, pending);
        }
    }

    private Observable<Permission> request_(final String... permissions) {
        List<Observable<Permission>> list = new ArrayList<>(permissions.length);
        List<String> unrequestedPermissions = new ArrayList<>();

        // In case of multiple permissions, we create a observable for each of them.
        // At the end, the observables are combined to have a unique response.
        for (String permission : permissions) {
            log("Requesting permission " + permission);
            if (isGranted(permission)) {
                // Already granted, or not Android M
                // Return a granted Permission object.
                list.add(Observable.just(new Permission(permission, true)));
                continue;
            }

            PublishSubject<Permission> subject = mSubjects.get(permission);
            // Create a new subject if not exists
            if (subject == null) {
                unrequestedPermissions.add(permission);
                subject = PublishSubject.create();
                mSubjects.put(permission, subject);
            }

            list.add(subject);
        }

        if (!unrequestedPermissions.isEmpty()) {
            startShadowActivity(unrequestedPermissions
                                    .toArray(new String[unrequestedPermissions.size()]));
        }
        return Observable.concat(Observable.from(list));
    }

    private void log(String message) {
        Log.d(TAG, message);
    }
}
