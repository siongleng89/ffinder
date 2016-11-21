package com.ffinder.android.services;

import android.app.IntentService;
import android.content.Intent;
import com.ffinder.android.absint.receivers.GcmAliveHeartbeatBroadcastReceiver;
import com.ffinder.android.helpers.NotificationConsumer;
import com.ffinder.android.helpers.Threadings;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by SiongLeng on 27/9/2016.
 */
public class HandoffToNotificationConsumerIntentService extends IntentService {

    public HandoffToNotificationConsumerIntentService() {
        super("HandoffToNotificationConsumerIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(intent != null){
            final String action = intent.getStringExtra("action");
            final String senderId = intent.getStringExtra("senderId");
            final String messageId = intent.getStringExtra("messageId");
            final String senderToken = intent.getStringExtra("senderToken");

            Map<String, String> map = new HashMap();
            map.put("action", action);
            map.put("senderId", senderId);
            map.put("messageId", messageId);
            map.put("senderToken", senderToken);

            //sleep for 2secs, to prevent duplicate handling of gcm and fcm
            Threadings.sleep(2000);

            new NotificationConsumer(this).consume(map);
            GcmAliveHeartbeatBroadcastReceiver.completeWakefulIntent(intent);
        }

    }

}
