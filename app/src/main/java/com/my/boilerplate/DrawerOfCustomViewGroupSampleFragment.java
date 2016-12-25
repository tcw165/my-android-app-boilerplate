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
import android.widget.Toast;

import com.my.boilerplate.view.DropDownMenuLayout;
import com.my.boilerplate.view.INavMenu;

public class DrawerOfCustomViewGroupSampleFragment extends Fragment {

    private Toolbar mToolbar;
    private DropDownMenuLayout mDrawerLayout;

    public DrawerOfCustomViewGroupSampleFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_drawer_of_custom_viewgroup_sample,
                                       container,
                                       false);

        mDrawerLayout = (DropDownMenuLayout) layout;
        mDrawerLayout.setOnMenuStateChangeListener(onMenuStateChange());

        // Set back icon of the toolbar.
        mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_back);
        mToolbar.setTitle("Sample of custom ViewGroup");

        // It wants to contribute the menu option.
        setHasOptionsMenu(true);

        return layout;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu,
                                    MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.drawer_sample_menu, menu);
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
        if (mDrawerLayout.isDrawerOpened()) {
            mDrawerLayout.closeDrawer();
            return true;
        } else {
            return false;
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    private void toggleDrawerMenu() {
        if (mDrawerLayout == null) return;

        if (mDrawerLayout.isDrawerOpened()) {
            mDrawerLayout.closeDrawer();
        } else {
            mDrawerLayout.openDrawer();
        }
    }

    private INavMenu.OnMenuStateChange onMenuStateChange() {
        return new INavMenu.OnMenuStateChange() {
            @Override
            public void onShowMenu() {
                Log.d("xyz", "DrawerOfCustomViewGroupSampleFragment#onShowMenu");
            }

            @Override
            public void onHideMenu() {
                Log.d("xyz", "DrawerOfCustomViewGroupSampleFragment#onHideMenu");
            }
        };
    }

    private View.OnClickListener onClickMenuItem() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),
                               R.string.menu_settings,
                               Toast.LENGTH_SHORT)
                     .show();
            }
        };
    }
}
