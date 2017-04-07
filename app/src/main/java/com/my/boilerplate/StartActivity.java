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

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.my.boilerplate.data.GeoPlace;
import com.my.boilerplate.protocol.IOnClickObjectListener;
import com.my.boilerplate.util.PrefUtil;
import com.my.boilerplate.view.PlacesAutoCompleteAdapter;
import com.my.widget.IProgressBarView;
import com.my.widget.util.ViewUtil;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

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

    GeoPlace mTargetPlace;

    Toolbar mToolbar;
    EditText mSearchView;
    RecyclerView mSearchSuggestionView;
    PlacesAutoCompleteAdapter mSearchSuggestionViewAdapter;
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

        // Google API client.
        mGoogleApiClient = new GoogleApiClient
            .Builder(this, this, this)
            .addApi(Places.GEO_DATA_API)
            .addApi(Places.PLACE_DETECTION_API)
            .enableAutoManage(this, this)
            .build();
        mGoogleApiClient.connect();

        // Search.
        mSearchView = (EditText) findViewById(R.id.search_text);
        mSearchView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v,
                                      boolean hasFocus) {
                if (!hasFocus) {
                    mSearchSuggestionViewAdapter.clear();
                }
            }
        });
        mSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s,
                                          int start,
                                          int count,
                                          int after) {
                // DO NOTHING.
            }

            @Override
            public void onTextChanged(CharSequence s,
                                      int start,
                                      int before,
                                      int count) {
                mSearchSuggestionViewAdapter.getFilter().filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // DO NOTHING.
            }
        });
        mSearchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v,
                                          int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    updateMapByAddress(v.getText().toString());
                    return true;
                } else {
                    return false;
                }
            }
        });
        mSearchSuggestionViewAdapter = new PlacesAutoCompleteAdapter(
            this, mGoogleApiClient, R.layout.view_places_suggestion_item, null, null, this);
        mSearchSuggestionView = (RecyclerView) findViewById(R.id.search_suggestion);
        mSearchSuggestionView.setLayoutManager(new LinearLayoutManager(
            this, LinearLayoutManager.VERTICAL, false));
        mSearchSuggestionView.setHasFixedSize(true);
        mSearchSuggestionView.setAdapter(mSearchSuggestionViewAdapter);

        // Avatar.
        mAvatarView = (ImageView) findViewById(R.id.avatar_image);
        mAvatarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StartActivity.this,
                                         MyQuestionsActivity.class));
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
                if (mTargetPlace == null) return;

                // Pass address info
                startActivity(new Intent(StartActivity.this,
                                         NewQuestionActivity.class)
                                  .putExtra(Const.PARAMS_TARGET_PLACE, mTargetPlace));
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mSearchView.isFocused()) {
            mSearchView.clearFocus();
        } else {
            super.onBackPressed();
        }
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
        final GeoPlace suggestion = (GeoPlace) data;
        mSearchView.setText(suggestion.fullAddress);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    private void updateMapByAddress(final String s) {
        Observable
            .create(new ObservableOnSubscribe<Place>() {
                @Override
                public void subscribe(final ObservableEmitter<Place> e)
                    throws Exception {
                    // Submit the query to the autocomplete API and retrieve a PendingResult that will
                    // contain the results when the query completes.
                    final PendingResult<AutocompletePredictionBuffer> results =
                        Places.GeoDataApi
                            .getAutocompletePredictions(mGoogleApiClient, s, null, null);

                    // This method should have been called off the main UI thread. Block and wait for at most 60s
                    // for a result from the API.
                    final AutocompletePredictionBuffer predictions = results
                        .await(60, TimeUnit.SECONDS);

                    // Confirm that the query completed successfully, otherwise return null
                    final Status status = predictions.getStatus();
                    if (!status.isSuccess()) {
                        predictions.release();
                        throw new Exception("Error contacting API: " + status.toString());
                    }

                    Log.i("xyz", "Query completed. Received " + predictions.getCount()
                                 + " predictions.");

                    // Copy the results into our own data structure, because we can't hold onto the buffer.
                    // AutocompletePrediction objects encapsulate the API response (place ID and fullAddress).
                    // Choose the first result.
                    final String placeId = predictions.get(0).getPlaceId();
                    Places.GeoDataApi
                        .getPlaceById(mGoogleApiClient, placeId)
                        .setResultCallback(new ResultCallback<PlaceBuffer>() {
                            @Override
                            public void onResult(@NonNull PlaceBuffer places) {
                                if (places.getStatus().isSuccess() && places.getCount() > 0) {
                                    e.onNext(places.get(0));
                                    e.onComplete();
                                }
                            }
                        });

                    // Release the buffer now that all data has been copied.
                    predictions.release();
                }
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new DisposableObserver<Place>() {
                @Override
                public void onNext(Place place) {
                    // Clear the suggestion.
                    mSearchSuggestionViewAdapter.clear();

                    final LatLng location = place.getLatLng();

                    mTargetPlace = new GeoPlace(place.getId(),
                                                place.getAddress().toString());

                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(location));
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                        new CameraPosition.Builder()
                            .target(location)
                            .zoom(17)
                            .build()));
                }

                @Override
                public void onError(Throwable e) {
                    Toast.makeText(StartActivity.this,
                                   e.getMessage(),
                                   Toast.LENGTH_SHORT)
                         .show();
                }

                @Override
                public void onComplete() {
                    // DO NOTHING.
                }
            });
    }
}
