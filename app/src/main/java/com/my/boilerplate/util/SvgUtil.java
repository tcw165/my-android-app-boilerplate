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

package com.my.boilerplate.util;

import android.graphics.PointF;

import java.util.List;

public class SvgUtil {

    public static boolean containsPoint(List<PointF> closedPath,
                                        PointF testPt) {
        if (closedPath == null ||
            testPt == null ||
            closedPath.size() < 2) return false;

        boolean isIntersected = false;
        for (int end = 0, start = closedPath.size() - 1;
             end < closedPath.size(); end++) {
            if (ifRayCrossesPathSegment(testPt,
                                        closedPath.get(start),
                                        closedPath.get(end))) {
                isIntersected = !isIntersected;
            }
            start = end;
        }

        return isIntersected;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    /**
     * According to the even-odd rule, you draw a ray from the testing point
     * to infinity in any direction and counting the number of path segments
     * from the given shape that ray crosses.
     * In our case, we draw the ray from the testing point to infinity in the
     * horizontal direction.
     * <br/>
     * e.g.
     * <pre>
     * ^                    ^
     * |       2            |  1
     * |      /             |   \
     * |  *--/-------...    | *--\--------... Ray from the testing point to
     * |    /  *-----...    |     \  *----... infinity.
     * |   1                |      2
     * +----------->        +----------->
     *   slope > 0     and    slope < 0
     *
     *  y - y1     y2 - y1
     * -------- = --------- = pathSlope
     *  x - x1     x2 - x1
     *
     * becomes...
     *
     *       y - y1
     * x = ---------- + x1
     *      pathSlope
     *
     * So if the ray crosses the path segment, the following must be
     * fulfilled:
     * 1) the y of testing point must be in the range of pathPt1.y and
     * pathPt2.y.
     * 2) the testing x should always be on the left side of the path
     * segment.
     * </pre>
     */
    public static boolean ifRayCrossesPathSegment(PointF rayStartPt,
                                                  PointF pathPt1,
                                                  PointF pathPt2) {
        if (rayStartPt.y == pathPt1.y && pathPt1.y == pathPt2.y) {
            // Horizontal line case. In case that the denominator is 0 when
            // calculating the slope reciprocal.
            return rayStartPt.x <= Math.max(pathPt1.x, pathPt2.x);
        } else if ((pathPt1.y >= rayStartPt.y) != (pathPt2.y >= rayStartPt.y)) {
            // It's not a horizontal line and the testing y is in the range
            // of the path segment.
            if (rayStartPt.x <= Math.min(pathPt1.x, pathPt2.x)) {
                // The ray always crosses with the path segment if the starting
                // x is less than or equal to the minimum x value of the path
                // segment.
                return true;
            } else if (rayStartPt.x > Math.max(pathPt1.x, pathPt2.x)) {
                // The ray always doesn't crosses with the path segment if the
                // starting x is larger than the minimum x value of the path
                // segment.
                return false;
            } else {
                float pathSlopeRecip = (pathPt2.x - pathPt1.x) / (pathPt2.y - pathPt1.y);
                return rayStartPt.x <= (rayStartPt.y - pathPt1.y) * pathSlopeRecip + pathPt1.x;
            }
        } else {
            return false;
        }
    }
}
