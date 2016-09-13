package com.ffinder.android.services;

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
        //Logs.show("MyFirebaseMsgService received msg!!");
        // Check if message contains a data payload.
       // new NotificationConsumer(this).consume("Firebase", remoteMessage.getData());
    }
    // [END receive_message]


}