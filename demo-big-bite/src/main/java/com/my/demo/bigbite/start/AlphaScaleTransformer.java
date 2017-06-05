package com.my.demo.bigbite.start;

import android.support.annotation.FloatRange;
import android.view.View;

import com.yarolegovich.discretescrollview.transform.Pivot;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

public class AlphaScaleTransformer extends ScaleTransformer {

    private Pivot mPivotX;
    private Pivot mPivotY;
    private float mMinScale;
    private float mMaxMinScaleDiff;
    private float mMinAlpha;
    private float mMinMaxAlphaDiff;

    public AlphaScaleTransformer() {
        mPivotX = Pivot.X.CENTER.create();
        mPivotY = Pivot.Y.CENTER.create();
        mMinScale = 0.8f;
        mMaxMinScaleDiff = 0.2f;
        mMinAlpha = 0.5f;
        mMinMaxAlphaDiff = 0.5f;
    }

    @Override
    public void transformItem(View item, float position) {
        super.transformItem(item, position);

        final float closenessToCenter = 1f - Math.abs(position);
        final float scale = mMinScale + mMaxMinScaleDiff * closenessToCenter;
        mPivotX.setOn(item);
        mPivotY.setOn(item);
        item.setScaleX(scale);
        item.setScaleY(scale);

        final float alpha = mMinAlpha + mMinMaxAlphaDiff * closenessToCenter;
        item.setAlpha(alpha);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    public static class Builder {

        private AlphaScaleTransformer transformer;
        private float maxScale;
        private float maxAlpha;

        public Builder() {
            super();
            transformer = new AlphaScaleTransformer();
            maxScale = 1f;
            maxAlpha = 1f;
        }

        public AlphaScaleTransformer.Builder setMinAlpha(@FloatRange(from = 0.01) float alpha) {
            transformer.mMinAlpha = alpha;
            return this;
        }

        public AlphaScaleTransformer.Builder setMaxAlpha(@FloatRange(from = 0.01) float alpha) {
            if (alpha > 1f || alpha < 0f) return this;
            maxAlpha = alpha;
            return this;
        }

        public AlphaScaleTransformer.Builder setMinScale(@FloatRange(from = 0.01) float scale) {
            transformer.mMinScale = scale;
            return this;
        }

        public AlphaScaleTransformer.Builder setMaxScale(@FloatRange(from = 0.01) float scale) {
            maxScale = scale;
            return this;
        }

        public AlphaScaleTransformer.Builder setPivotX(Pivot.X pivotX) {
            return setPivotX(pivotX.create());
        }

        public AlphaScaleTransformer.Builder setPivotX(Pivot pivot) {
            assertAxis(pivot, Pivot.AXIS_X);
            transformer.mPivotX = pivot;
            return this;
        }

        public AlphaScaleTransformer.Builder setPivotY(Pivot.Y pivotY) {
            return setPivotY(pivotY.create());
        }

        public AlphaScaleTransformer.Builder setPivotY(Pivot pivot) {
            assertAxis(pivot, Pivot.AXIS_Y);
            transformer.mPivotY = pivot;
            return this;
        }

        public AlphaScaleTransformer build() {
            transformer.mMaxMinScaleDiff = maxScale - transformer.mMinScale;
            transformer.mMinMaxAlphaDiff = maxAlpha - transformer.mMinAlpha;
            return transformer;
        }

        private void assertAxis(Pivot pivot, @Pivot.Axis int axis) {
            if (pivot.getAxis() != axis) {
                throw new IllegalArgumentException("You passed a Pivot for wrong axis.");
            }
        }
    }
}
