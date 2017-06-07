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

package com.my.reactive.result;

public class MsgProgressResult extends ProgressResult {

    public final String message;

    public static MsgProgressResult inProgress(String msg,
                                               int progress) {
        return new MsgProgressResult(true, false, msg, 0, null);
    }

    public static MsgProgressResult succeed(String msg) {
        return new MsgProgressResult(false, true, msg, 100, null);
    }

    public static MsgProgressResult failed(Throwable err) {
        return new MsgProgressResult(false, false, err.getMessage(), 0, err);
    }

    @Override
    public String toString() {
        return "MsgProgressResult{" +
               "isInProgress=" + isInProgress +
               ", isSuccessful=" + isSuccessful +
               ", message=\"" + message + "\"" +
               ", progress=" + progress +
               ", err=" + err +
               '}';
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    protected MsgProgressResult(boolean isInProgress,
                                boolean isSuccessful,
                                String msg,
                                int progress,
                                Throwable err) {
        super(isInProgress, isSuccessful, progress, err);
        this.message = msg;
    }
}
