package com.my.widget;

/**
 * View/ViewGroup implementing this interface supports notifying the status of
 * the drawer.
 */
public interface IDrawerViewLayout {

    /**
     * Set the listener that subscribes to the status of the drawer.
     */
    void setOnDrawerStateChangeListener(OnDrawerStateChange listener);

    /**
     * Activity/Fragment implementing this interface supports receiving the
     * update of the drawer status.
     */
    interface OnDrawerStateChange {
        void onOpenDrawer();

        void onCloseDrawer();
    }
}
