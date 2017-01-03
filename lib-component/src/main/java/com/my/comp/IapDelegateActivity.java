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

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.my.comp.util.iap.IabHelper;
import com.my.comp.util.iap.IabResult;
import com.my.comp.util.iap.Inventory;
import com.my.comp.util.iap.Purchase;

import java.util.ArrayList;
import java.util.List;

public class IapDelegateActivity extends AppCompatActivity {

    public static final String KEY_SKU = "key_sku";
    public static final String KEY_PRICE = "key_price";

    private IabHelper mIabHelper;
    private float mPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_iap_delegate);

        mPrice = getIntent().getFloatExtra(KEY_PRICE, 0f);

        // Setup IAB
        try {
            // Create the helper, passing it our context and the public key to
            // verify signatures with
            mIabHelper = new IabHelper(this, getString(R.string.iap_pub_key_google));

            // enable debug logging (for a production application, you should set
            // this to false).
            mIabHelper.enableDebugLogging(false);

            // Start setup. This is asynchronous and the specified listener
            // will be called once setup completes.
            mIabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                @Override
                public void onIabSetupFinished(IabResult result) {
                    purchase(getIntent().getStringExtra(KEY_SKU));
                }
            });
        } catch (Exception ignored) {
            // Catch all exceptions here when setup IAB, just skip the setup if
            // any exception occurs. Later, the informative message will show up
            // to users if user is trying to do billing procedure.
            leavePage(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Pass on the activity result to the helper for handling
        if (null == mIabHelper || !mIabHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void purchase(final String purchaseItemId) {
        if (TextUtils.isEmpty(purchaseItemId)) {
//            Utils.showToast(this, R.string.an_error_occurred, Toast.LENGTH_LONG);
            leavePage(false);
            return;
        }
        if (mIabHelper == null || !mIabHelper.isSetupDone()) {
            AlertDialogFragment alertDialog = AlertDialogFragment.newInstance(
                null,
                getString(R.string.iap_error),
                getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        leavePage(false);
                    }
                });
            if (!isFinishing()) {
                try {
                    alertDialog.show(getSupportFragmentManager(), "can not bind iab helper");
                } catch (IllegalStateException e) {
                    Log.d("xyz", e.toString());
                }
            }
            return;
        }
        final int requestCode = 701;
        try {
            mIabHelper.launchPurchaseFlow(this, purchaseItemId, requestCode, new IabHelper.OnIabPurchaseFinishedListener() {
                @Override
                public void onIabPurchaseFinished(IabResult result, final Purchase purchase) {
                    // If we were disposed of in the meantime, quit.
                    if (mIabHelper == null) return;

                    // If user already own product, `purchase` will be null here.
                    // So, we only check `result` here and check the case for
                    // item already own later.
                    if (result == null)
                        return;

                    boolean isItemAlreadyOwn = result.getResponse() == IabHelper.BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED;
                    if (result.isFailure() && !isItemAlreadyOwn) {
                        switch (result.getResponse()) {
                            // it's a issue from "you already own this item" with error callback. we should
                            // reset this iab item let user had chance to restore this item later.
                            // ref : https://trello.com/c/XzzLL9Jq/2270-5797-4-31-4-can-t-buy-backgrounds
                            //
                            case IabHelper.BILLING_RESPONSE_RESULT_ERROR:
                                List<String> skuList = new ArrayList<>();
                                skuList.add(purchaseItemId);
                                mIabHelper.queryInventoryAsync(true, skuList, new IabHelper.QueryInventoryFinishedListener() {
                                    @Override
                                    public void onQueryInventoryFinished(IabResult result, Inventory inv) {
                                        if (result.isFailure()) {
                                            return;
                                        }
                                        Purchase background = inv.getPurchase(purchaseItemId);
                                        if (purchase != null) {
                                            // we don't care about the callback from consume procedure
                                            mIabHelper.consumeAsync(background, null);
                                        }
                                    }
                                });
                                break;
                        }
                        return;
                    }

                    if (isItemAlreadyOwn) {
//                        Utils.showToast(IapDelegateActivity.this,
//                                        R.string.item_already_owned_restore_download_bundle,
//                                        Toast.LENGTH_SHORT);
                    }
                    if (purchase != null) {
//                        LogEventUtils.sendTranscationEvent(purchase.getOrderId(),
//                                                           purchase.getPackageName(),
//                                                           purchase.getSku(),
//                                                           mPrice);
                    } else {
//                        LogEventUtils.sendException(new PictureFiles.Exception(result.getMessage()));
                    }
                    leavePage(true);
                }});
        } catch (IllegalStateException e) {
            // We observed some illegal states internally:
            // 1. Use billing function before IAB helper is setup, or
            // 2. Try starting another async task before previous is finished.
//            Utils.showToast(this, R.string.can_not_purchase_items_temporally,
//                            Toast.LENGTH_LONG);
            leavePage(false);
        }
    }

    private void leavePage(boolean successful) {
        if (successful) {
            setResult(RESULT_OK);
        } else {
            setResult(RESULT_CANCELED);
        }
        finish();
    }
}
