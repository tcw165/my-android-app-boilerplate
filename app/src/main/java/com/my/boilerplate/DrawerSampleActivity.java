// Copyright (c) 2016 boyw165
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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.my.boilerplate.view.DropDownMenuView;
import com.my.boilerplate.view.INavMenu;

public class DrawerSampleActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private DropDownMenuView mDrawerMenu;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_drawer_sample);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mDrawerMenu = (DropDownMenuView) findViewById(R.id.drawer_menu);
        mDrawerMenu.setOnMenuStateChangeListener(onMenuStateChange());
        mDrawerMenu.setOnClickMenuItemListener(onClickMenuItem());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.drawer_sample_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_drawer:
                toggleDrawerMenu();
                break;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        if (mDrawerMenu != null && mDrawerMenu.isShowing()) {
            mDrawerMenu.hideWithAnimation();
        } else {
            super.onBackPressed();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    private void toggleDrawerMenu() {
        if (mDrawerMenu == null) return;

        if (mDrawerMenu.isShowing()) {
            mDrawerMenu.hideWithAnimation();
        } else {
            mDrawerMenu.showWithAnimation();
        }
    }

    private INavMenu.OnMenuStateChange onMenuStateChange() {
        return new INavMenu.OnMenuStateChange() {
            @Override
            public void onShowMenu() {
                // DO NOTHING.
            }

            @Override
            public void onHideMenu() {
                // DO NOTHING.
            }
        };
    }

    private OnClickListener onClickMenuItem() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DrawerSampleActivity.this,
                               R.string.menu_settings,
                               Toast.LENGTH_SHORT)
                     .show();
            }
        };
    }
}
