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

package com.my.demo.bigbite.game.reactive;

import android.graphics.RectF;

import com.my.demo.bigbite.game.data.IBiteDetector;
import com.my.demo.bigbite.game.event.action.DetectBiteAction;
import com.my.demo.bigbite.game.event.result.DetectBiteResult;
import com.my.jni.dlib.data.DLibFace;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.Scheduler;
import io.reactivex.functions.Function;

public class DLibBiteDetectorTransformer implements ObservableTransformer<DetectBiteAction, DetectBiteResult> {

    final IBiteDetector mBiteDetector;
    final Scheduler mWorkerScheduler;

    public DLibBiteDetectorTransformer(final IBiteDetector detector,
                                       final Scheduler workerScheduler) {
        mBiteDetector = detector;
        mWorkerScheduler = workerScheduler;
    }

    @Override
    public ObservableSource<DetectBiteResult> apply(Observable<DetectBiteAction> upstream) {
        return upstream
            .observeOn(mWorkerScheduler)
            .map(new Function<DetectBiteAction, DetectBiteResult>() {
                @Override
                public DetectBiteResult apply(DetectBiteAction action)
                    throws Exception {
                    final DLibFace face = action.message;

                    // Find boundary.
                    float left = Integer.MAX_VALUE;
                    float top = Integer.MAX_VALUE;
                    float right = Integer.MIN_VALUE;
                    float bottom = Integer.MIN_VALUE;
                    for (DLibFace.Landmark landmark : face.getInnerLipsLandmarks()) {
                        left = Math.min(left, landmark.x);
                        top = Math.min(top, landmark.y);
                        right = Math.max(right, landmark.x);
                        bottom = Math.max(bottom, landmark.y);
                    }
//                    Log.d("mouth", String.format("Lips bound=%s", new RectF(left, top, right, bottom)));

                    mBiteDetector.detect(action.message);

                    return new DetectBiteResult(
                        new RectF(left, top, right, bottom),
                        mBiteDetector.getBiteCount());
                }
            });
    }
}
