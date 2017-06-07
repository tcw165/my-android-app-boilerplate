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

package com.my.demo.bigbite.game.event.uiEvent;

import android.util.SparseArray;

import com.my.reactive.uiEvent.UiEvent;

public final class FrameUiEvent<T> extends UiEvent<SparseArray<T>> {

    private static final int FIRST_FRAME = 0;
    private static final int REPEATED_FRAME = 1;

    public static <T> FrameUiEvent<T> firstFrame(SparseArray<T> data) {
        return new FrameUiEvent<>(FIRST_FRAME, data);
    }

    public static <T> FrameUiEvent<T> repeatedFrame(SparseArray<T> data) {
        return new FrameUiEvent<>(REPEATED_FRAME, data);
    }

    public boolean isFirstFrame() {
        return state == FIRST_FRAME;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    private FrameUiEvent(int state, SparseArray<T> data) {
        super(state, data);
    }
}
