package com.ffinder.android.absint.receivers;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import com.ffinder.android.enums.FCMMessageType;
import com.ffinder.android.services.HandoffToNotificationConsumerIntentService;

/**
 * Created by SiongLeng on 27/9/2016.
 */
public class GcmAliveHeartbeatBroadcastReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        final String action = intent.getStringExtra("action");
        FCMMessageType fcmAction = FCMMessageType.convertStringToFCMMessageType(action);

        if(fcmAction == FCMMessageType.UpdateLocation){
            Intent service = new Intent(context, HandoffToNotificationConsumerIntentService.class);
            service.putExtras(intent);
            startWakefulService(context, service);
        }
    }
}
