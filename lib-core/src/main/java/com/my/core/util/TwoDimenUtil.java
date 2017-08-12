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

import android.graphics.PointF;

import java.util.List;

/**
 * 2D util class.
 */
public class TwoDimenUtil {

    public static final int RAY_CASTING_BY_EVEN_ODD = 0;
    public static final int RAY_CASTING_BY_NON_ZERO = 1;

    /**
     * Tell if a testing point is on the inside or outside of a closed path.
     *
     * @param closedPath A list of points representing a closed path.
     * @param testPt The testing point.
     * @param rayCastingMethod RAY_CASTING_BY_EVEN_ODD | RAY_CASTING_BY_NON_ZERO
     */
    @SuppressWarnings("unused")
    public static boolean ifPathContainsPoint(List<PointF> closedPath,
                                              PointF testPt,
                                              int rayCastingMethod) {
        if (closedPath == null ||
            testPt == null ||
            closedPath.size() < 2) return false;

        if (rayCastingMethod == RAY_CASTING_BY_EVEN_ODD) {
            // The winding counter in clockwise and counter-clockwise direction.
            boolean ifInside = false;
            for (int end = 0, start = closedPath.size() - 1;
                 end < closedPath.size(); end++) {
                if (ifRayCrossesPathSegmentByEvenOdd(testPt,
                                                     closedPath.get(start),
                                                     closedPath.get(end))) {
                    ifInside = !ifInside;
                }
                start = end;
            }

            return ifInside;
        } else if (rayCastingMethod == RAY_CASTING_BY_NON_ZERO) {
            // The winding counter in clockwise and counter-clockwise direction.
            int windingCounter = 0;
            for (int end = 0, start = closedPath.size() - 1;
                 end < closedPath.size(); end++) {
                windingCounter += ifRayCrossesPathSegmentByNonZero(testPt,
                                                                   closedPath.get(start),
                                                                   closedPath.get(end));
                start = end;
            }

            return windingCounter != 0;
        }

        return false;
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
     * <pre>
     *
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
     * Reference:
     * <br/>
     * https://en.wikipedia.org/wiki/Even%E2%80%93odd_rule
     */
    private static boolean ifRayCrossesPathSegmentByEvenOdd(PointF rayStartPt,
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

    /**
     * It is basically the same with the {@code ifRayCrossesPathSegmentByEvenOdd}.
     * Further more, this algorithm takes the winding direction into account too.
     * If the number of the path segments that crosses ray is non zero and the
     * winding counter is zero, the testing point is on the outside of the closed
     * path; If the winding counter is zero, the testing point is on the inside
     * of the closed path.
     * <br/>
     * <pre>
     *
     * 1) How to tell if the ray crosses the path segment?
     *
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
     * 2) How to tell if the path segment is in clockwise and counter-clockwise
     * winding?
     *
     * The clockwise examples:
     * ^               ^               ^
     * |   2           |      2        |
     * |   |           |     /         |
     * |   |           |    /          |
     * |   |           |   /           |
     * |   1           |  1            |  1-----2
     * +----------->   +----------->   +----------->
     *
     * The counter-clockwise examples:
     * ^               ^               ^
     * |   1           |      1        |
     * |   |           |     /         |
     * |   |           |    /          |
     * |   |           |   /           |
     * |   2           |  2            |  2-----1
     * +----------->   +----------->   +----------->
     *
     * </pre>
     * Reference:
     * <br/>
     * https://en.wikipedia.org/wiki/Nonzero-rule
     *
     * @return 0 = no cross;
     *         +1 = cross in clockwise winding direction;
     *         -1 = cross in counter-clockwise winding direction.
     */
    private static int ifRayCrossesPathSegmentByNonZero(PointF rayStartPt,
                                                        PointF pathPt1,
                                                        PointF pathPt2) {
        if (pathPt1.equals(pathPt2)) return 0;

        // +1 = clockwise.
        // -1 = counter-clockwise.
        int windingCounter = 0;
        if (rayStartPt.y == pathPt1.y && pathPt1.y == pathPt2.y &&
            rayStartPt.x <= Math.max(pathPt2.x, pathPt1.x)) {
            // Horizontal line case. In case that the denominator is 0 when
            // calculating the slope reciprocal.
            // Clockwise:         x2 > x1;
            // Counter-clockwise: x2 < x1;
            windingCounter = (int) Math.signum(pathPt1.x - pathPt2.x);
        } else if ((pathPt1.y >= rayStartPt.y) != (pathPt2.y >= rayStartPt.y) &&
                   rayStartPt.x <= Math.max(pathPt1.x, pathPt2.x)) {
            // 1) It's not a horizontal line and the testing y is in the range
            // of the path segment.
            // 2) The ray always doesn't crosses with the path segment if the
            // starting x is larger than the minimum x value of the path
            // segment.
            //
            // If the ray crosses the path segment, we calculate the winding
            // counter.
            // Clockwise         = y2 > y1;
            // Counter-clockwise = y2 < y1;
            float pathSlope = (pathPt2.y - pathPt1.y) / (pathPt2.x - pathPt1.x);
            if (rayStartPt.x <= (rayStartPt.y - pathPt1.y) / pathSlope + pathPt1.x) {
                windingCounter = (int) Math.signum(pathPt2.y - pathPt1.y);
            }
        }

        return windingCounter;
    }
}
