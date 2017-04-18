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
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.my.boilerplate.LongOperationReceiver.ILongOperation;
import com.my.core.protocol.IProgressBarView;
import com.my.core.util.ViewUtil;
import com.my.widget.adapter.SampleMenuAdapter;
import com.my.widget.adapter.SampleMenuAdapter.SampleMenuItem;

public class ServiceSampleActivity
    extends AppCompatActivity
    implements IProgressBarView {

    protected Toolbar mToolbar;
    protected ListView mMenu;

    protected LongOperationReceiver mLongOperationReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_services_sample);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mMenu = (ListView) findViewById(R.id.menu);
        mMenu.setAdapter(onCreateSampleMenu());
        mMenu.setOnItemClickListener(onClickSampleMenuItem());

        mLongOperationReceiver = new LongOperationReceiver(onReceiveLongOperation());
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(mLongOperationReceiver,
                         LongOperationReceiver.createIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(mLongOperationReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void showProgressBar() {
        ViewUtil
            .with(this)
            .showAsProgressBar(true)
            .setProgressBarCancelable(false)
            .showProgressBar(getResources().getString(R.string.loading));
    }

    @Override
    public void hideProgressBar() {
        ViewUtil
            .with(this)
            .hideProgressBar();
    }

    @Override
    public void updateProgress(int progress) {
        ViewUtil
            .with(this)
            .updateProgress(progress);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    @SuppressWarnings({"unchecked"})
    private SampleMenuAdapter onCreateSampleMenu() {
        return new SampleMenuAdapter(
            this,
            new SampleMenuItem[]{
                new SampleMenuItem(
                    "Launch an immortal Service",
                    "Let's see how long could the service last and " +
                    "see what the Services lifecycle is like.",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startService(new Intent(ServiceSampleActivity.this,
                                                    ImmortalService.class));
                        }
                    }),
                new SampleMenuItem(
                    "Launch a long operation Service",
                    "It will show the progress when it's processing " +
                    "the long operation and terminate itself when " +
                    "done. The DialogFragment will be present when " +
                    "the task is under processing and be hidden " +
                    "when the task is completed.\n" +
                    "Bug: The activity should be in the foreground " +
                    "in case it misses to receive the complete notification.",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startService(new Intent(ServiceSampleActivity.this,
                                                    LongOperationService.class));
                        }
                    })
            });
    }

    private AdapterView.OnItemClickListener onClickSampleMenuItem() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent,
                                    View view,
                                    int position,
                                    long id) {
                final SampleMenuItem item = (SampleMenuItem) parent.getAdapter()
                                                                   .getItem(position);
                item.onClickListener.onClick(view);
            }
        };
    }

    protected ILongOperation onReceiveLongOperation() {
        return new ILongOperation() {
            @Override
            public void onProgressStart() {
                showProgressBar();
            }

            @Override
            public void onProgressPause() {
                // DO NOTHING.
            }

            @Override
            public void onProgressCancel() {
                // DO NOTHING.
            }

            @Override
            public void onProgressComplete() {
                hideProgressBar();
            }

            @Override
            public void onProgressUpdate(int progress) {
                updateProgress(progress);
            }
        };
    }
}
