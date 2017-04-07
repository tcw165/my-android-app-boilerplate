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

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.my.boilerplate.protocol.IOnClickObjectListener;
import com.my.boilerplate.util.PrefUtil;
import com.my.widget.IProgressBarView;
import com.my.widget.util.ViewUtil;
import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;

public class StartActivity
    extends AppCompatActivity
    implements GoogleApiClient.ConnectionCallbacks,
               GoogleApiClient.OnConnectionFailedListener,
               OnMapReadyCallback,
               IProgressBarView,
               IOnClickObjectListener {

    GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    SupportMapFragment mMapFragment;

    Toolbar mToolbar;
    EditText mSearchView;
    ImageView mAvatarView;
    TextView mBtnDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_start);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Search.
        mSearchView = (EditText) findViewById(R.id.search_text);
        mSearchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v,
                                          int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
//                    final String addr = v.getText();

                    return true;
                } else {
                    return false;
                }
            }
        });

        // Avatar.
        mAvatarView = (ImageView) findViewById(R.id.avatar_image);
        mAvatarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StartActivity.this,
                                         MyRequestsActivity.class));
            }
        });
        // Load avatar.
        Glide.with(this)
             .load(PrefUtil.getString(this, PrefUtil.PREF_AVATAR_IMAGE_PATH))
             .into(mAvatarView);

        // Button.
        mBtnDone = (TextView) findViewById(R.id.btn_done);
        mBtnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Pass address info
                startActivity(new Intent(StartActivity.this,
                                         NewRequestsActivity.class));
            }
        });

        // Google API client.
        mGoogleApiClient = new GoogleApiClient
            .Builder(this, this, this)
            .addApi(Places.GEO_DATA_API)
            .addApi(Places.PLACE_DETECTION_API)
            .enableAutoManage(this, this)
            .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Google Map.
        mMapFragment = (SupportMapFragment) getSupportFragmentManager()
            .findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this,
                       "Google API connection is suspended.",
                       Toast.LENGTH_SHORT)
             .show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this,
                       "Google API connection is failed.",
                       Toast.LENGTH_SHORT)
             .show();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(
//            new MarkerOptions()
//                .position(sydney)
//                .title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        // Request ACCESS_FINE_LOCATION permission.
        RxPermissions
            .getInstance(this)
            .request(Manifest.permission.ACCESS_FINE_LOCATION)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new DisposableObserver<Boolean>() {
                @Override
                public void onNext(Boolean granted) {
                    if (!granted) {
                        final String msg = "ACCESS_FINE_LOCATION";
                        // Show a toast.
                        Toast.makeText(StartActivity.this, msg, Toast.LENGTH_SHORT)
                             .show();
                    }

                    if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                        StartActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        // Access to the location has been granted to the app.
                        mMap.setMyLocationEnabled(true);
                    }
                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onComplete() {

                }
            });
    }

    @Override
    public void showProgressBar() {
        ViewUtil
            .with(this)
            .setProgressBarCancelable(false)
            .showProgressBar(getString(R.string.loading));
    }

    @Override
    public void hideProgressBar() {
        ViewUtil
            .with(this)
            .hideProgressBar();
    }

    @Override
    public void updateProgress(int progress) {
        showProgressBar();
    }

    @Override
    public void onClickObject(View view,
                              Object data) {
//        final String imagePath = (String) data;
//
        // DO NOTHING.
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

}
