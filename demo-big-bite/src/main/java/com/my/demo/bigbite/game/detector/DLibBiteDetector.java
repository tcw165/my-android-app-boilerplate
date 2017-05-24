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

package com.my.demo.bigbite.game.detector;

import android.util.Log;

import com.my.demo.bigbite.game.data.IBiteDetector;
import com.my.jni.dlib.data.DLibFace;

import java.util.List;

public class DLibBiteDetector implements IBiteDetector {

    private static final double TWO_PI = 2f * Math.PI;

    /**
     * Landmarks of inner lip.
     * <pre>
     *
     * 0    1  2  3    4
     * +----+--+--+----+
     *  \  7       5  /
     *     +   6   +
     *       \ + /
     *
     * </pre>
     */

    /**
     * Left-upper vector of inner lip where from index 0 to index 1.
     */
    private static final int[] L_UP = new int[]{0, 2};
    /**
     * Left-lower vector of inner lip where from index 0 to index 1.
     */
    private static final int[] L_LOW = new int[]{0, 6};
    /**
     * Right-upper vector of inner lip where from index 0 to index 1.
     */
    private static final int[] R_UP = new int[]{4, 2};
    /**
     * Right-lower vector of inner lip where from index 0 to index 1.
     */
    private static final int[] R_LOW = new int[]{4, 6};

    private float[] mLeftUpVec = new float[]{0f, 0f};
    private float[] mLeftLowVec = new float[]{0f, 0f};
    private float[] mRightUpVec = new float[]{0f, 0f};
    private float[] mRightLowVec = new float[]{0f, 0f};

    private long mTimeStamp = 0;

    @Override
    public boolean detect(DLibFace face) {
        if (face == null ||
            face.getInnerLipsLandmarks().size() == 0) {
            throw new IllegalArgumentException("Given face is invalid");
        }

        final List<DLibFace.Landmark> marks = face.getInnerLipsLandmarks();

        // Update the vectors.
        mLeftUpVec[0] = marks.get(L_UP[1]).x - marks.get(L_UP[0]).x;
        mLeftUpVec[1] = marks.get(L_UP[1]).y - marks.get(L_UP[0]).y;
        mLeftLowVec[0] = marks.get(L_LOW[1]).x - marks.get(L_LOW[0]).x;
        mLeftLowVec[1] = marks.get(L_LOW[1]).y - marks.get(L_LOW[0]).y;
        mRightUpVec[0] = marks.get(R_UP[1]).x - marks.get(R_UP[0]).x;
        mRightUpVec[1] = marks.get(R_UP[1]).y - marks.get(R_UP[0]).y;
        mRightLowVec[0] = marks.get(R_LOW[1]).x - marks.get(R_LOW[0]).x;
        mRightLowVec[1] = marks.get(R_LOW[1]).y - marks.get(R_LOW[0]).y;
        Log.d("xyz123", String.format("mLeftUpVec=(x=%.5f, y=%.5f), mLeftLowVec=(x=%.5f, y=%.5f), " +
                                      "mRightUpVec=(x=%.5f, y=%.5f), mRightLowVec=(x=%.5f, y=%.5f)",
                                      mLeftUpVec[0], mLeftUpVec[1],
                                      mLeftLowVec[0], mLeftLowVec[1],
                                      mRightUpVec[0], mRightUpVec[1],
                                      mRightLowVec[0], mRightLowVec[1]));

        // Calculate the angles of both left and right side.
        double leftUpAngle = Math.atan2((double) mLeftUpVec[1], (double) mLeftUpVec[0]);
        double leftLowAngle = Math.atan2((double) mLeftLowVec[1], (double) mLeftLowVec[0]);
        double rightUpAngle = Math.atan2((double) mRightUpVec[1], (double) mRightUpVec[0]);
        double rightLowAngle = Math.atan2((double) mRightLowVec[1], (double) mRightLowVec[0]);
        Log.d("xyz123", String.format("leftUpAngle=%.5f, leftLowAngle=%.5f, " +
                                      "rightUpAngle=%.5f, rightLowAngle=%.5f",
                                      Math.toDegrees(leftUpAngle),
                                      Math.toDegrees(leftLowAngle),
                                      Math.toDegrees(rightUpAngle),
                                      Math.toDegrees(rightLowAngle)));

        // Calculate the left and right angle of the mouth.
        double leftAngle = Math.abs(leftUpAngle - leftLowAngle);
        // It's possible to get a large angle, but what I want is a small angle.
        if (leftAngle > Math.PI) {
            leftAngle  = TWO_PI - leftAngle;
        }
        double rightAngle = Math.abs(rightUpAngle - rightLowAngle);
        if (rightAngle > Math.PI) {
            rightAngle  = TWO_PI - rightAngle;
        }

        // Calculate the min/max angle of the mouth.
        final double minAngle = Math.min(leftAngle, rightAngle);
        final double maxAngle = Math.max(leftAngle, rightAngle);

        Log.d("xyz123", String.format("Mouth opening degrees, min=%.3ff, max=%.3f",
                                      Math.toDegrees(minAngle),
                                      Math.toDegrees(maxAngle)));

        return false;
    }

    @Override
    public int getBiteCount() {
        return 0;
    }
}
