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

package com.my.core.util;

import android.graphics.Matrix;

/**
 * A convenient class for getting tx, ty, sx, sy and rotation given a
 * {@link Matrix}.
 * <br/>
 * Usage:
 * <pre>
 *     // Usage 1:
 *     final TwoDTransformUtils util = new TwoDTransformUtils(matrix);
 *     util.getTranslationX();
 *
 *     // Usage 2:
 *     TwoDTransformUtils.getTranslationX(matrix);
 * </pre>
 */
public class MatrixUtil {

    // Given...
    private final float[] mValues;

    public MatrixUtil() {
        mValues = new float[9];
    }

    public MatrixUtil(Matrix matrix) {
        mValues = new float[9];

        // Get the values from the matrix.
        matrix.getValues(mValues);
    }

    @SuppressWarnings("unused")
    public void getValues(Matrix matrix) {
        matrix.getValues(mValues);
    }

    @SuppressWarnings("unused")
    public float getTranslationX() {
        return mValues[Matrix.MTRANS_X];
    }

    @SuppressWarnings("unused")
    public float getTranslationY() {
        return mValues[Matrix.MTRANS_Y];
    }

    @SuppressWarnings("unused")
    public float getScaleX() {
        // TODO: Has to take the negative scale into account.
        // [a, b, tx]   [ sx*cos  -sy*sin  ? ]
        // [c, d, ty] = [ sx*sin   sy*cos  ? ]
        // [0, 0,  1]   [    0        0    1 ]
        //  ^  ^   ^
        //  i  j   k hat (axis vector)
        final float a = mValues[Matrix.MSCALE_X];
        final float b = mValues[Matrix.MSKEW_X];

        return (float) Math.hypot(a, b);
    }

    @SuppressWarnings("unused")
    public float getScaleY() {
        // TODO: Has to take the negative scale into account.
        // [a, b, tx]   [ sx*cos  -sy*sin  ? ]
        // [c, d, ty] = [ sy*sin   sy*cos  ? ]
        // [0, 0,  1]   [    0        0    1 ]
        //  ^  ^   ^
        //  i  j   k hat (axis vector)
        final float c = mValues[Matrix.MSKEW_Y];
        final float d = mValues[Matrix.MSCALE_Y];

        return (float) Math.hypot(c, d);
    }

    @SuppressWarnings("unused")
    public float getRotationInDegrees() {
        // TODO: Has to take the negative scale into account.
        // [a, b, tx]   [ sx*cos  -sy*sin  ? ]
        // [c, d, ty] = [ sx*sin   sy*cos  ? ]
        // [0, 0,  1]   [    0        0    1 ]
        //  ^  ^   ^
        //  i  j   k hat (axis vector)
        final float a = mValues[Matrix.MSCALE_X];
        final float c = mValues[Matrix.MSKEW_Y];
        // From -pi to +pi.
        float radian = (float) Math.atan2(c, a);

        return (float) Math.toDegrees(radian);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Public Static Methods //////////////////////////////////////////////////

    @SuppressWarnings("unused")
    public static float getTranslationX(Matrix matrix) {
        if (matrix == null) {
            return 0.f;
        } else {
            return new MatrixUtil(matrix).getTranslationX();
        }
    }

    @SuppressWarnings("unused")
    public static float getTranslationY(Matrix matrix) {
        if (matrix == null) {
            return 0.f;
        } else {
            return new MatrixUtil(matrix).getTranslationY();
        }
    }

    /**
     * Get the scaleX from an affine transform matrix.
     *
     * @param matrix The affine transform matrix.
     */
    @SuppressWarnings("unused")
    public static float getScaleX(Matrix matrix) {
        if (matrix == null) {
            return 0.f;
        } else {
            return new MatrixUtil(matrix).getScaleX();
        }
    }

    /**
     * Get the scaleY from an affine transform matrix.
     *
     * @param matrix The affine transform matrix.
     */
    @SuppressWarnings("unused")
    public static float getScaleY(Matrix matrix) {
        if (matrix == null) {
            return 0.f;
        } else {
            return new MatrixUtil(matrix).getScaleY();
        }
    }

    @SuppressWarnings("unused")
    public static float getRotationInDegrees(Matrix matrix) {
        if (matrix == null) {
            return 0.f;
        } else {
            return new MatrixUtil(matrix).getRotationInDegrees();
        }
    }
}
