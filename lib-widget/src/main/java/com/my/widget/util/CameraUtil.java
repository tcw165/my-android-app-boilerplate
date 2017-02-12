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
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CameraUtil {

    private static final String TAG = CameraUtil.class.getSimpleName();

    private static int frontCameraId = -1;
    private static int backCameraId = -1;

    private static final Comparator<Camera.Size> sAreaComparator = new Comparator<Camera.Size>() {
        @Override
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            return Long.signum((long) lhs.width * lhs.height -
                               (long) rhs.width * rhs.height);
        }
    };

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

    public static int getDisplayOrientation(int cameraId) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);

        return info.orientation;
    }

    public static int getOptimalDisplayOrientation(Context context,
                                                   int cameraId) {
        if (cameraId < 0 || cameraId > getNumberOfCameras()) {
            return -1;
        }
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);

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
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
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

    public static Camera.Size getOptimalPreviewSize(Camera camera,
                                                    int displayOrientation,
                                                    int preferredWidth,
                                                    int preferredHeight) {
        if (camera == null) return null;

        // Ensure orientation is [0 .. 360].
        displayOrientation %= 360;
        // Consider the camera display orientation.
        final int preferredRotatedWidth = displayOrientation == 90 || displayOrientation == 270 ?
            preferredHeight : preferredWidth;
        final int preferredRotatedHeight = displayOrientation == 90 || displayOrientation == 270 ?
            preferredWidth : preferredHeight;
        final float preferredAr = (float) preferredRotatedWidth / preferredRotatedHeight;
        final Camera.Parameters parameters = camera.getParameters();
        final List<Camera.Size> choices = parameters.getSupportedPreviewSizes();
        // Collect the supported resolutions that are at least as big as the preview Surface
        final List<Camera.Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        final List<Camera.Size> notBigEnough = new ArrayList<>();
        for (Camera.Size option : choices) {
            // Skip mismatched aspect-ration approximately.
            final float optAr = (float) option.width / option.height;
            if (!approximatelyEqual(preferredAr, optAr, 0.25f)) continue;

            if (option.width >= preferredRotatedWidth &&
                option.height >= preferredRotatedHeight) {
                bigEnough.add(option);
            } else {
                notBigEnough.add(option);
            }
        }
        Log.d(TAG, "prefer size: (" + preferredRotatedWidth + "," + preferredRotatedHeight + ")");
        Log.d(TAG, "all support preview size: " + dumpPreviewSizeList(choices));
        Log.d(TAG, "all big-enough preview size: " + dumpPreviewSizeList(bigEnough));
        Log.d(TAG, "all not-bit-enough preview size: " + dumpPreviewSizeList(notBigEnough));

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size() > 0) {
            Camera.Size size = Collections.min(bigEnough, sAreaComparator);
            Log.d(TAG, "Choose big-enough size=(" + size.width + "," + size.height + ")");
            return size;
        } else if (notBigEnough.size() > 0) {
            Camera.Size size = Collections.max(notBigEnough, sAreaComparator);
            Log.d(TAG, "Choose not-big-enough size=(" + size.width + "," + size.height + ")");
            return size;
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices.get(0);
        }
    }

    public static String dumpPreviewSizeList(List<Camera.Size> sizes) {
        String result = "";
        for (Camera.Size size : sizes) {
            result += "(" + size.width + "," + size.height + ") ";
        }
        if (result.isEmpty()) {
            result = "0";
        }
        return result;
    }

    public static void enableAutoFocusModel(final Camera camera) {
        if (camera == null) return;

        Camera.Parameters params = camera.getParameters();
        // Check if the camera supports auto-focus mode.
        boolean ifSupportAutoFocus = false;
        for (String mode : params.getSupportedFocusModes()) {
            if (mode.equals(Camera.Parameters.FOCUS_MODE_AUTO)) {
                ifSupportAutoFocus = true;
                break;
            }
        }
        if (!ifSupportAutoFocus) return;

        // Set auto-focus mode.
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        camera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                Log.d("xyz", "auto-focus mode: " + success);
                // Set the focus area.
                Camera.Parameters params = camera.getParameters();
                List<Camera.Area> areas = new ArrayList<>();
                areas.add(new Camera.Area(new Rect(-500, -500, 500, 500), 1));
                params.setFocusAreas(areas);
                camera.setParameters(params);
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    @SuppressWarnings("unused")
    private static boolean approximatelyEqual(float a,
                                              float b,
                                              float epsilon) {
        final float absA = Math.abs(a);
        final float absB = Math.abs(b);
        return Math.abs(a - b) <= ((absA < absB ? absB : absA) * epsilon);
    }
}
