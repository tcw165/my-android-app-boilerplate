package com.my.core.protocol;

/**
 * View/ViewGroup implementing this interface supports notifying the status of
 * the drawer.
 */
public interface IDrawerViewLayout {

    /**
     * Add the listener that subscribes to the status of the drawer.
     */
    void addOnDrawerStateChangeListener(OnDrawerStateChange listener);

    /**
     * Remove the listener.
     */
    void removeOnDrawerStateChangeListener(OnDrawerStateChange listener);

    /**
     * Remove all the listeners.
     */
    void removeAllOnDrawerStateChangeListeners();

    /**
     * Activity/Fragment implementing this interface supports receiving the
     * update of the drawer status.
     */
    interface OnDrawerStateChange {
        void onOpenDrawer();

        void onCloseDrawer();
    }
}
