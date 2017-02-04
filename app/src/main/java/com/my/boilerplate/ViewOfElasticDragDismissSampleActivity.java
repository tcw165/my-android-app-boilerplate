package com.my.boilerplate;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.my.widget.ElasticDragDismissLayout;

public class ViewOfElasticDragDismissSampleActivity extends AppCompatActivity {

    ElasticDragDismissLayout mLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_of_elastic_drag_dismiss_sample);
        // Disable the default window transition and let mLayout to handle it.
        overridePendingTransition(0, 0);

        mLayout = (ElasticDragDismissLayout) findViewById(R.id.layout);
        mLayout.addOnDragDismissListener(new ElasticDragDismissLayout.SystemChromeFader(this) {
            @Override
            public void onDrag(float elasticOffset,
                               float elasticOffsetPixels,
                               float rawOffset,
                               float rawOffsetPixels) {
                super.onDrag(elasticOffset,
                             elasticOffsetPixels,
                             rawOffset,
                             rawOffsetPixels);
            }

            @Override
            public void onDismissByDragOver(float totalScroll) {
                Log.d("xyz", "onDismissByDragOver");
                finishWithResult();
            }

            @Override
            public void onDismissByBackPressed() {
                Log.d("xyz", "onDismissByBackPressed");
                finishWithResult();
            }

            @Override
            public void onDismissByBgPressed() {
                Log.d("xyz", "onDismissByBgPressed");
                finishWithResult();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Actively remove the listeners to prevent coupled reference.
        mLayout.removeAllOnDragDismissListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Open the layout with animation.
        mLayout.postOpen();
    }

    @Override
    public void onBackPressed() {
        // Close the layout with animation.
        mLayout.close();
    }

    public void finishWithResult() {
        // We cannot get the SupportFragmentManager to pop its stack when the
        // activity is paused by calling super.onBackPressed().
        ActivityCompat.finishAfterTransition(this);

        // Disable the window transition and let the launching activity handle
        // it.
        overridePendingTransition(0, 0);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////
}
