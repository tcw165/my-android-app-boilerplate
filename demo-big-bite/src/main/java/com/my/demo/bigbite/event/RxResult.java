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

package com.my.demo.bigbite.event;

public class RxResult {

    public final boolean isSuccessful, isInProgress;
    public final Throwable err;

    public static RxResult inProgress() {
        return new RxResult(true, false, null);
    }

    public static RxResult succeed() {
        return new RxResult(false, true, null);
    }

    public static RxResult failed(Throwable err) {
        return new RxResult(false, false, err);
    }

    @Override
    public String toString() {
        return "RxResult{" +
               "isInProgress=" + isInProgress +
               ", isSuccessful=" + isSuccessful +
               ", err=" + err +
               '}';
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    protected RxResult(boolean isInProgress,
                       boolean isSuccessful,
                       Throwable err) {
        this.isInProgress = isInProgress;
        this.isSuccessful = isSuccessful;
        this.err = err;
    }
}
