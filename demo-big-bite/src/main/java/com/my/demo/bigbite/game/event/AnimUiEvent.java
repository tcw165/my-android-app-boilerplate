//// Copyright (c) 2017-present boyw165
////
//// Permission is hereby granted, free of charge, to any person obtaining a copy
//// of this software and associated documentation files (the "Software"), to deal
//// in the Software without restriction, including without limitation the rights
//// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//// copies of the Software, and to permit persons to whom the Software is
//// furnished to do so, subject to the following conditions:
////
////    The above copyright notice and this permission notice shall be included in
//// all copies or substantial portions of the Software.
////
////    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//// THE SOFTWARE.
//
//package com.my.demo.bigbite.game.event;
//
//import com.my.demo.bigbite.event.UiEvent;
//
//public class AnimUiEvent<T> extends UiEvent {
//
//    private static final int START = 0;
//    private static final int ANIMATING = 1;
//    private static final int END = 2;
//    private static final int CANCEL = 3;
//
//    private T mData;
//
//    public static <TT> AnimUiEvent<TT> start() {
//        return new AnimUiEvent<>(START);
//    }
//
//    public static boolean isStart(AnimUiEvent event) {
//        return event.state == START;
//    }
//
//    public static <TT> AnimUiEvent<TT> animating() {
//        return new AnimUiEvent<>(ANIMATING);
//    }
//
//    public static boolean isAnimating(AnimUiEvent event) {
//        return event.state == ANIMATING;
//    }
//
//    public static <TT> AnimUiEvent<TT> end() {
//        return new AnimUiEvent<>(END);
//    }
//
//    public static boolean isEnd(AnimUiEvent event) {
//        return event.state == END;
//    }
//
//    public static <TT> AnimUiEvent<TT> cancel() {
//        return new AnimUiEvent<>(CANCEL);
//    }
//
//    public static boolean isCancel(AnimUiEvent event) {
//        return event.state == CANCEL;
//    }
//
//    public T getmData() {
//        return mData;
//    }
//
//    ///////////////////////////////////////////////////////////////////////////
//    // Protected / Private Methods ////////////////////////////////////////////
//
//    private AnimUiEvent(int state) {
//        this.state  = state;
//    }
//}
