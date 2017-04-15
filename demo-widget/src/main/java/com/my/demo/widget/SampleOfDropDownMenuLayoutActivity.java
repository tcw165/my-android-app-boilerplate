package com.my.demo.widget;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.my.core.protocol.IDrawerViewLayout;
import com.my.widget.DropDownMenuLayout;

public class SampleOfDropDownMenuLayoutActivity
    extends AppCompatActivity {

    Toolbar mToolbar;
    DropDownMenuLayout mMenuLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);

        setContentView(R.layout.activity_sample_of_drop_down_menu_layout);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }

        mMenuLayout = (DropDownMenuLayout) findViewById(R.id.menu_layout);
        mMenuLayout.addOnDrawerStateChangeListener(onMenuStateChange());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.menu_drawer_sample, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_toggle_drawer:
                toggleDrawerMenu();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    private void toggleDrawerMenu() {
        if (mMenuLayout == null) return;

        if (mMenuLayout.isDrawerOpened()) {
            mMenuLayout.closeDrawer();
        } else {
            mMenuLayout.openDrawer();
        }
    }

    private IDrawerViewLayout.OnDrawerStateChange onMenuStateChange() {
        return new IDrawerViewLayout.OnDrawerStateChange() {
            @Override
            public void onOpenDrawer() {
                Log.d("xyz", "SampleOfDropDownMenuLayoutActivity#onOpenDrawer");
            }

            @Override
            public void onCloseDrawer() {
                Log.d("xyz", "SampleOfDropDownMenuLayoutActivity#onCloseDrawer");
            }
        };
    }

    private View.OnClickListener onClickMenuItem() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SampleOfDropDownMenuLayoutActivity.this,
                               R.string.menu_settings,
                               Toast.LENGTH_SHORT)
                     .show();
            }
        };
    }
}
