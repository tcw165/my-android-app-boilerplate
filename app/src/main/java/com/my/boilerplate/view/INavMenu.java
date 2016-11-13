package com.my.boilerplate.view;

public interface INavMenu {
    void setOnMenuStateChangeListener(OnMenuStateChange listener);

    interface OnMenuStateChange {
        void onShowMenu();

        void onHideMenu();
    }
}
