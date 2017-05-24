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

import android.os.SystemClock;
import android.util.Log;

import com.my.demo.bigbite.game.data.IBiteDetector;
import com.my.jni.dlib.data.DLibFace;

import java.util.ArrayList;
import java.util.List;

public class DLibBiteDetector implements IBiteDetector {

    private static final double TWO_PI = 2f * Math.PI;

    //
    // Landmarks of inner lip.
    //
    // 0    1  2  3    4
    // +----+--+--+----+
    //  \  7       5  /
    //     +   6   +
    //       \ + /
    //

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

    // Mouth state.
    private static final int MOUTH_UNDEFINED = 0;
    private static final int MOUTH_OPENED = 1;
    private static final int MOUTH_CLOSED = 2;
    private int mLastMouthState = MOUTH_UNDEFINED;

    // Records.
    private static final double ALPHA = 0.5f;
    private static final double DEGREE_FOR_MOUTH_OPENED = 18;
    private static final double DEGREE_FOR_MOUTH_CLOSED = 12;
    private static final int MAX_RECORDS = 10;
    private static final int CHECK_PAST_N_RECORDS = 2;
    private final List<Record> mRecords = new ArrayList<>();

    // Bite count.
    private int mBiteCount = 0;

    private final Object mMutex = new Object();

    @Override
    public boolean detect(DLibFace face) {
        if (face == null ||
            face.getInnerLipsLandmarks().size() == 0) {
            throw new IllegalArgumentException("Given face is invalid");
        }

        synchronized (mMutex) {
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
//        Log.d("Mouth", String.format("mLeftUpVec=(x=%.5f, y=%.5f), mLeftLowVec=(x=%.5f, y=%.5f), " +
//                                      "mRightUpVec=(x=%.5f, y=%.5f), mRightLowVec=(x=%.5f, y=%.5f)",
//                                      mLeftUpVec[0], mLeftUpVec[1],
//                                      mLeftLowVec[0], mLeftLowVec[1],
//                                      mRightUpVec[0], mRightUpVec[1],
//                                      mRightLowVec[0], mRightLowVec[1]));

            // Calculate the angles of both left and right side.
            double leftUpAngle = Math.atan2((double) mLeftUpVec[1], (double) mLeftUpVec[0]);
            double leftLowAngle = Math.atan2((double) mLeftLowVec[1], (double) mLeftLowVec[0]);
            double rightUpAngle = Math.atan2((double) mRightUpVec[1], (double) mRightUpVec[0]);
            double rightLowAngle = Math.atan2((double) mRightLowVec[1], (double) mRightLowVec[0]);
//        Log.d("Mouth", String.format("leftUpAngle=%.5f, leftLowAngle=%.5f, " +
//                                      "rightUpAngle=%.5f, rightLowAngle=%.5f",
//                                      Math.toDegrees(leftUpAngle),
//                                      Math.toDegrees(leftLowAngle),
//                                      Math.toDegrees(rightUpAngle),
//                                      Math.toDegrees(rightLowAngle)));

            // Calculate the left and right angle of the mouth.
            double leftAngle = Math.abs(leftUpAngle - leftLowAngle);
            // It's possible to get a large angle, but what I want is a small angle.
            if (leftAngle > Math.PI) {
                leftAngle = TWO_PI - leftAngle;
            }
            double rightAngle = Math.abs(rightUpAngle - rightLowAngle);
            if (rightAngle > Math.PI) {
                rightAngle = TWO_PI - rightAngle;
            }

            // Calculate the min/max angle of the mouth.
            final double minDegrees = Math.toDegrees(Math.min(leftAngle, rightAngle));
            final double maxDegrees = Math.toDegrees(Math.max(leftAngle, rightAngle));
            final double openingDegrees = Math.max(minDegrees, maxDegrees);

//            Log.d("Mouth", String.format("Mouth opening degrees, min=%.3ff, max=%.3f (ts=%d)",
//                                          minDegrees, maxDegrees,
//                                          SystemClock.currentThreadTimeMillis()));

            // Record the opening degree.
            record(openingDegrees);

            // Determine if it is a valid bite.
            return detectBiteFromRecords();
        }
    }

