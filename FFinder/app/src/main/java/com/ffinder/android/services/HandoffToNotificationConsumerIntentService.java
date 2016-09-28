package com.ffinder.android.services;

import android.app.IntentService;
import android.content.Intent;
import com.ffinder.android.absint.receivers.GcmAliveHeartbeatBroadcastReceiver;
import com.ffinder.android.helpers.NotificationConsumer;
import com.ffinder.android.utils.Threadings;

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
        final String action = intent.getStringExtra("action");
        final String senderId = intent.getStringExtra("senderId");
        final String messageId = intent.getStringExtra("messageId");

        Map<String, String> map = new HashMap();
        map.put("action", action);
        map.put("senderId", senderId);
        map.put("messageId", messageId);
        Threadings.sleep(2000);
        new NotificationConsumer(this).consume(map);
        GcmAliveHeartbeatBroadcastReceiver.completeWakefulIntent(intent);
    }

}
