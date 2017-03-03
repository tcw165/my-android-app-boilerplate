package com.my.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.ImageView;

import com.my.widget.data.ObservableCheckableState;

/**
 * A checkable image-card view with the ability to constraint the aspect-ratio.
 * <br/>
 * Attributes:
 * <br/>
 * {@link R.styleable#CheckableImageCardView_checkable}
 * <br/>
 * {@link R.styleable#CheckableImageCardView_isChecked}
 * <br/>
 * {@link R.styleable#CheckableImageCardView_aspectRatio}
 * <br/>
 * {@link R.styleable#CheckableImageCardView_fixedDimension}
 */
public class CheckableImageCardView
    extends CardView
    implements Checkable,
               ObservableCheckableState.OnStateChangeListener {

    /**
     * Enable the checkable mode and change the UI.
     * <br/>
     * By default is false.
     */
    private boolean mIsCheckable;
    /**
     * Check the view like checkbox.
     * <br/>
     * By default is false.
     */
    private boolean mIsChecked;
    /**
     * The value of width over height. Zero indicates uncertain aspect-ratio.
     * The {@link #mFixedDimension} is valid when the aspect-ratio is not
     * zero.
     * <br/>
     * By default is 0.
     */
    private float mAspectRatio;
    /**
     * 0 indicates the fixed dimension is width;
     * <br/>
     * 1 indicates the fixed dimension is height;
     * <br/>
     * By default is 0.
     */
    private int mFixedDimension;

    // Sub-views.
    private ImageView mImageView;
    private View mBorderView;
    private View mCheckboxView;

    public CheckableImageCardView(Context context) {
        this(context, null);
    }

    public CheckableImageCardView(Context context,
                                  AttributeSet attrs) {
        super(context, attrs);

        // Determine the attributes.
        final TypedArray array = context.obtainStyledAttributes(
            attrs, R.styleable.CheckableImageCardView, 0, 0);
        mIsChecked = array.getBoolean(R.styleable.CheckableImageCardView_isChecked, false);
        mIsCheckable = array.getBoolean(R.styleable.CheckableImageCardView_checkable, false);
        mAspectRatio = array.getFloat(R.styleable.CheckableImageCardView_aspectRatio, 0.f);
        mFixedDimension = array.getInt(R.styleable.CheckableImageCardView_fixedDimension, 0);
        array.recycle();

        // For the IDE preview, show the checkable state.
        if (isInEditMode()) {
            mIsCheckable = true;
        }

        // Inflate custom view.
        View layout = inflate(context, R.layout.view_checkable_image_cardview, this);
        mImageView = (ImageView) layout.findViewById(R.id.image);
        mBorderView = layout.findViewById(R.id.border);
        mBorderView.setVisibility(mIsCheckable ? View.VISIBLE : View.INVISIBLE);
        mCheckboxView = layout.findViewById(R.id.checkbox);
        mCheckboxView.setVisibility(mIsChecked ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void setChecked(boolean checked) {
        if (!mIsCheckable) return;

        mIsChecked = checked;

        // Update the border and checkbox.
        mBorderView.setVisibility(checked ? View.VISIBLE : View.INVISIBLE);
        mCheckboxView.setVisibility(checked ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public boolean isChecked() {
        return mIsChecked;
    }

    @Override
    public void toggle() {
        setChecked(!isChecked());
    }

    @Override
    public void onUpdateCheckableState(boolean checkable) {
        setCheckable(checkable);
    }

    public float getAspectRatio() {
        return mAspectRatio;
    }

    public void setAspectRatio(float ratio) {
        mAspectRatio = ratio;
        // Request layout again.
        requestLayout();
    }

    @SuppressWarnings("unused")
    public void setCheckable(boolean checkable) {
        final boolean isChanged = mIsCheckable != checkable;

        mIsCheckable = checkable;

        // With reference to the design, no UI change when it is checkable.
        // Update the border and checkbox.
        if (!checkable) {
            mBorderView.setVisibility(View.INVISIBLE);
            mCheckboxView.setVisibility(View.INVISIBLE);
        }
    }

    @SuppressWarnings("unused")
    public boolean isCheckable() {
        return mIsCheckable;
    }

    @SuppressWarnings("unused")
    public ImageView getImageView() {
        return mImageView;
    }

    @SuppressWarnings("unused")
    public void setImageBitmap(Bitmap bmp) {
        mImageView.setImageBitmap(bmp);
    }

    @SuppressWarnings("unused")
    public void setImageUrl(Uri uri) {
        mImageView.setImageURI(uri);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    @Override
    protected void onMeasure(int widthSpec,
                             int heightSpec) {
        if (mAspectRatio == 0 || mFixedDimension >= 2) {
            super.onMeasure(widthSpec, heightSpec);
        } else {
            // We ignored padding or many other cases.
            int width = MeasureSpec.getSize(widthSpec);
            int height = MeasureSpec.getSize(heightSpec);
            // Determine the fixed dimension and calculate the other.
            // See {@link R.styleable#CheckableCardView_fixedDimension}
            if (mFixedDimension == 0) {
                // Fixed width.
                height = (int) (width / mAspectRatio);
            } else if (mFixedDimension == 1) {
                // Fixed height.
                width = (int) (height * mAspectRatio);
            }

            // Because it is a ViewGroup, we need to measure child views.
            // Setting MeasureSpec.EXACTLY is necessary to fix the size.
            super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        }
    }
}
