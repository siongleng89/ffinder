package com.ffinder.android.absint.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import com.ffinder.android.helpers.RequestLocationHandler;
import com.ffinder.android.models.MyModel;
import com.ffinder.android.utils.Logs;
import com.ffinder.android.utils.RunnableArgs;

/**
 * Created by SiongLeng on 21/9/2016.
 */
public class LocationBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {

        LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(network_enabled) {
            final MyModel myModel = new MyModel(context);
            myModel.loginFirebase(0, new RunnableArgs<Boolean>() {
                @Override
                public void run() {
                    new RequestLocationHandler(context, null, myModel).run();
                }
            });
        }

    }
}
