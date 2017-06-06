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

package com.my.demo.bigbite.event.misc;

import com.my.demo.bigbite.event.RxResult;

public class ProgressResult extends RxResult {

    public final int progress;

    public static ProgressResult inProgress(int progress) {
        return new ProgressResult(true, false, 0, null);
    }

    public static ProgressResult succeed() {
        return new ProgressResult(false, true, 100, null);
    }

    public static ProgressResult failed(Throwable err) {
        return new ProgressResult(false, false, 0, err);
    }

    @Override
    public String toString() {
        return "ProgressResult{" +
               ", isInProgress=" + isInProgress +
               ", isSuccessful=" + isSuccessful +
               ", progress=" + progress +
               ", err=" + err +
               '}';
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    protected ProgressResult(boolean isInProgress,
                             boolean isSuccessful,
                             int progress,
                             Throwable err) {
        super(isInProgress, isSuccessful, err);
        this.progress = progress;
    }
}
