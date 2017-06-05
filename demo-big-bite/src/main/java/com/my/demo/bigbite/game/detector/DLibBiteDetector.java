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

    private static final double DEGREE_150 = 150;
    private static final double DEGREE_180 = 180;
    private static final double DEGREE_360 = 360;

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

    // Records.
    private static final double ALPHA = 0.3f;
    private static final double DEGREE_FOR_MOUTH_OPENED = 18;
    private static final double DEGREE_FOR_MOUTH_CLOSED = 18;
    private static final int MAX_RECORDS = 10;
    private static final int CHECK_LATEST_N_RECORDS = 4;
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

//            Log.d("mouth", "----------");

            // Update the vectors.
            mLeftUpVec[0] = marks.get(L_UP[1]).x - marks.get(L_UP[0]).x;
            mLeftUpVec[1] = marks.get(L_UP[1]).y - marks.get(L_UP[0]).y;
            mLeftLowVec[0] = marks.get(L_LOW[1]).x - marks.get(L_LOW[0]).x;
            mLeftLowVec[1] = marks.get(L_LOW[1]).y - marks.get(L_LOW[0]).y;
            mRightUpVec[0] = marks.get(R_UP[1]).x - marks.get(R_UP[0]).x;
            mRightUpVec[1] = marks.get(R_UP[1]).y - marks.get(R_UP[0]).y;
            mRightLowVec[0] = marks.get(R_LOW[1]).x - marks.get(R_LOW[0]).x;
            mRightLowVec[1] = marks.get(R_LOW[1]).y - marks.get(R_LOW[0]).y;
//            Log.d("mouth", String.format("mLeftUpVec=(x=%.5f, y=%.5f), mLeftLowVec=(x=%.5f, y=%.5f), " +
//                                          "mRightUpVec=(x=%.5f, y=%.5f), mRightLowVec=(x=%.5f, y=%.5f)",
//                                          mLeftUpVec[0], mLeftUpVec[1],
//                                          mLeftLowVec[0], mLeftLowVec[1],
//                                          mRightUpVec[0], mRightUpVec[1],
//                                          mRightLowVec[0], mRightLowVec[1]));

            // Calculate the angles of both left and right side.
            double leftUpDegree = Math.toDegrees(Math.atan2((double) mLeftUpVec[1], (double) mLeftUpVec[0]));
            double leftLowDegree = Math.toDegrees(Math.atan2((double) mLeftLowVec[1], (double) mLeftLowVec[0]));
            double rightUpDegree = Math.toDegrees(Math.atan2((double) mRightUpVec[1], (double) mRightUpVec[0]));
            double rightLowDegree = Math.toDegrees(Math.atan2((double) mRightLowVec[1], (double) mRightLowVec[0]));
//            Log.d("mouth", String.format("leftUpDegree=%.5f, leftLowDegree=%.5f, " +
//                                          "rightUpDegree=%.5f, rightLowDegree=%.5f",
//                                          leftUpDegree, leftLowDegree,
//                                          rightUpDegree, rightLowDegree));

            // Calculate the left and right angle of the mouth.
            double leftDegree = Math.abs(leftUpDegree - leftLowDegree);
            // It's possible to get a large angle, but what I want is a small angle.
            if (leftDegree > DEGREE_180) {
                leftDegree = DEGREE_360 - leftDegree;
            }
            double rightDegree = Math.abs(rightUpDegree - rightLowDegree);
            if (rightDegree > DEGREE_180) {
                rightDegree = DEGREE_360 - rightDegree;
            }
//            Log.d("mouth", String.format("leftDegree=%.5f, rightDegree=%.5f",
//                                         leftDegree, rightDegree));

            // Calculate the min/max angle of the mouth.
            final double minDegrees = Math.min(leftDegree, rightDegree);
            final double maxDegrees = Math.max(leftDegree, rightDegree);
            double openingDegrees = Math.max(minDegrees, maxDegrees);

            if (openingDegrees > DEGREE_150) {
                openingDegrees = DEGREE_150;
            } else if (openingDegrees < 0f) {
                openingDegrees = 0f;
            }

