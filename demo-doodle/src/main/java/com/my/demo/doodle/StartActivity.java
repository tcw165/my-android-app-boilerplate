package com.my.demo.doodle;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.jakewharton.rxbinding2.view.RxView;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.functions.Consumer;

public class StartActivity extends AppCompatActivity {

    // View
    @BindView(R.id.btn_close)
    View mBtnClose;
    @BindView(R.id.btn_doodle)
    View mBtnDoodle;

    // Binder.
    Unbinder mUnbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_start);

        // Bind view.
        mUnbinder = ButterKnife.bind(this);

        // Listener.
        RxView.clicks(mBtnDoodle)
              .debounce(550, TimeUnit.MILLISECONDS)
              .subscribe(new Consumer<Object>() {
                  @Override
                  public void accept(Object o) throws Exception {
                      startActivity(new Intent(StartActivity.this,
                                               DoodleEditorActivity.class));
                  }
              });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unbind view.
        mUnbinder.unbind();
    }
}
