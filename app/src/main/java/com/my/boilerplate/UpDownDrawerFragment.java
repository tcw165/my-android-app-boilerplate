package com.my.boilerplate;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;

import jp.wasabeef.blurry.Blurry;

public class UpDownDrawerFragment extends Fragment {

    protected View mDrawerBody;
    protected ImageView mDrawerBg;

    public UpDownDrawerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_up_down_drawer, container, false);

        mDrawerBody = layout.findViewById(R.id.drawer_body);
        mDrawerBg = (ImageView) layout.findViewById(R.id.drawer_background);
        mDrawerBg.setOnClickListener(onClickDismiss());

        // For the animation.
        ViewCompat.setTranslationY(mDrawerBody, -mDrawerBody.getLayoutParams().height);
        ViewCompat.setAlpha(mDrawerBg, 0.f);
        Blurry.with(getActivity())
              .radius(13)
              .sampling(32)
              .color(Color.argb(0x66, 0, 0, 0))
              .capture(getActivity().findViewById(R.id.frame))
              .into(mDrawerBg);

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();

        animateEntering();
    }

    public void animateEntering() {
        ViewCompat
            .animate(mDrawerBody)
            .translationY(0.f)
            .setDuration(300)
            .setInterpolator(new AccelerateDecelerateInterpolator())
            .start();
        ViewCompat
            .animate(mDrawerBg)
            .alpha(1.f)
            .setDuration(300)
            .setInterpolator(new AccelerateDecelerateInterpolator())
            .start();
    }

    public void animateExiting() {
        ViewCompat
            .animate(mDrawerBody)
            .translationY(-mDrawerBody.getHeight())
            .setDuration(200)
            .setInterpolator(new AccelerateInterpolator())
            .start();
        ViewCompat
            .animate(mDrawerBg)
            .alpha(0.f)
            .setDuration(200)
            .setInterpolator(new AccelerateInterpolator())
            .setListener(new ViewPropertyAnimatorListener() {
                @Override
                public void onAnimationStart(View view) {}

                @Override
                public void onAnimationEnd(View view) {
                    if (getActivity() != null) {
                        getActivity()
                            .getSupportFragmentManager()
                            .popBackStack();
                    }
                }

                @Override
                public void onAnimationCancel(View view) {}
            })
            .start();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    protected View.OnClickListener onClickDismiss() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animateExiting();
            }
        };
    }
}
