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

package com.my.boilerplate.data;

import android.content.ContentValues;
import android.provider.BaseColumns;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class MyQuestion {

    public static final long ANONYMOUS_ASKER_ID = -1;

    public final long createdTime;

    public final GeoPlace questionPlace;
    public final long questionId;
    public final String questionDescription;

    public MyQuestion(GeoPlace questionPlace,
                      long questionId,
                      String questionDescription) {
        this.questionPlace = questionPlace;
        this.questionId = questionId;
        this.questionDescription = questionDescription;

        // Create time.
        this.createdTime = new Date().getTime();
    }

    public MyQuestion(JsonWrap wrap) {
        this.createdTime = wrap.askTime;
        this.questionId = wrap.questionId;
        this.questionDescription = wrap.questionDescription;
        this.questionPlace = new GeoPlace(null, wrap.questionLocationAddress);
    }

    public ContentValues toContentValues() {
        final ContentValues values = new ContentValues();
        final String data = new Gson().toJson(new JsonWrap(this));

        values.put(Entry.COL_DATA, data);

        return values;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    /**
     * The database entries.
     */
    public static class Entry implements BaseColumns {

        public static final String TABLE_NAME = "my_questions";

        public static final String COL_DATA = "data";
    }

    public static class JsonWrap {

        @SerializedName("ask_time")
        public long askTime;

        @SerializedName("asker_id")
        public long askerId;

        @SerializedName("asker_avatar")
        public String askerAvatar;

        @SerializedName("question_id")
        public long questionId;

        @SerializedName("question_location_address")
        public String questionLocationAddress;

        @SerializedName("question_description")
        public String questionDescription;

        public JsonWrap(MyQuestion request) {
            askTime = request.createdTime;
            askerId = MyQuestion.ANONYMOUS_ASKER_ID;
            askerAvatar = null;
            questionId = request.questionId;
            questionLocationAddress = request.questionPlace.fullAddress;
            questionDescription = request.questionDescription;
        }
    }
}
