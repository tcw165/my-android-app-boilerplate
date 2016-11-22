package com.my.boilerplate;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Testing how long could a Service last even if the task is killed.
 */
public class ImmortalService extends Service {

    private static final String TAG = ImmortalService.class.getSimpleName();

    public ImmortalService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("xyz", String.format("%s::onCreate", TAG));
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
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
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
