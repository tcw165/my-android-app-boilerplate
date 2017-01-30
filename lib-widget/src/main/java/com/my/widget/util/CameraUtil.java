// Copyright (c) 2016-present boyw165
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

package com.my.widget.util;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import java.util.List;

public class CameraUtil {

    private static final String TAG = CameraUtil.class.getSimpleName();

    private static int frontCameraId = -1;
    private static int backCameraId = -1;

    public static int getNumberOfCameras() {
        return Camera.getNumberOfCameras();
    }

    public static Camera.CameraInfo getCameraInfo(int id) {
        if (id >= 0 && id < getNumberOfCameras()) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(id, cameraInfo);
            return cameraInfo;
        } else {
            return null;
        }
    }

    public static boolean hasFrontCamera() {
        return getFrontCameraId() != -1;
    }

    public static boolean hasBackCamera() {
        return getBackCameraId() != -1;
    }

    public static boolean isFrontCamera(int cameraId) {
        if (cameraId >= 0 &&
            cameraId < Camera.getNumberOfCameras()) return false;

        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);

        return info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT;
    }

    public static boolean isBackCamera(int cameraId) {
        if (cameraId >= 0 &&
            cameraId < Camera.getNumberOfCameras()) return false;

        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);

        return info.facing == Camera.CameraInfo.CAMERA_FACING_BACK;
    }

    /**
     * Return the first front camera's ID.
     */
    public static int getFrontCameraId() {
        if (frontCameraId == -1) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            for (int i = 0; i < getNumberOfCameras(); i++) {
                Camera.getCameraInfo(i, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    frontCameraId = i;
                    break;
                }
            }
        }
        return frontCameraId;
    }

    /**
     * Return the first back camera's ID.
     */
    public static int getBackCameraId() {
        if (backCameraId == -1) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            for (int i = 0; i < getNumberOfCameras(); i++) {
                Camera.getCameraInfo(i, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    backCameraId = i;
                    break;
                }
            }
        }
        return backCameraId;
    }

    public static int getDisplayOrientation(Context context,
                                            int cameraId) {
        if (cameraId < 0 || cameraId > getNumberOfCameras()) {
            return -1;
        }
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);

        int rotation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
            .getDefaultDisplay()
            .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (isFrontCamera(cameraId)) {
            result = (cameraInfo.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (cameraInfo.orientation - degrees + 360) % 360;
        }

        return result;
    }

    public static int[] getClosestFpsRange(Camera camera,
                                           int minFrameRate,
                                           int maxFrameRate) {
        minFrameRate *= 1000;
        maxFrameRate *= 1000;
        Camera.Parameters parameters = camera.getParameters();
        int minIndex = 0;
        int minDiff = Integer.MAX_VALUE;
        List<int[]> rangeList = parameters.getSupportedPreviewFpsRange();
        Log.d(TAG, "support preview fps range list: " + dumpFpsRangeList(rangeList));

        for (int i = 0; i < rangeList.size(); i++) {
            int[] fpsRange = rangeList.get(i);
            if (fpsRange.length != 2) {
                continue;
            }
            int minFps = fpsRange[0] / 1000;
            int maxFps = fpsRange[1] / 1000;
            int diff = Math.abs(minFps - minFrameRate) + Math.abs(maxFps - maxFrameRate);
            if (diff < minDiff) {
                minDiff = diff;
                minIndex = i;
            }
        }

        return rangeList.get(minIndex);
    }

    public static String dumpFpsRangeList(List<int[]> rangeList) {
        String result = "";
        for (int[] range : rangeList) {
            if (range.length != 2) {
                continue;
            }
            result += "(" + range[0] + "," + range[1] + ") ";
        }
        return result;
    }

    public static Camera.Size getClosetPreviewSize(Camera camera,
                                                   int preferWidth,
                                                   int preferHeight) {
        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> allSupportSizes = parameters.getSupportedPreviewSizes();
        Log.d(TAG, "prefer size: " + preferWidth + "x" + preferHeight);
        Log.d(TAG, "all support preview size: " + dumpPreviewSizeList(allSupportSizes));
        int minDiff = Integer.MAX_VALUE;
        int index = 0;

        for (int i = 0; i < allSupportSizes.size(); i++) {
            Camera.Size size = allSupportSizes.get(i);
            int supportWidth = size.width;
            int supportHeight = size.height;

            int diff = Math.abs(supportWidth - preferWidth) +
                       Math.abs(supportHeight - preferHeight);
            if (diff < minDiff) {
                minDiff = diff;
                index = i;
            }
        }
        Log.d(TAG, "select size: " + allSupportSizes.get(index).width +
                   "x" + allSupportSizes.get(index).height);

        return allSupportSizes.get(index);
    }

    public static String dumpPreviewSizeList(List<Camera.Size> sizes) {
        String result = "";
        for (Camera.Size size : sizes) {
            result += "(" + size.width + "," + size.height + ") ";
        }
        return result;
    }
}
