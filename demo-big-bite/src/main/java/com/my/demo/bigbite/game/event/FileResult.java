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

package com.my.demo.bigbite.game.event;

import com.my.demo.bigbite.event.RxResult;

import java.io.File;

public class FileResult extends RxResult {

    public final File file;

    public static FileResult inProgress() {
        return new FileResult(true, false, null, null);
    }

    public static FileResult succeed(File file) {
        return new FileResult(false, true, file, null);
    }

    public static FileResult failed(Throwable err) {
        return new FileResult(false, false, null, err);
    }

    @Override
    public String toString() {
        return "FileResult{" +
               ", isInProgress=" + isInProgress +
               ", isSuccessful=" + isSuccessful +
               ", file=" + file +
               ", err=" + err +
               '}';
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    protected FileResult(boolean isInProgress,
                         boolean isSuccessful,
                         File file,
                         Throwable err) {
        super(isInProgress, isSuccessful, err);
        this.file = file;
    }
}