    @Override
    public int getBiteCount() {
        synchronized (mMutex) {
            return mBiteCount;
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    private long getTimestamp() {
        return SystemClock.currentThreadTimeMillis();
    }

    private void record(double mouthOpeningDegree) {
        final int size = mRecords.size();
        final Record record = new Record();

        // Determine the degree velocity.
        if (mRecords.isEmpty()) {
            record.mouthOpeningVelocity = 0;
            record.mouthOpeningDegree = mouthOpeningDegree;
            record.timestamp = SystemClock.currentThreadTimeMillis();
        } else {
            final Record last = mRecords.get(size - 1);

            record.timestamp = getTimestamp();

            if (mRecords.size() >= 2) {
                // Because there are noises like...
                //
                //              A valid bite
                //  |<-------------------------------->|
                //
                // degree
                // ^
                // |
                // |                   +   +
                // |               +           +
                // |       +
                // |   +      [+]                  +
                // |+                                  +
                // +-------------------------------------> t
                //             ^
                //             +-- This is a noise!
                //
                // Apply the smooth algorithm to fix the noise if there were
                // already two records.
                final long tOffset = record.timestamp - last.timestamp;
                final double projectDegree = last.mouthOpeningDegree +
                                             last.mouthOpeningVelocity * tOffset;
                final double smoothDegree = ALPHA * projectDegree +
                                            (1f - ALPHA) * mouthOpeningDegree;

                record.mouthOpeningDegree = smoothDegree >= 0f ? smoothDegree : 0f;
                record.mouthOpeningVelocity =
                    (record.mouthOpeningDegree - last.mouthOpeningDegree) / tOffset;
            } else {
                // If there are zero or one record, just record it.
                final double dOffset = mouthOpeningDegree - last.mouthOpeningDegree;
                final long tOffset = record.timestamp - last.timestamp;

                record.mouthOpeningDegree = mouthOpeningDegree >= 0f ?
                    mouthOpeningDegree : 0f;
                record.mouthOpeningVelocity = dOffset / tOffset;
            }
        }

        // Add it.
        mRecords.add(record);
        Log.d("mouth", "" + record);

        // Discard the old records.
        while (mRecords.size() > MAX_RECORDS) {
            mRecords.remove(0);
        }
    }

    private boolean detectBiteFromRecords() {
        final int size = mRecords.size();

        if (mRecords.size() > 2) {
            // Step 1: Determine the mouth state: OPENING or CLOSING?
            // Tell if mouth is opening by checking the past 3 frames.
            int openingCount = 0;
            int closingCount = 0;
            for (int i = size - 1; i >= Math.max(0, size - CHECK_PAST_N_RECORDS); --i) {
                final Record record = mRecords.get(i);

                if (record.mouthOpeningVelocity > 0f) {
                    ++openingCount;
                    closingCount = 0;
                } else if (record.mouthOpeningVelocity < 0f) {
                    openingCount = 0;
                    ++closingCount;
                }
            }
//            Log.d("mouth", String.format("openingCount=%d, closingCount=%d",
//                                         openingCount,
//                                         closingCount));
            final int mouthState;
            final Record latest = mRecords.get(size - 1);
            if (latest.mouthOpeningDegree > DEGREE_FOR_MOUTH_OPENED &&
                openingCount == CHECK_PAST_N_RECORDS) {
                mouthState = MOUTH_OPENED;
//                Log.d("mouth", "mouthState = MOUTH_OPENED");
            } else if (latest.mouthOpeningDegree < DEGREE_FOR_MOUTH_CLOSED &&
                       closingCount == CHECK_PAST_N_RECORDS) {
                mouthState = MOUTH_CLOSED;
//                Log.d("mouth", "mouthState = MOUTH_CLOSED");
            } else {
                mouthState = MOUTH_UNDEFINED;
            }

            // Step 2: Determine the bite according to the mouth state.
            // A bite is counted as a closing along with a opening.
            final boolean isABite;
            if (mLastMouthState == MOUTH_OPENED &&
                mouthState == MOUTH_CLOSED) {
                ++mBiteCount;
                Log.d("mouth", String.format("bite count=%d", mBiteCount));

                isABite = true;
            } else {
                isABite = false;
            }

            // Step 3: Record the valid mouth state.
            if (mouthState != MOUTH_UNDEFINED) {
                mLastMouthState = mouthState;
            }

            return isABite;
        } else {
            return false;
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    private static class Record {

        double mouthOpeningDegree;
        double mouthOpeningVelocity;

        long timestamp;

        @Override
        public String toString() {
            return String.format("Record{openingDegree=%.3f, " +
                                 "openingVelocity=%.3f, " +
                                 "timestamp=%d}",
                                 mouthOpeningDegree,
                                 mouthOpeningVelocity,
                                 timestamp);
        }
    }
}
