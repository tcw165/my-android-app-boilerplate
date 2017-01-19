package com.my.boilerplate;

import android.content.Intent;
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
 * The start menu of the {@code ViewSampleActivity}.
 */
public class ViewSampleFragment extends Fragment {

    private Toolbar mToolbar;

    public ViewSampleFragment() {
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
        mToolbar.setTitle(getString(R.string.title_view_sample));

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();

        mToolbar.setNavigationIcon(R.drawable.ic_toolbar_close);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    @SuppressWarnings({"unchecked"})
    protected SampleMenuAdapter onSampleMenuCreate() {
        return new SampleMenuAdapter(
            getActivity(),
            new Pair[]{
                new Pair<>("CoordinatorLayout and Behavior",
                           "Use the CoordinatorLayout and Behavior to imitate " +
                           "the drag-and-drop drawer menu in the vertical way. " +
                           "I use \"imitate\" here is because I think the best " +
                           "solution is to customize a ViewGroup."),
                new Pair<>("Custom ViewGroup behaves like DrawerLayout",
                           "The custom ViewGroup is responsible for intercept " +
                           "the dragging touch event."),
                new Pair<>("Custom ElasticDragDismissFrameLayout.",
                           "Idea inspired from the sample code of Plaid app. It " +
                           "is using a translucent Activity to implement the " +
                           "drag-to-dismiss gesture.")
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
                            .replace(R.id.frame, new ViewOfCoordinatorLayoutSampleFragment())
                            .addToBackStack(null)
                            .commit();
                        break;
                    case 1:
                        getActivity()
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frame, new ViewOfDropDownMenuLayoutSampleFragment())
                            .addToBackStack(null)
                            .commit();
                        break;
                    case 2:
//                        startActivity(
//                            new Intent(getActivity(),
//                                       ViewOfDragDismissSampleActivity.class),
//                            // Necessary option to enable the scene transition.
//                            ActivityOptionsCompat
//                                .makeSceneTransitionAnimation(
//                                    getActivity(),
//                                    (android.support.v4.util.Pair<View, String>[]) null)
//                                .toBundle());
                        startActivity(
                            new Intent(getActivity(),
                                       ViewOfDragDismissSampleActivity.class));
                        break;
                }
            }
        };
    }
}