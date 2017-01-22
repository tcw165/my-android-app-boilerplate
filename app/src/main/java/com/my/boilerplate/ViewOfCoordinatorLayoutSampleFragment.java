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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.my.widget.ElasticDragDismissLayout;
import com.my.widget.ElasticDragMenuLayout;

public class ViewOfCoordinatorLayoutSampleFragment extends Fragment {

    Toolbar mToolbar;
    ElasticDragMenuLayout mLayout;
//    DropDownMenuView mDrawerMenu;

    public ViewOfCoordinatorLayoutSampleFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mLayout = (ElasticDragMenuLayout) inflater.inflate(
            R.layout.fragment_view_of_coordinator_layout_sample,
            container,
            false);
        mLayout.addOnDragDismissListener(new ElasticDragDismissLayout.DragDismissCallback() {
            @Override
            public void onDrag(float elasticOffset, float elasticOffsetPixels, float rawOffset, float rawOffsetPixels) {
                // DO NOTHING.
            }

            @Override
            public void onDragDismissed(float totalScroll) {
                Log.d("xyz", "onDragDismissed");
            }

            @Override
            public void onBackPressedDismissed() {
                // DO NOTHING.
            }

            @Override
            public void onCoverPressedDismissed() {
                // DO NOTHING.
            }
        });

//        mDrawerMenu = (DropDownMenuView) layout.findViewById(R.id.drawer_menu);
//        mDrawerMenu.addOnDrawerStateChangeListener(onMenuStateChange());
//        mDrawerMenu.setOnClickMenuItemListener(onClickMenuItem());

        // Set back icon of the toolbar.
        mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_back);
        mToolbar.setTitle("CoordinatorLayout");

        // It wants to contribute the menu option.
        setHasOptionsMenu(true);

        return mLayout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mLayout.removeAllOnDragDismissListeners();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu,
                                    MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_drawer_sample, menu);
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

    public boolean onBackPressed() {
        if (mLayout == null) return false;

        if (mLayout.isMenuOpened()) {
            mLayout.closeMenu();
            return true;
        } else {
            return false;
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    private void toggleDrawerMenu() {
        if (mLayout == null) return;

        if (mLayout.isMenuOpened()) {
            mLayout.closeMenu();
        } else {
            mLayout.openMenu();
        }
    }

//    private IDrawerViewLayout.OnDrawerStateChange onMenuStateChange() {
//        return new IDrawerViewLayout.OnDrawerStateChange() {
//            @Override
//            public void onOpenDrawer() {
//                // DO NOTHING.
//            }
//
//            @Override
//            public void onCloseDrawer() {
//                // DO NOTHING.
//            }
//        };
//    }
//
//    private View.OnClickListener onClickMenuItem() {
//        return new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(getActivity(),
//                               R.string.menu_settings,
//                               Toast.LENGTH_SHORT)
//                     .show();
//            }
//        };
//    }
}
