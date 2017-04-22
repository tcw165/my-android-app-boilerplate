package com.my.demo.dlib;

import android.content.res.AssetManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class StartActivity extends AppCompatActivity {

    // View.
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.img_input)
    ImageView mImgInput;
    @BindView(R.id.img_output)
    ImageView mImgOutput;

    // Butter Knife.
    Unbinder mUnbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_start);

        // Init view binding.
        mUnbinder = ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }

        // Init the input image.
        Glide.with(this)
            .load(Uri.parse("file:///android_asset/boyw165-i-am-tyson-chandler.jpg"))
            .placeholder(R.color.black_70)
            .into(mImgInput);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mUnbinder.unbind();
    }
}
