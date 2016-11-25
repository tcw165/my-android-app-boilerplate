package com.my.boilerplate;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Handle data payload of FCM messages.
        Log.d("xyz", "FCM Message Id: " + remoteMessage.getMessageId());
        Log.d("xyz", "FCM Notification Message: " +
                     remoteMessage.getNotification());
        Log.d("xyz", "FCM Data Message: " + remoteMessage.getData());
    }
}
