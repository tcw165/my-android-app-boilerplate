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

package com.my.demo.doodle;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.jakewharton.rxbinding2.widget.RxSeekBar;
import com.my.demo.doodle.data.ColorPenBrushFactory;
import com.my.demo.doodle.protocol.ISketchBrush;
import com.my.demo.doodle.view.SketchEditorView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

public class DoodleEditorActivity
    extends AppCompatActivity {

    // Unbinder.
    Unbinder mUnbinder;

    // Rx.
    CompositeDisposable mDisposables;

    // View.
    @BindView(R.id.btn_close)
    View mBtnClose;
    @BindView(R.id.btn_undo)
    View mBtnUndo;
    @BindView(R.id.btn_redo)
    View mBtnRedo;
    @BindView(R.id.btn_check)
    View mBtnCheck;
    @BindView(R.id.stroke_width_picker)
    AppCompatSeekBar mStrokeWidthPicker;
    @BindView(R.id.brush_picker)
    RecyclerView mBrushPicker;
    @BindView(R.id.sketch_editor)
    SketchEditorView mSketchEditor;

    // Adapter.
    RequestManager mGlide;
    BrushAdapter mBrushPickerAdapter;

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set content view.
        setContentView(R.layout.activity_doodle_editor);

        // Bind view.
        mUnbinder = ButterKnife.bind(this);

        // Glide request manager.
        mGlide = Glide.with(this);

        // Init disposable pool.
        mDisposables = new CompositeDisposable();

        // Brush stroke picker.
        mDisposables.add(
            RxSeekBar
                .changes(mStrokeWidthPicker)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer value) throws Exception {
                        if (mSketchEditor.getBrush() == null) return;
                        Log.d("xyz", "seek-bar value=" + value);

                        final float min = mSketchEditor.getMinStrokeWidth();
                        final float max = mSketchEditor.getMaxStrokeWidth();
                        final ISketchBrush brush = mSketchEditor.getBrush();

                        brush.setStrokeWidth(min + (max - min) * value / 100f);
                    }
                }));

        // Brush picker.
        mBrushPickerAdapter = new BrushAdapter(getLayoutInflater(),
                                               mGlide,
                                               onClickBrush());
        mBrushPickerAdapter.setItems(
            new ColorPenBrushFactory()
                .setStrokeWidth(getResources().getDimension(
                    R.dimen.sketch_min_stroke_width))
                .addColor(Color.parseColor("#FFFFFF"))
                .addColor(Color.parseColor("#000000"))
                .addColor(Color.parseColor("#3897F0"))
                .addColor(Color.parseColor("#70C050"))
                .addColor(Color.parseColor("#FDCB5C"))
                .addColor(Color.parseColor("#FD8D32"))
                .addColor(Color.parseColor("#ED4956"))
                .addColor(Color.parseColor("#D13076"))
                .addColor(Color.parseColor("#A307BA"))
                .addColor(Color.parseColor("#FFB7C5"))
                .addColor(Color.parseColor("#D1AF94"))
                .addColor(Color.parseColor("#97D5E0"))
                .addColor(Color.parseColor("#4FC3C6"))
                .addColor(Color.parseColor("#0C4C8A"))
                .addColor(Color.parseColor("#5C7148"))
                .addColor(Color.parseColor("#262626"))
                .addColor(Color.parseColor("#595959"))
                .addColor(Color.parseColor("#7F7F7F"))
                .addColor(Color.parseColor("#999999"))
                .addColor(Color.parseColor("#B3B3B3"))
                .addColor(Color.parseColor("#CCCCCC"))
                .addColor(Color.parseColor("#E6E6E6"))
                .build());
        mBrushPicker.setLayoutManager(new LinearLayoutManager(
            this, LinearLayoutManager.HORIZONTAL, false));
        mBrushPicker.setAdapter(mBrushPickerAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unbind view.
        mUnbinder.unbind();

        // Finish the remaining Glide request.
        mGlide.onDestroy();

        // Dispose all disposables.
        mDisposables.clear();
    }

    @OnClick(R.id.btn_close)
    void onClickToClose(View view) {
        onBackPressed();
    }

    @OnClick(R.id.btn_undo)
    void onClickToUndo(View view) {
        Log.d("xyz", "Yet implement.");
    }

    @OnClick(R.id.btn_redo)
    void onClickToRedo(View view) {
        Log.d("xyz", "Yet implement.");
    }

    @OnClick(R.id.btn_check)
    void onClickToCheck(View view) {
        Log.d("xyz", "Yet implement.");
    }

    BrushListener onClickBrush() {
        return new BrushListener() {
            @Override
            public void onClickBrush(ISketchBrush brush) {
                // TODO: Update the width.
//                brush.getStroke().setWidth()
                mSketchEditor.setBrush(brush);
            }
        };
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    private interface BrushListener {
        void onClickBrush(ISketchBrush brush);
    }

    private static class BrushAdapter
        extends RecyclerView.Adapter<BrushViewHolder> {

        private final LayoutInflater mInflater;
        private final RequestManager mGlide;
        private final BrushListener mCallback;

        private final List<ISketchBrush> mBrushes = new ArrayList<>();
        private final List<ColorDrawable> mColorDrawables = new ArrayList<>();

        private BrushAdapter(final LayoutInflater inflater,
                             final RequestManager glide,
                             final BrushListener callback) {
            mInflater = inflater;
            mGlide = glide;
            mCallback = callback;
        }

        @Override
        public BrushViewHolder onCreateViewHolder(ViewGroup parent,
                                                  int viewType) {
            return new BrushViewHolder(mInflater.inflate(
                R.layout.view_doodle_brush_item, parent, false));
        }

        @Override
        public void onBindViewHolder(BrushViewHolder holder,
                                     int position) {
            final ImageView view = (ImageView) holder.itemView;
            final ISketchBrush brush = mBrushes.get(position);
            final ColorDrawable drawable = mColorDrawables.get(position);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCallback == null) return;

                    mCallback.onClickBrush(brush);
                }
            });
            Log.d("xyz", "onBind:: #" + position + ", " +
                         "color=" + brush.getStrokeColor());

            view.setImageDrawable(drawable);
//            // Display.
//            mGlide.load(drawable)
//                  .placeholder(R.color.white)
//                  .into(view);
        }

        @Override
        public int getItemCount() {
            return mBrushes.size();
        }

        public void setItems(List<ISketchBrush> brushes) {
            mBrushes.addAll(brushes);

            // Construct color drawable list.
            mColorDrawables.clear();
            for (ISketchBrush brush : mBrushes) {
                mColorDrawables.add(new ColorDrawable(
                    brush.getStrokeColor()));
            }
        }
    }

    private static class BrushViewHolder
        extends RecyclerView.ViewHolder {

        private BrushViewHolder(View itemView) {
            super(itemView);
        }
    }
}
