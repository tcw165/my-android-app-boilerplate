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

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;

import com.google.protobuf.InvalidProtocolBufferException;
import com.my.jni.dlib.data.Face;
import com.my.jni.dlib.data.Messages;

import java.util.ArrayList;
import java.util.List;

public class FaceLandmarksDetector {

    public FaceLandmarksDetector() {
        // TODO: Load library in worker thread?
        try {
            System.loadLibrary("c++_shared");
            Log.d("jni", "libc++_shared.so is loaded");
        } catch (UnsatisfiedLinkError error) {
            throw new RuntimeException(
                "\"c++_shared\" not found; check that the correct native " +
                "libraries are present in the APK.");
        }

        // TODO: Load library in worker thread?
        try {
            System.loadLibrary("my_core_jni");
            Log.d("jni", "my_core_jni.so is loaded");
        } catch (UnsatisfiedLinkError error) {
            throw new RuntimeException(
                "\"my_core_jni\" not found; check that the correct native " +
                "libraries are present in the APK.");
        }

        // TODO: Load library in worker thread?
        try {
            System.loadLibrary("protobuf-lite-3.2.0");
            Log.d("jni", "libprotobuf-lite-3.2.0.so is loaded");
        } catch (UnsatisfiedLinkError error) {
            throw new RuntimeException(
                "\"protobuf-lite-3.2.0\" not found; check that the correct " +
                "native libraries are present in the APK.");
        }

        // TODO: Load library in worker thread?
        try {
            System.loadLibrary("dlib");
            Log.d("jni", "libdlib.so is loaded");
        } catch (UnsatisfiedLinkError error) {
            throw new RuntimeException(
                "\"dlib\" not found; check that the correct native libraries " +
                "are present in the APK.");
        }

        // TODO: Load library in worker thread?
        try {
            System.loadLibrary("dlib_jni");
            Log.d("jni", "libdlib_jni.so is loaded");
        } catch (UnsatisfiedLinkError error) {
            throw new RuntimeException(
                "\"dlib_jni\" not found; check that the correct native " +
                "libraries are present in the APK.");
        }
    }

    public List<Face.Landmark> findLandmarksInFace(Bitmap bitmap,
                                                   Rect bound)
        throws InvalidProtocolBufferException {
        // Call detector JNI.
        final byte[] rawData = detectLandmarksInFace(
            bitmap, bound.left, bound.top, bound.right, bound.bottom);
        final Messages.LandmarkList rawLandmarks = Messages.LandmarkList.parseFrom(rawData);
        Log.d("xyz", "Detect " + rawLandmarks.getLandmarksCount() +
                     " landmarks in the face");

        // Convert raw data to my data structure.
        final List<Face.Landmark> landmarks = new ArrayList<>();
        for (int i = 0; i < rawLandmarks.getLandmarksCount(); ++i) {
            final Messages.Landmark rawLandmark = rawLandmarks.getLandmarks(i);
            final Face.Landmark landmark  = new Face.Landmark(rawLandmark);

            landmarks.add(landmark);
        }

        return landmarks;
    }

    public List<Face> findFacesAndLandmarks(Bitmap bitmap)
        throws InvalidProtocolBufferException {
        // Do the face landmarks detection.
        final byte[] rawData = detectFacesAndLandmarks(bitmap);
        final Messages.FaceList rawFaces = Messages.FaceList.parseFrom(rawData);
        Log.d("xyz", "Detect " + rawFaces.getFacesCount() + " faces");

        // Convert raw data to my data structure.
        final List<Face> faces = new ArrayList<>();
        for (int i = 0; i < rawFaces.getFacesCount(); ++i) {
            final Messages.Face rawFace = rawFaces.getFaces(i);
            final Face face = new Face(rawFace);
            Log.d("xyz", "Face #" + i + "=" + face);

            faces.add(face);
        }

        return faces;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    public native boolean isFaceDetectorReady();

    public native boolean isFaceLandmarksDetectorReady();

    /**
     * Prepare (deserialize the graph) the face detector.
     */
    public native void prepareFaceDetector();

    /**
     * Prepare the face landmarks detector.
     *
     * @param path The model (serialized graph) file.
     */
    public native void prepareFaceLandmarksDetector(String path);

    /**
     * Detect all the faces from the given photo.
     *
     * @param bitmap The photo.
     * @return The byte array of serialized {@link List<Face>}.
     */
    private native byte[] detectFaces(Bitmap bitmap);

    /**
     * Detect landmarks per face.
     *
     * @param bitmap The small bitmap right covering a face.
     * @return The byte array of serialized {@link Face}.
     */
    private native byte[] detectLandmarksInFace(Bitmap bitmap,
                                                long left,
                                                long top,
                                                long right,
                                                long bottom);

    /**
     * Find the faces and landmarks from the given Bitmap.
     * <br/>
     * Before calling this method, make sure the face and landmarks detectors
     * are both initialized. Otherwise a {@link RuntimeException} would be fired.
     *
     * @param bitmap The bitmap.
     * @return The byte array of serialized {@link List<Face>}.
     */
    private native byte[] detectFacesAndLandmarks(Bitmap bitmap);
}