//            Log.d("mouth", String.format("mouth opening degrees, min=%.3ff, max=%.3f (ts=%d)",
//                                          minDegrees, maxDegrees,
//                                          getTimestamp()));

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

        record.timestamp = getTimestamp();

        // Determine the degree velocity.
        if (mRecords.isEmpty()) {
            record.mouthOpeningVelocity = 0;
            record.mouthOpeningDegree = mouthOpeningDegree;
        } else {
            final Record last = mRecords.get(size - 1);

            // For unknown reason, there would be very close two records
            // recorded.
            if (last.timestamp == record.timestamp) return;

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
//                final double projectDegree = last.mouthOpeningDegree +
//                                             last.mouthOpeningVelocity * tOffset;
//                final double smoothDegree = ALPHA * projectDegree +
//                                            (1f - ALPHA) * mouthOpeningDegree;
                final double smoothDegree = ALPHA * last.mouthOpeningDegree +
                                            (1f - ALPHA) * mouthOpeningDegree;

                record.mouthOpeningDegree = smoothDegree >= 0f ? smoothDegree : 0f;
                record.mouthOpeningVelocity =
                    (record.mouthOpeningDegree - last.mouthOpeningDegree) / tOffset;
            } else {
                // If there are zero or one record, just record it.
                final double dOffset = mouthOpeningDegree - last.mouthOpeningDegree;
                final long tOffset = record.timestamp - last.timestamp;

                record.mouthOpeningDegree = mouthOpeningDegree;
                record.mouthOpeningVelocity = dOffset / tOffset;
            }

            // Determine the mouth state.
            if (record.mouthOpeningDegree >= DEGREE_FOR_MOUTH_OPENED &&
                last.mouthOpeningVelocity > 0f) {
                record.mouthState = Record.MOUTH_OPENED;
            } else if (record.mouthOpeningDegree <= DEGREE_FOR_MOUTH_CLOSED &&
                last.mouthOpeningVelocity < 0f) {
                record.mouthState = Record.MOUTH_CLOSED;
            } else {
                // For the uncertain state, just follow the previous state.
                record.mouthState = last.mouthState;
            }
        }

        // Add it.
        mRecords.add(record);
//        Log.d("mouth", String.format("Given %.3f, Save %s", mouthOpeningDegree, record));
        Log.d("mouth", String.format("mouth opening state = %s",
                                     (record.mouthState == Record.MOUTH_OPENED ?
                                     "OPENED" : "CLOSED")));

        // Discard the old records.
        while (mRecords.size() > MAX_RECORDS) {
            mRecords.remove(0);
        }
    }

    private boolean detectBiteFromRecords() {
        final int size = mRecords.size();
        boolean isABite = false;

        if (mRecords.size() >= CHECK_LATEST_N_RECORDS) {
            //    mouth state.
            //    ^
            // +1 |   +  +     +     +  +
            //    +-------------------------------------> t
            // -1 |+        +     +        +  +  +
            //                      |<-------->|
            //                       This counts a bite.

            final Record latest = mRecords.get(size - 1);
            if (latest.mouthState != Record.MOUTH_CLOSED) return false;

            // According to the above figure, I want to find a pattern of an
            // order sensitive sequence of [OPENED, OPENED, CLOSED, CLOSED].
            // To make it order sensitive, I multiply the state value and the
            // position index (start from 1) and then add them up.
            int accumulatedStateCount = 0;
            for (int i = 1; i <= CHECK_LATEST_N_RECORDS; ++i) {
                final int index = size - i;
                accumulatedStateCount += (i * mRecords.get(index).mouthState);
            }

            // If the sequence matches [OPENED, OPENED, CLOSED, CLOSED].
            //                  value:    +1      +1      -1      -1
            //         position index:     4       3       2       1
            //            final value:    +4      +3      -2      -1
            // --------------------------------------------------------
            //                    sum:    +4
            if (accumulatedStateCount == 4) {
                isABite = true;
                ++mBiteCount;
            }
        }

        return isABite;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    /**
     * The mouth state record.
     */
    private static class Record {

        static final int MOUTH_CLOSED = -1;
        static final int MOUTH_OPENED = 1;
        int mouthState = MOUTH_CLOSED;

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
