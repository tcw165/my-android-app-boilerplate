package com.my.boilerplate;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class LongOperationService extends IntentService {

    private static final String TAG = LongOperationService.class.getSimpleName();

    protected static final int NOTIFICATION_ID = 123456;

    NotificationManager mNotiMangr;
    NotificationCompat.Builder mNotibuilder;

    public LongOperationService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // onStartCommand(Intent, int, int) will return START_REDELIVER_INTENT,
        // so if this process dies before onHandleIntent(Intent) returns, the
        // process will be restarted and the intent redelivered.
        setIntentRedelivery(true);

        mNotiMangr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotibuilder = new NotificationCompat
            .Builder(this)
            .setSmallIcon(R.drawable.ic_download)
            .setContentTitle("Long operation")
            .setContentText("Something in progress");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("xyz", String.format("%s::onDestroy", TAG));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("xyz", String.format("%s::onStartCommand(flags=%s, startId=%d)",
                                   TAG,
                                   translateFlags(flags),
                                   startId));

        // TODO: Understand the flag, START_CONTINUATION_MASK.

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.d("xyz", String.format("%s::onTaskRemoved", TAG));
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Log.d("xyz", String.format("%s::onTrimMemory", TAG));
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.d("xyz", String.format("%s::onLowMemory", TAG));
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            for (int i = 0; i < 100; ++i) {
                Log.d("xyz", String.format("%s::progress=%d", TAG, i));

                mNotibuilder.setProgress(100, i, false);
                mNotiMangr.notify(NOTIFICATION_ID, mNotibuilder.build());

                Thread.sleep(100);
            }

            // When the long operation is finished, update the notification.
            mNotibuilder
                .setContentText("Something complete")
                // Remove the progress bar.
                .setProgress(0, 0, false)
                .setContentIntent(
                    PendingIntent.getActivity(
                        this, 0,
                        new Intent(this, NotificationResultSampleActivity1.class),
                        PendingIntent.FLAG_UPDATE_CURRENT))
                .setAutoCancel(true);
            mNotiMangr.notify(NOTIFICATION_ID, mNotibuilder.build());
        } catch (InterruptedException e) {
            Log.d("xyz", String.format("%s::onInterrupt", TAG));
            // Restore interrupt status.
            Thread.currentThread().interrupt();
        }
    }

    protected String translateFlags(int flags) {
        String translation = "";
        List<String> flagStrs = new ArrayList<>();

        if ((flags & START_FLAG_RETRY) == START_FLAG_RETRY) {
            flagStrs.add("START_FLAG_RETRY");
        }
        if ((flags & START_FLAG_REDELIVERY) == START_FLAG_REDELIVERY) {
            flagStrs.add("START_FLAG_REDELIVERY");
        }

        for (int i = 0; i < flagStrs.size(); ++i) {
            translation += flagStrs.get(i);
            if (i > 0 && i < flagStrs.size() - 1) {
                translation += "|";
            }
        }

        return translation;
    }
}
