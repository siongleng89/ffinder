package com.ffinder.android.absint.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

/**
 * Created by sionglengho on 21/11/16.
 */
public class LocationProviderChangedBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        LocationManager locationManager = (LocationManager)
                context.getSystemService(Context.LOCATION_SERVICE);

        if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Intent intent2 = new Intent();
            intent2.setAction("com.ffinder.android.GET_LOCATION");
            context.sendBroadcast(intent2);
        }

    }
}
