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

package my.demo.news.news;

import java.util.List;

import my.demo.news.event.StateEvent;

public final class UpdateViewEvent extends StateEvent {

    public List<INewsEntity> entities;

    public static UpdateViewEvent inProgress() {
        final UpdateViewEvent event = new UpdateViewEvent();

        event.state = STATE_IN_PROGRESS;

        return event;
    }

    public static UpdateViewEvent success(final List<INewsEntity> entities) {
        final UpdateViewEvent event = new UpdateViewEvent();

        event.state = STATE_SUCCESS;
        event.entities = entities;

        return event;
    }

    public static UpdateViewEvent failure(final Throwable err) {
        final UpdateViewEvent event = new UpdateViewEvent();

        event.state = STATE_FAILURE;
        event.err = err;

        return event;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    private UpdateViewEvent() {
        // DO NOTHING.
    }
}
