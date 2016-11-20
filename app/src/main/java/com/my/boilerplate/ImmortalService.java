package com.my.boilerplate;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

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
        Log.d("xyz", String.format("%s::onStartCommand", TAG));
        return super.onStartCommand(intent, flags, startId);
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
}
