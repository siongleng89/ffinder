package com.ffinder.android.absint.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import com.ffinder.android.services.AutoSearchIntentService;

/**
 * Created by sionglengho on 21/11/16.
 */
public class AutoSearchWakefulBroadcastReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent autoSearchIntentService = new Intent(context, AutoSearchIntentService.class);
        int i = 0;
        if(intent != null && intent.hasExtra("count")){
            i = intent.getIntExtra("count", 0);
        }

        autoSearchIntentService.putExtra("count", i);
        startWakefulService(context, autoSearchIntentService);
    }
}
