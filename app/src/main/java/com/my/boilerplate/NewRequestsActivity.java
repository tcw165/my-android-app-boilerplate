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

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.my.boilerplate.data.GeoPlace;
import com.my.boilerplate.data.MyRequest;
import com.my.widget.IProgressBarView;
import com.my.widget.util.ViewUtil;

import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.internal.operators.flowable.FlowableOnErrorReturn;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.schedulers.TestScheduler;

public class NewRequestsActivity
    extends AppCompatActivity
    implements IProgressBarView {

    GeoPlace mTargetPlace;

    Toolbar mToolbar;
    TextView mAddress;
    RadioGroup mRequestsView;
    TextView mBtnDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_new_requests);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        final Bundle extra = getIntent().getExtras();

        mTargetPlace = extra.getParcelable(Const.PARAMS_TARGET_PLACE);
        if (mTargetPlace == null) {
            throw new IllegalArgumentException("Given place is null");
        }

        // Address.
        mAddress = (TextView) findViewById(R.id.address);
        mAddress.setText(mTargetPlace.fullAddress);

        // Requests.
        mRequestsView = (RadioGroup) findViewById(R.id.requests);

        // Button.
        mBtnDone = (TextView) findViewById(R.id.btn_done);
        mBtnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int requestId = -1;
                RadioButton option = null;
                for (int i = 0; i < mRequestsView.getChildCount(); ++i) {
                    final RadioButton child = (RadioButton) mRequestsView.getChildAt(i);

                    if (child.isChecked()) {
                        requestId = i;
                        option = child;
                        break;
                    }
                }
                if (requestId == -1) return;

                // TODO: Send request to the server.
                sendRequestToServer(
                    new MyRequest(mTargetPlace,
                                  requestId,
                                  option.getText().toString()));
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

//        // Check which radio button was clicked
//        switch(view.getId()) {
//            case R.id.radio_pirates:
//                if (checked)
//                    // Pirates are the best
//                    break;
//            case R.id.radio_ninjas:
//                if (checked)
//                    // Ninjas rule
//                    break;
//        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    void sendRequestToServer(final MyRequest request) {
        showProgressBar();

        // FIXME: Really send to the server.
        Observable
            .just(request)
            .delay(1000, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new DisposableObserver<MyRequest>() {
                @Override
                public void onNext(MyRequest value) {
                    hideProgressBar();

                    // Show greeting.
                    new AlertDialog.Builder(NewRequestsActivity.this)
                        .setMessage("Request is sent, please wait for a while. :)")
                        .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(NewRequestsActivity.this,
                                                         MyRequestsActivity.class));
                                finish();
                            }
                        })
                        .create()
                        .show();
                }

                @Override
                public void onError(Throwable e) {
                    // DO NOTHING.
                }

                @Override
                public void onComplete() {
                    // DO NOTHING.
                }
            });
    }
}
