//package boyw165.com.my_mosaic_stickers.view;
//
//import android.content.Context;
//import android.graphics.PointF;
//import android.support.v4.view.MotionEventCompat;
//import android.view.MotionEvent;
//import android.view.View;
//
//public class PinchGestureDetector implements View.OnTouchListener {
//
//    private static final String TAG = PinchGestureDetector.class.getSimpleName();
//
//    protected static final int STATE_OTHER = 0x10000001;
//    protected static final int STATE_SCALING = 0x10000002;
//    protected static final int STATE_DRAGGING = 0x10000004;
//    protected int mGestureState = STATE_OTHER;
//
//    protected static final int INVALID_ID = -1;
//    protected int mActivePointerId0 = INVALID_ID;
//    protected int mActivePointerId1 = INVALID_ID;
//
//    protected static final float PRESSURE_THRESHOLD = 0.67f;
//
//    /**
//     * Transform information used by all gesture detectors.
//     */
//    protected TransformInfo mTransformInfo = new TransformInfo();
//    protected static final float INVALID_DIMEN = Float.MAX_VALUE;
//    protected float mMinScale = INVALID_DIMEN;
//    protected float mMaxScale = INVALID_DIMEN;
//
//    public PinchGestureDetector(Context context) {
//        // DO NOTHING.
//    }
//
//    public PinchGestureDetector(Context context,
//                                float minScale,
//                                float maxScale) {
//        mMinScale = minScale;
//        mMaxScale = maxScale;
//    }
//
//    public PinchGestureDetector(Context context, TransformInfo info) {
//        mTransformInfo = info;
//    }
//
//    @Override
//    public boolean onTouch(View view, MotionEvent event) {
//        final int action = MotionEventCompat.getActionMasked(event);
//        TransformInfo info = mTransformInfo;
//
//        // Start fresh.
//        switch (action) {
//            case MotionEvent.ACTION_DOWN:
//            case MotionEvent.ACTION_CANCEL:
//            case MotionEvent.ACTION_UP: {
//                LogUtils.log(TAG, "ACTION_DOWN/ACTION_CANCEL/ACTION_UP: reset()");
//                mTransformInfo.reset();
//                mActivePointerId0 = INVALID_ID;
//                mActivePointerId1 = INVALID_ID;
//                mGestureState = STATE_OTHER;
//                break;
//            }
//        }
//
//        // Gesture detection.
//        if (mGestureState == STATE_OTHER || mGestureState == STATE_DRAGGING) {
//            // If it's in the progress of translation.
//            switch (action) {
//                case MotionEvent.ACTION_DOWN: {
//                    final int pointerIndex = MotionEventCompat.getActionIndex(event);
//                    final float x = MotionEventCompat.getX(event, pointerIndex);
//                    final float y = MotionEventCompat.getY(event, pointerIndex);
//
//                    // Save the ID of this pointer.
//                    mActivePointerId0 = MotionEventCompat.getPointerId(event, 0);
//                    // Remember where we started.
//                    updatePivotInfo(info, event, mActivePointerId0, mActivePointerId1);
//
//                    LogUtils.log(TAG, String.format("ACTION_DOWN"));
//                    break;
//                }
//                case MotionEvent.ACTION_POINTER_DOWN: {
//                    // Always save latest pointer as active pointer #1.
//                    int index1 = MotionEventCompat.getActionIndex(event);
//
//                    mActivePointerId1 = MotionEventCompat.getPointerId(event, index1);
//                    updatePivotInfo(info, event, mActivePointerId0, mActivePointerId1);
//                    // Switch to scaling mode.
//                    mGestureState = STATE_SCALING;
//
//                    LogUtils.log(TAG, String.format("BEGIN_SCALE"));
//                    break;
//                }
//                case MotionEvent.ACTION_CANCEL:
//                case MotionEvent.ACTION_UP: {
//                    LogUtils.log(TAG, String.format("ACTION_CANCEL/ACTION_UP"));
//                    mActivePointerId0 = -1;
//                    break;
//                }
//                case MotionEvent.ACTION_MOVE: {
//                    // Do translation.
//                    if (TransformInfo.isValid(info.prevPivot)) {
//                        int index = MotionEventCompat.getActionIndex(event);
//                        float x = MotionEventCompat.getX(event, index);
//                        float y = MotionEventCompat.getY(event, index);
//
//                        info.currPivot.set(x, y);
//                        LogUtils.log(TAG, String.format("   DRAG_MOVE"));
//                    }
//                    break;
//                }
//            }
//        } else if (mGestureState == STATE_SCALING) {
//            // When scaling...
//            switch (action) {
//                case MotionEvent.ACTION_MOVE: {
//                    int index0 = MotionEventCompat.findPointerIndex(event, mActivePointerId0);
//                    int index1 = MotionEventCompat.findPointerIndex(event, mActivePointerId1);
//
//                    if (index0 == -1 || index1 == -1) {
//                        break;
//                    }
//
//                    float pressure0 = event.getPressure(index0);
//                    float pressure1 = event.getPressure(index1);
//
//                    // Only accept the event if our relative pressure is within
//                    // a certain limit - this can help filter shaky data as a
//                    // finger is lifted.
//                    if (pressure1 / pressure0 < PRESSURE_THRESHOLD) {
//                        break;
//                    }
//
//                    float x0 = MotionEventCompat.getX(event, index0);
//                    float y0 = MotionEventCompat.getY(event, index0);
//                    float x1 = MotionEventCompat.getX(event, index1);
//                    float y1 = MotionEventCompat.getY(event, index1);
//
//                    // Update pivot.
//                    info.currPivot.set((x0 + x1) / 2,
//                                       (y0 + y1) / 2);
//                    // Update span and span vector.
//                    info.currSpanVec.set(x1 - x0, y1 - y0);
//                    info.currSpan = getVectorLength(info.currSpanVec);
//                    // Update scale.
//                    float deltaScale = (info.currSpan - info.prevSpan) / info.prevSpan;
//                    info.deltaScale.set(deltaScale, deltaScale);
//                    // Update rotation according to given span vectors.
//                    info.deltaRotation = getVectorsAngle(info.prevSpanVec,
//                                                         info.currSpanVec);
//
//                    // Clamp scale.
//                    if (mMinScale != INVALID_DIMEN &&
//                        (info.deltaScale.x < 0 || info.deltaScale.y < 0)) {
//                        float limit = mMinScale - view.getScaleX();
//                        if (limit >= info.deltaScale.x) {
//                            info.deltaScale.set(limit, limit);
//                        }
//                    } else if (mMaxScale != INVALID_DIMEN &&
//                               (info.deltaScale.x > 0 || info.deltaScale.y > 0)) {
//                        float limit = mMaxScale - view.getScaleX();
//                        if (limit <= info.deltaScale.x) {
//                            info.deltaScale.set(limit, limit);
//                        }
//                    }
//                    // Post scale clamp.
//                    if (info.deltaScale.x == 0f || info.deltaScale.y == 0f) {
//                        // Disable pivot translation and enable dragging
//                        // translation according to the first action point.
//                        float dx = x0 - info.prevPos.x;
//                        float dy = y0 - info.prevPos.y;
//
//                        info.currPivot.set(info.prevPivot.x + dx,
//                                           info.prevPivot.y + dy);
//                    }
//
//                    LogUtils.log(TAG, String.format("   SCALE_MOVE: pt0=(%f, %f), pt1=(%f, %f)", x0, y0, x1, y1));
//                    LogUtils.log(TAG, String.format("               prevPivot=%s, prevSpan=%f", info.prevPivot, info.prevSpan));
//                    LogUtils.log(TAG, String.format("               currPivot=%s, currSpan=%f, currScale=%f", info.currPivot, info.currSpan, view.getScaleX()));
//                    LogUtils.log(TAG, String.format("               deltaScale=%s", info.deltaScale));
//                    LogUtils.log(TAG, String.format("               deltaRotation=%f", info.deltaRotation));
//                    break;
//                }
//                case MotionEvent.ACTION_POINTER_UP: {
//                    int index0 = MotionEventCompat.findPointerIndex(event, mActivePointerId0);
//                    int index1 = MotionEventCompat.findPointerIndex(event, mActivePointerId1);
//                    int index = MotionEventCompat.getActionIndex(event);
//                    int id = MotionEventCompat.getPointerId(event, index);
//
//                    LogUtils.log(TAG, "ACTION_POINTER_UP");
//
//                    if (id == mActivePointerId0) {
//                        // Find new index except either current one or active index #1.
//                        int newIndex0 = findNewPointIndex(event, index, index1);
//
//                        if (newIndex0 >= 0) {
//                            // Update new id.
//                            mActivePointerId0 = MotionEventCompat.getPointerId(event, newIndex0);
//                            // Update info because active pointer #0 is considered
//                            // as previous position.
//                            updatePivotInfo(info, event, mActivePointerId0, mActivePointerId1);
//                        } else {
//                            if (mActivePointerId1 == INVALID_ID) {
//                                mActivePointerId0 = INVALID_ID;
//                                mGestureState = STATE_OTHER;
//
//                                info.reset();
//                            } else {
//                                // Replace active pointer #1.
//                                mActivePointerId0 = mActivePointerId1;
//                                mActivePointerId1 = INVALID_ID;
//                                mGestureState = STATE_DRAGGING;
//
//                                updatePivotInfo(info, event, mActivePointerId0, INVALID_ID);
//                            }
//                        }
//                    } else if (id == mActivePointerId1) {
//                        // Find new index except either current one or active index #0.
//                        int newIndex1 = findNewPointIndex(event, index, index0);
//
//                        if (newIndex1 >= 0) {
//                            // Update new id.
//                            mActivePointerId1 = MotionEventCompat.getPointerId(event, newIndex1);
//
//                            updatePivotInfo(info, event, mActivePointerId0, mActivePointerId1);
//                        } else {
//                            mActivePointerId1 = INVALID_ID;
//                            mGestureState = STATE_DRAGGING;
//
//                            updatePivotInfo(info, event, mActivePointerId0, INVALID_ID);
//                        }
//                    }
//                    break;
//                }
//            }
//        }
//
//        // Apply transform.
//        setTransform(view, mTransformInfo);
//
//        return true;
//    }
//
//    public void setScaleLimit(float min, float max) {
//        mMinScale = min;
//        mMaxScale = max;
//    }
//
//    ///////////////////////////////////////////////////////////////////////////
//    // Statics ////////////////////////////////////////////////////////////////
//
//    ///////////////////////////////////////////////////////////////////////////
//    // Private ////////////////////////////////////////////////////////////////
//
//    private int findNewPointIndex(MotionEvent event, int excludeIndex0, int excludeIndex1) {
//        int count = MotionEventCompat.getPointerCount(event);
//
//        for (int i = 0; i < count; ++i) {
//            if (i != excludeIndex0 && i != excludeIndex1) {
//                return i;
//            }
//        }
//
//        return -1;
//    }
//
//    private void updatePivotInfo(TransformInfo info, MotionEvent event, int activePointer0, int activePointer1) {
//        int index0 = MotionEventCompat.findPointerIndex(event, activePointer0);
//        int index1 = MotionEventCompat.findPointerIndex(event, activePointer1);
//        float x0 = MotionEventCompat.getX(event, index0);
//        float y0 = MotionEventCompat.getY(event, index0);
//
//        // Reset info.
//        info.reset();
//        // Remember previous position (this might be a redundant operation but is
//        // still necessary for consistent initialization).
//        info.prevPos.set(x0, y0);
//        info.prevPivot.set(x0, y0);
//
//        if (index1 != -1) {
//            float x1 = MotionEventCompat.getX(event, index1);
//            float y1 = MotionEventCompat.getY(event, index1);
//
//            // Update span vector.
//            info.prevSpanVec.set(x1 - x0, y1 - y0);
//            info.prevSpan = getVectorLength(info.prevSpanVec);
//            info.currSpanVec.set(info.prevSpanVec);
//            info.currSpan = getVectorLength(info.currSpanVec);
//            // Update pivot.
//            info.prevPivot.set((x0 + x1) / 2,
//                               (y0 + y1) / 2);
//            info.currPivot.set(info.prevPivot);
//        }
//    }
//
//    private void setTransform(View view, TransformInfo info) {
//        boolean isChanged = false;
//
//        if (TransformInfo.isNonZero(info.deltaScale)) {
//            float scaleX = view.getScaleX() + info.deltaScale.x;
//            float scaleY = view.getScaleY() + info.deltaScale.y;
//
//            view.setScaleX(scaleX);
//            view.setScaleY(scaleY);
//            isChanged = true;
//        }
//
//        // For translation, pivot change is always prior to position change.
//        if (TransformInfo.isValid(info.prevPivot) && TransformInfo.isValid(info.currPivot) &&
//            !info.prevPivot.equals(info.currPivot)) {
//            float[] deltaVec = {info.currPivot.x - info.prevPivot.x,
//                                info.currPivot.y - info.prevPivot.y};
//
//            // Update pivot translation.
//            view.getMatrix().mapVectors(deltaVec);
//            view.setTranslationX(view.getTranslationX() + deltaVec[0]);
//            view.setTranslationY(view.getTranslationY() + deltaVec[1]);
//            isChanged = true;
//        }
//
//        if (TransformInfo.isNonZero(info.deltaRotation)) {
//            float rotation = view.getRotation() + info.deltaRotation;
//
//            view.setRotation(rotation);
//            isChanged = true;
//        }
//
//        if (isChanged) {
//            view.invalidate();
//        }
//    }
//
//    private float getVectorsAngle(PointF vecA, PointF vecB) {
//        if (vecA.x == vecB.x && vecA.y == vecB.y) {
//            return 0;
//        } else {
//            return (float) (Math.toDegrees(
//                (Math.atan2(vecB.y, vecB.x) - Math.atan2(vecA.y, vecA.x))));
//        }
//    }
//
//    private float getVectorLength(PointF vec) {
//        // Don't use Math.hypot() is because it's much slower than Math.sqrt().
//        // Ref: http://blog.element84.com/improving-java-math-perf-with-jafama.html
//        return (float) Math.sqrt(vec.x * vec.x + vec.y * vec.y);
//    }
//
//    ///////////////////////////////////////////////////////////////////////////
//    // Clazz //////////////////////////////////////////////////////////////////
//
//    /**
//     * Used to apply transform to view.
//     */
//    public static class TransformInfo {
//
//        public static final float INVALID = Float.MAX_VALUE;
//
//        public PointF deltaScale = new PointF(0f, 0f);
//
//        /**
//         * Previous position of first active pointer.
//         */
//        public PointF prevPos = new PointF(INVALID, INVALID);
//        /**
//         * Previous pivot. When dragging, this would be same wth previous
//         * position; When scaling, this will not in general.
//         */
//        public PointF prevPivot = new PointF(INVALID, INVALID);
//        public PointF currPivot = new PointF(INVALID, INVALID);
//
//        // Span vector which is used to calculate the rotation angle.
//        public PointF prevSpanVec = new PointF(INVALID, INVALID);
//        public PointF currSpanVec = new PointF(INVALID, INVALID);
//        public float prevSpan = 0;
//        public float currSpan = 0;
//
//        public float deltaRotation;
//
//        public void reset() {
//            prevPos.set(INVALID, INVALID);
//
//            deltaScale.set(0f, 0f);
//
//            prevPivot.set(INVALID, INVALID);
//            currPivot.set(INVALID, INVALID);
//
//            prevSpanVec.set(INVALID, INVALID);
//            currSpanVec.set(INVALID, INVALID);
//
//            deltaRotation = 0;
//        }
//
//        public static boolean isValid(PointF pt) {
//            return (pt != null && pt.x != INVALID && pt.y != INVALID);
//        }
//
//        public static boolean isNonZero(PointF pt) {
//            return (pt != null && pt.x != 0f && pt.y != 0f);
//        }
//
//        public static boolean isNonZero(float num) {
//            return num != 0;
//        }
//    }
//}
