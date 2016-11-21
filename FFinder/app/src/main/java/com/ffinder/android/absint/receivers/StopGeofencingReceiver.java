package com.ffinder.android.absint.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.ffinder.android.services.StartGeofencingService;

/**
 * Created by sionglengho on 21/11/16.
 */
public class StopGeofencingReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent geoIntent = new Intent(context, StartGeofencingService.class);
        context.stopService(geoIntent);
    }
}
