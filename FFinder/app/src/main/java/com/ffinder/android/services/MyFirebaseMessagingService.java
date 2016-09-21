package com.ffinder.android.services;

import com.ffinder.android.helpers.NotificationConsumer;
import com.ffinder.android.utils.Logs;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Logs.show("MyFirebaseMsgService received msg!!");
        new NotificationConsumer(this).consume(remoteMessage.getData());
    }
    // [END receive_message]


}