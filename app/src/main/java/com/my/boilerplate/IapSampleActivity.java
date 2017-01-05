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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.my.comp.IapDelegateActivity;

public class IapSampleActivity extends AppCompatActivity {

    Toolbar mToolbar;
    Button mBtnBuy;
    Button mBtnReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_iap_sample);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mBtnBuy = (Button) findViewById(R.id.btn_buy_it);
        mBtnBuy.setOnClickListener(onClickBuyIt());

        mBtnReset = (Button) findViewById(R.id.btn_reset);
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this,
                               "Successfully purchase the item.",
                               Toast.LENGTH_SHORT)
                     .show();
            } else {
                Toast.makeText(this,
                               "Failed to purchase the item.",
                               Toast.LENGTH_SHORT)
                     .show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    OnClickListener onClickBuyIt() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivityForResult(
//                    new Intent(IapSampleActivity.this,
//                               IapDelegateActivity.class)
//                        .putExtra(IapDelegateActivity.KEY_SKU, "com.cardinalblue.piccollage.glitterny")
//                        .putExtra(IapDelegateActivity.KEY_PRICE, 1.99f),
//                    0);
                startActivityForResult(
                    new Intent(IapSampleActivity.this,
                               IapDelegateActivity.class),
                    0);
            }
        };
    }
}
