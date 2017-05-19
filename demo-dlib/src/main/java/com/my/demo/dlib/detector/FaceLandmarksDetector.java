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

package com.my.demo.dlib.detector;

import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.my.core.util.ProfilerUtil;
import com.my.jni.dlib.IDLibFaceDetector;
import com.my.jni.dlib.data.DLibFace;

public class FaceLandmarksDetector extends Detector<DLibFace> {

    private final Detector<Face> mFaceDetector;
    private final IDLibFaceDetector mLandmarksDetector;

    public FaceLandmarksDetector(final Detector<Face> faceDetector,
                                 final IDLibFaceDetector landmarksDetector) {
        mFaceDetector = faceDetector;
        mLandmarksDetector = landmarksDetector;
    }

    @Override
    public SparseArray<DLibFace> detect(Frame frame) {
        ProfilerUtil.startProfiling();

        SparseArray<Face> faces = mFaceDetector.detect(frame);
        Log.d("xyz", "Detect "  + faces + " (took " + ProfilerUtil.stopProfiling() + "ms)");

//        frame.getBitmap();
//        mLandmarksDetector.findLandmarksInFace()

        return null;
    }

    public static class PostProcessor implements Detector.Processor<DLibFace> {

        @Override
        public void release() {

        }

        @Override
        public void receiveDetections(Detections<DLibFace> detections) {
//            detections.getDetectedItems().size();
        }
    }
}
