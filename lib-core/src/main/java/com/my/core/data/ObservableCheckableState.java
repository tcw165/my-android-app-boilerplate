package com.my.core.data;

import android.os.Handler;
import android.os.Looper;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * It is a state representing the checkable mode and could be subscribed to.
 */
// TODO: Save/restore state?
public class ObservableCheckableState {

    private List<OnStateChangeListener> mCallbacks;
    private boolean mIsCheckable;
    private Handler mUiHandler;

    public ObservableCheckableState() {
        mIsCheckable = false;
        mUiHandler = new Handler(Looper.myLooper());
    }

    public boolean isCheckable() {
        return mIsCheckable;
    }

    public synchronized void setCheckable(boolean checkable) {
        final boolean changed = mIsCheckable != checkable;

        mIsCheckable = checkable;

        if (changed) {
            dispatchUpdateCheckableCallbacks(mIsCheckable);
        }
    }

    public void addOnStateChangeListener(OnStateChangeListener listener) {
        // Ensure the list.
        if (mCallbacks == null) {
            mCallbacks = new CopyOnWriteArrayList<>();
        }

        mCallbacks.add(listener);
    }

    @SuppressWarnings("unused")
    public void removeOnStateChangeListener(OnStateChangeListener listener) {
        if (mCallbacks == null) return;

        mCallbacks.remove(listener);
    }

    @SuppressWarnings("unused")
    public void removeAllOnStateChangeListener() {
        if (mCallbacks == null) return;

        mCallbacks.clear();
    }

    ///////////////////////////////////////////////////////////////////////////
    // private/protected //////////////////////////////////////////////////////

    private void dispatchUpdateCheckableCallbacks(final boolean checkable) {
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mCallbacks == null) return;

                for (OnStateChangeListener callback : mCallbacks) {
                    callback.onUpdateCheckableState(checkable);
                }
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    public interface OnStateChangeListener {
        void onUpdateCheckableState(boolean checkable);
    }
}
