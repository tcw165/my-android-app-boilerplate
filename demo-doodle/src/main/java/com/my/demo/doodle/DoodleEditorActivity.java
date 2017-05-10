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

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.my.demo.doodle.view.DoodleEditorView;
import com.my.widget.ElasticDragDismissLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class DoodleEditorActivity extends AppCompatActivity {

    // View.
    @BindView(R.id.btn_close)
    View mBtnClose;
    @BindView(R.id.btn_undo)
    View mBtnUndo;
    @BindView(R.id.btn_redo)
    View mBtnRedo;
    @BindView(R.id.btn_check)
    View mBtnCheck;
    @BindView(R.id.doodle_editor)
    DoodleEditorView mDoodleEdtior;

    // Unbinder.
    Unbinder mUnbinder;

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set content view.
        setContentView(R.layout.activity_doodle_editor);

        // Bind view.
        mUnbinder = ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mUnbinder.unbind();
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
}
