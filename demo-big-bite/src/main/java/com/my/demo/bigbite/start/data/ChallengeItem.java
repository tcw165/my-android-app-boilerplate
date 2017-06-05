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

package com.my.demo.bigbite.start.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChallengeItem implements IChallengeItem,
                                      Parcelable {

    String mPreviewUrl;
    List<String> mSpriteUrls = new CopyOnWriteArrayList<>();

    public ChallengeItem(final IChallengeItem other,
                         final String pathPrefix) {
        String prefix = "";
        if (pathPrefix != null && pathPrefix.length() > 0) {
            prefix = pathPrefix;
        }

        mPreviewUrl = prefix + other.getPreviewUrl();
        for (String url : other.getSpriteUrls()) {
            mSpriteUrls.add(prefix + url);
        }
    }

//    public ChallengeItem(final String previewUrl,
//                         final List<String> spriteUrls) {
//        mPreviewUrl = previewUrl;
//        mSpriteUrls.addAll(spriteUrls);
//    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mPreviewUrl);
        dest.writeStringList(this.mSpriteUrls);
    }

    public String getPreviewUrl() {
        return mPreviewUrl;
    }

    public List<String> getSpriteUrls() {
        return mSpriteUrls;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    private ChallengeItem(Parcel in) {
        this.mPreviewUrl = in.readString();
        this.mSpriteUrls = in.createStringArrayList();
    }

    public static final Creator<ChallengeItem> CREATOR =
        new Creator<ChallengeItem>() {

        @Override
        public ChallengeItem createFromParcel(Parcel source) {
            return new ChallengeItem(source);
        }

        @Override
        public ChallengeItem[] newArray(int size) {
            return new ChallengeItem[size];
        }
    };
}
