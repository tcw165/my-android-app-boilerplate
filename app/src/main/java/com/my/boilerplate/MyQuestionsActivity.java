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

package com.my.boilerplate;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.my.boilerplate.data.MyQuestion;
import com.my.boilerplate.data.MyQuestionStore;
import com.my.boilerplate.protocol.IOnClickObjectListener;
import com.my.widget.IProgressBarView;
import com.my.widget.MarginDecoration;
import com.my.widget.util.ViewUtil;

import java.util.Collections;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;

public class MyQuestionsActivity
    extends AppCompatActivity
    implements IProgressBarView,
               IOnClickObjectListener {

    Toolbar mToolbar;
    RecyclerView mQuestionList;
    MyQuestionsAdapter mQuestionListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_my_questions);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // My questions.
        mQuestionListAdapter = new MyQuestionsAdapter(this);
        mQuestionList = (RecyclerView) findViewById(R.id.my_questions);
        mQuestionList.setAdapter(mQuestionListAdapter);
        mQuestionList.addItemDecoration(new MarginDecoration(
            (int) getResources().getDimension(R.dimen.grid_item_spacing_big)));
        mQuestionList.setLayoutManager(new LinearLayoutManager(
            this, LinearLayoutManager.VERTICAL, false));
        mQuestionList.setHasFixedSize(true);

        loadMyQuestions();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void showProgressBar() {
        ViewUtil
            .with(this)
            .setProgressBarCancelable(false)
            .showProgressBar(getString(R.string.loading));
    }

    @Override
    public void hideProgressBar() {
        ViewUtil
            .with(this)
            .hideProgressBar();
    }

    @Override
    public void updateProgress(int progress) {
        showProgressBar();
    }

    @Override
    public void onClickObject(View view,
                              Object data) {
//        final String imagePath = (String) data;
//
        // DO NOTHING.
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    void loadMyQuestions() {
        MyQuestionStore
            .with(getApplicationContext())
            // Get all the questions.
            .getAll()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new DisposableObserver<List<MyQuestion>>() {
                @Override
                public void onNext(List<MyQuestion> value) {
                    mQuestionListAdapter.setData(value);
                }

                @Override
                public void onError(Throwable e) {
                    // DO NOTHING.
                }

                @Override
                public void onComplete() {
                    // DO NOTHING.
                }
            });
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    private static class MyQuestionsAdapter
        extends RecyclerView.Adapter<MyQuestionViewHolder> {

        List<MyQuestion> mData = Collections.emptyList();

        final Context mContext;

        MyQuestionsAdapter(final Context context) {
            mContext = context;
        }

        @Override
        public MyQuestionViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);

            return new MyQuestionViewHolder(
                inflater.inflate(R.layout.view_my_question_item,
                                 parent, false));
        }

        @Override
        public void onBindViewHolder(MyQuestionViewHolder holder,
                                     int position) {
            final MyQuestion question = mData.get(position);

            holder.questionDescView.setText(question.questionDescription);
            holder.questionElapseTimeView.setText("?");
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        public void setData(List<MyQuestion> data) {
            mData = data;
            notifyDataSetChanged();
        }
    }

    private static class MyQuestionViewHolder extends RecyclerView.ViewHolder {

        final TextView questionDescView;
        final TextView questionElapseTimeView;

        MyQuestionViewHolder(View itemView) {
            super(itemView);

            questionDescView = (TextView) itemView.findViewById(R.id.question_description);
            questionElapseTimeView = (TextView) itemView.findViewById(R.id.question_elapse_time);
        }
    }
}
