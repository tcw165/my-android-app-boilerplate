package com.my.boilerplate;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.my.boilerplate.view.SampleMenuAdapter;

/**
 * The start menu of the {@code DrawerSampleActivity}.
 */
public class DrawerSampleFragment extends Fragment {

    private Toolbar mToolbar;

    public DrawerSampleFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_drawer_sample,
                                       container,
                                       false);

        ListView menu = (ListView) layout;
        menu.setAdapter(onSampleMenuCreate());
        menu.setOnItemClickListener(onClickMenuItem());

        mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();

        mToolbar.setNavigationIcon(R.drawable.ic_close);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    @SuppressWarnings({"unchecked"})
    protected SampleMenuAdapter onSampleMenuCreate() {
        return new SampleMenuAdapter(
            getActivity(),
            new Pair[] {
                new Pair<>("CoordinatorLayout and Behavior",
                           "Use the CoordinatorLayout and Behavior to imitate " +
                           "the drag-and-drop drawer menu in the vertical way. " +
                           "I use \"imitate\" here is because I think the best " +
                           "solution is to customize a ViewGroup."),
                new Pair<>("Custom ViewGroup behaves like DrawerLayout",
                           "The custom ViewGroup is responsible for intercept " +
                           "the dragging touch event.")
            });
    }

    protected AdapterView.OnItemClickListener onClickMenuItem() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent,
                                    View view,
                                    int position,
                                    long id) {
                switch (position) {
                    case 0:
                        getActivity()
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frame, new CoordinatorLayoutExampleFragment())
                            .addToBackStack(null)
                            .commit();
                        break;
                    case 1:
                        break;
                }
            }
        };
    }
}
