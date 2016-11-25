package com.my.boilerplate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

public class LongOperationReceiver extends BroadcastReceiver {

    private static final String TAG = LongOperationReceiver.class.getCanonicalName();

    public static final String ACTION_START = TAG + ".action_start";
    public static final String ACTION_PAUSE = TAG + ".action_pause";
    public static final String ACTION_CANCEL = TAG + ".action_cancel";
    public static final String ACTION_COMPLETE = TAG + ".action_complete";
    public static final String ACTION_UPDATE_PROGRESS = TAG + ".action_update_progress";

    /**
     * The progress is an integer [0..100].
     */
    public static final String DATA_PROGRESS = TAG + ".data_progress";

    protected ILongOperation mListener;

    public LongOperationReceiver(ILongOperation listener) {
        mListener = listener;
    }

    public static IntentFilter createIntentFilter() {
        IntentFilter filter = new IntentFilter();

        filter.addAction(ACTION_START);
        filter.addAction(ACTION_PAUSE);
        filter.addAction(ACTION_CANCEL);
        filter.addAction(ACTION_COMPLETE);
        filter.addAction(ACTION_UPDATE_PROGRESS);

        return filter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (TextUtils.isEmpty(intent.getAction())) return;

        String action = intent.getAction();
        if (action.equalsIgnoreCase(ACTION_START)) {
            if (mListener != null) {
                mListener.onProgressStart();
            }
        } else if (action.equalsIgnoreCase(ACTION_PAUSE)) {
            if (mListener != null) {
                mListener.onProgressPause();
            }
        } else if (action.equalsIgnoreCase(ACTION_CANCEL)) {
            if (mListener != null) {
                mListener.onProgressCancel();
            }
        } else if (action.equalsIgnoreCase(ACTION_UPDATE_PROGRESS)) {
            if (mListener != null) {
                int progress = intent.getIntExtra(DATA_PROGRESS, 0);
                mListener.onProgressUpdate(progress);
            }
        } else if (action.equalsIgnoreCase(ACTION_COMPLETE)) {
            if (mListener != null) {
                mListener.onProgressComplete();
            }
        } else {
            throw new IllegalArgumentException("The unknown action.");
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    public interface ILongOperation {
        void onProgressStart();
        void onProgressPause();
        void onProgressCancel();
        void onProgressComplete();
        void onProgressUpdate(int progress);
    }
}
