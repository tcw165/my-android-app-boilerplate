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
import com.my.comp.PhotoPickerActivity;

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
                new Pair<>("CameraTextureView",
                           "Use TextureView or SurfaceView to provide the " +
                           "camera feature."),
                new Pair<>("DropDownMenuLayout",
                           "The ViewGroup is responsible for intercepting the " +
                           "touch event."),
                new Pair<>("ElasticDragLayout",
                           "Inheriting from CoordinatorLayout and support elastic " +
                           "drag UX like iOS's scroll-view."),
                new Pair<>("ElasticDragDismissLayout (ElasticDragLayout)",
                           "Idea inspired from the sample code of Plaid app. It " +
                           "inherits from the CoordinatorLayout and is using a " +
                           "translucent Activity to implement the drag-to-dismiss " +
                           "gesture."),
                new Pair<>("ElasticDragMenuLayout (ElasticDragLayout)",
                           "A child class inheriting from ElasticDragDismissLayout. " +
                           "The layout allows a NestedScrollingChild child view " +
                           "being over dragged."),
                new Pair<>("PhotoPickerView",
                           "A google-photo like picker.")
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
                        startActivity(
                            new Intent(getActivity(),
                                       ViewOfCameraSampleActivity.class));
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
                        getActivity()
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frame, new ViewOfElasticDragLayoutSampleFragment())
                            .addToBackStack(null)
                            .commit();
                        break;
                    case 3:
//                        startActivity(
//                            new Intent(getActivity(),
//                                       ViewOfElasticDragDismissSampleActivity.class),
//                            // Necessary option to enable the scene transition.
//                            ActivityOptionsCompat
//                                .makeSceneTransitionAnimation(
//                                    getActivity(),
//                                    (android.support.v4.util.Pair<View, String>[]) null)
//                                .toBundle());
                        startActivity(
                            new Intent(getActivity(),
                                       ViewOfElasticDragDismissSampleActivity.class));
                        break;
                    case 4:
                        getActivity()
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frame, new ViewOfElasticDragMenuLayoutSampleFragment())
                            .addToBackStack(null)
                            .commit();
                        break;
                    case 5:
                        startActivityForResult(
                            new Intent(getActivity(), PhotoPickerActivity.class), 0);
                        break;
                }
            }
        };
    }
}
