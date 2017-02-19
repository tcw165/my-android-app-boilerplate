package com.my.boilerplate.data;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.my.boilerplate.R;

import java.io.InputStreamReader;
import java.io.Reader;

public class StickerBundleConfig {

    private static StickerBundleConfig sInstance;

    @SuppressWarnings("unused")
    @SerializedName("version")
    private int mVersion = 1;

    @SuppressWarnings("unused")
    @SerializedName("urlOfBundleList")
    private String mUrlOfBundleList = null;

    @SuppressWarnings("unused")
    @SerializedName("encryptionKey")
    private String mEncryptionKey = null;

    @SuppressWarnings("unused")
    @SerializedName("ruleOfBundleFile")
    private String mRuleOfBundleFile = null;

    /**
     * See {@link R.raw#sticker_bundle_config_v1}.
     */
    public static StickerBundleConfig getConfig(final Context context) {
        // TODO: Downward compatibility.
        return getConfig(context, R.raw.sticker_bundle_config_v1);
    }

    public static StickerBundleConfig getConfig(final Context context,
                                                final int res) {
        if (context == null) return null;
        if (sInstance == null) {
            final Reader reader = new InputStreamReader(
                context.getResources()
                       .openRawResource(res));
            sInstance = new Gson().fromJson(reader, StickerBundleConfig.class);
        }

        return sInstance;
    }

    public int getVersion() {
        return mVersion;
    }

    public String getUrlOfBundleList() {
        return mUrlOfBundleList;
    }

    public String getEncryptionKey() {
        return mEncryptionKey;
    }

    public String getRuleOfBundleFile() {
        return mRuleOfBundleFile;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////
}
