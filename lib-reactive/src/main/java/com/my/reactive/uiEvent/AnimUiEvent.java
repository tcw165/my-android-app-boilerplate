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

package com.my.reactive.uiEvent;

public final class AnimUiEvent extends UiEvent<Object> {

    private static final int START = 0;
    private static final int ANIMATING = 1;
    private static final int END = 2;
    private static final int CANCEL = 3;

    private static final Object DATA = new Object();

    public static AnimUiEvent start() {
        return new AnimUiEvent(START);
    }

    public static AnimUiEvent animating() {
        return new AnimUiEvent(ANIMATING);
    }

    public static AnimUiEvent end() {
        return new AnimUiEvent(END);
    }

    public static AnimUiEvent cancel() {
        return new AnimUiEvent(CANCEL);
    }

    public boolean isJustStarted() {
        return this.state == START;
    }

    public boolean isAnimating() {
        return this.state == ANIMATING;
    }

    public boolean isEnd() {
        return this.state == END;
    }

    public boolean isCancelled() {
        return this.state == CANCEL;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    private AnimUiEvent(int state) {
        super(state, DATA);
    }
}
