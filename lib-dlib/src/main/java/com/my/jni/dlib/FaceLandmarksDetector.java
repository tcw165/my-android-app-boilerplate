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

package com.my.jni.dlib;

import android.content.res.AssetManager;
import android.graphics.Bitmap;

public class FaceLandmarksDetector {

    final AssetManager mAssetManager;

    public FaceLandmarksDetector(final AssetManager manager) {
        mAssetManager = manager;

        // TODO: Load library in worker thread?
        try {
            System.loadLibrary("c++_shared");
        } catch (UnsatisfiedLinkError error) {
            throw new RuntimeException(
                "\"c++_shared\" not found; check that the correct native libraries " +
                "are present in the APK.");
        }

        // TODO: Load library in worker thread?
        try {
            System.loadLibrary("dlib");
        } catch (UnsatisfiedLinkError error) {
            throw new RuntimeException(
                "\"dlib\" not found; check that the correct native libraries " +
                "are present in the APK.");
        }

        // TODO: Load library in worker thread?
        try {
            System.loadLibrary("dlib_jni");
        } catch (UnsatisfiedLinkError error) {
            throw new RuntimeException(
                "\"dlib_jni\" not found; check that the correct native " +
                "libraries are present in the APK.");
        }
    }

    /**
     * Load default graph from assets.
     */
    public void loadShapeDetector(final String path) {
        deserializeShapeDetector(path);
//        try {
//            final InputStream is = mAssetManager.open(
//                "shape_predictor_68_face_landmarks.dat");
//            final byte[] data = new byte[is.available()];
//            final int numBytesRead = is.read(data);
//            final boolean loaded = numBytesRead == data.length && loadGraph(data);
//            is.close();
//
//            return loaded;
//        } catch (IOException error) {
//            return false;
//        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    public native void deserializeShapeDetector(String path);

    public native void deserializeFaceDetector();

    public native void findFaces(Bitmap bitmap);
}
