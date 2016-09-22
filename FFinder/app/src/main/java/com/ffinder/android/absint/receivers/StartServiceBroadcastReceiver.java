package com.ffinder.android.absint.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.ffinder.android.services.TestService;

/**
 * Created by SiongLeng on 22/9/2016.
 */
public class StartServiceBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent startServiceIntent = new Intent(context, TestService.class);
        context.startService(startServiceIntent);
    }
}
