package com.ffinder.android.absint.receivers;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import com.ffinder.android.enums.FCMMessageType;
import com.ffinder.android.helpers.NotificationConsumer;
import com.ffinder.android.services.GcmAliveHeartbeatService;
import com.ffinder.android.services.HandoffToNotificationConsumerIntentService;
import com.ffinder.android.utils.Strings;
import com.ffinder.android.utils.Threadings;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by SiongLeng on 27/9/2016.
 */
public class GcmAliveHeartbeatBroadcastReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        final String action = intent.getStringExtra("action");
        final String senderId = intent.getStringExtra("senderId");
        final String messageId = intent.getStringExtra("messageId");

        if(!Strings.isEmpty(action) && !Strings.isEmpty(messageId)){
            FCMMessageType fcmAction = null;
            for (FCMMessageType c : FCMMessageType.values()) {
                if (c.name().equals(action)) {
                    fcmAction = FCMMessageType.valueOf(action);
                    break;
                }
            }

            if(fcmAction == FCMMessageType.UpdateLocation){
                Intent service = new Intent(context, HandoffToNotificationConsumerIntentService.class);
                service.putExtra("action", action);
                service.putExtra("senderId", senderId);
                service.putExtra("messageId", messageId);
                startWakefulService(context, service);
            }
        }
    }
}
