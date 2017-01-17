package com.my.boilerplate;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.my.widget.ElasticDragDismissFrameLayout;

public class ViewOfElasticDragDismissFrameLayoutSampleActivity extends AppCompatActivity {

//    Toolbar mToolbar;
    ElasticDragDismissFrameLayout mFrame;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_view_of_elastic_drag_dismiss_frame_layout_sample);

//        mToolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(mToolbar);
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        }

        mFrame = (ElasticDragDismissFrameLayout) findViewById(R.id.frame);
        mFrame.addListener(new ElasticDragDismissFrameLayout.SystemChromeFader(this));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        // Disable the default window transition.
        overridePendingTransition(0, 0);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////
}
