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

package com.my.comp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * An invisible Activity that helps you to handle the in-app-purchase and
 * broadcasts the result with {@link Intent} action, {@link #ACTION_IAP_OK} and
 * {@link #ACTION_IAP_CANCELED}.
 * <br/>
 * <br/>
 * Example 1, from an {@link android.app.Activity}:
 * <br/>
 * <pre>
 * // Use the {@link #startActivityForResult(Intent, int)} and subscribe
 * // the result in {@link #onActivityResult(int, int, Intent)}.
 * startActivityForResult(
 *     new Intent(this, IapDelegateActivity.class)
 *         .putExtra(PARAMS_SKU, "sku")
 *         .putExtra(PARAMS_SKU_PRICE, 1.99f));
 * </pre>
 * Example 2, from a {@link android.app.Service}:
 * <pre>
 * // Use the {@link #startActivity(Intent)} and subscribe the result in
 * // {@link android.content.BroadcastReceiver#onReceive(Context, Intent)}.
 * startActivity(
 *     new Intent(this, IapDelegateActivity.class)
 *         .putExtra(PARAMS_SKU, "sku")
 *         .putExtra(PARAMS_SKU_PRICE, 1.99f))
 * </pre>
 * The 2nd way is more organized in many ways. If your app has many entries of
 * the IAP, you could manage the codes simply in a {@link android.app.Service}
 * instead of having multiple similar codes spreading everywhere.
 */
public class IapDelegateActivity extends AppCompatActivity {

    /**
     * The IAP task is successful.
     */
    public static final String ACTION_IAP_OK = "action.IAP_OK";
    /**
     * The IAP task is failed.
     */
    public static final String ACTION_IAP_CANCELED = "action.IAP_CANCELED";


    public static final String PARAMS_SKU = "PARAMS_SKU";
    public static final String PARAMS_SKU_PRICE = "PARAMS_SKU_PRICE";

    String mSku;
    float mSkuPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_placeholder);

        final Intent intent = getIntent();
        mSku = intent.getStringExtra(PARAMS_SKU);
        mSkuPrice = intent.getFloatExtra(PARAMS_SKU_PRICE, 0f);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // TODO: Handle the IAP task.
    }

    @Override
    protected void onStop() {
        super.onStop();

        // TODO: Interrupt the IAP task if necessary.
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // TODO: Save the SKU and SKU price in case the "Don't Keep Activity"
        // TODO: is enabled.
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedState) {
        super.onRestoreInstanceState(savedState);

        // TODO: Restore the SKU and SKU price.
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {
        if (resultCode == RESULT_OK) {
            // Notify the subscribers.
            sendBroadcast(
                new Intent(ACTION_IAP_OK)
                    .putExtra(PARAMS_SKU, mSku)
                    .putExtra(PARAMS_SKU_PRICE, mSkuPrice));
        } else {
            // Notify the subscribers.
            sendBroadcast(
                new Intent(ACTION_IAP_CANCELED)
                    .putExtra(PARAMS_SKU, mSku)
                    .putExtra(PARAMS_SKU_PRICE, mSkuPrice));
        }

        setResult(resultCode);
        finish();
    }
}
