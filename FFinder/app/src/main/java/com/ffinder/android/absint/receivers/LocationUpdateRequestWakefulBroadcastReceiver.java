package com.ffinder.android.absint.receivers;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import com.ffinder.android.services.LocationUpdateRequestIntentService;

/**
 * Created by sionglengho on 20/11/16.
 */
public class LocationUpdateRequestWakefulBroadcastReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Intent service = new Intent(context, LocationUpdateRequestIntentService.class);
        if(intent != null && intent.hasExtra("senderId")){
            service.putExtra("senderId", intent.getStringExtra("senderId"));
        }
        if(intent != null && intent.hasExtra("senderToken")){
            service.putExtra("senderToken", intent.getStringExtra("senderToken"));
        }
        if(intent != null && intent.hasExtra("fromPlatform")){
            service.putExtra("fromPlatform", intent.getStringExtra("fromPlatform"));
        }
        if(intent != null && intent.hasExtra("isAutoSearch")){
            service.putExtra("isAutoSearch", intent.getStringExtra("isAutoSearch"));
        }


        startWakefulService(context, service);
    }
}
