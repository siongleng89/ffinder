package com.ffinder.android.absint.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.preference.Preference;
import android.provider.Settings;
import android.widget.Toast;
import com.ffinder.android.enums.PreferenceType;
import com.ffinder.android.helpers.NotificationShower;
import com.ffinder.android.helpers.PreferenceUtils;
import com.ffinder.android.helpers.Strings;
import com.ffinder.android.services.StartGeofencingService;
import com.ffinder.android.statics.Constants;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

/**
 * Created by sionglengho on 19/11/16.
 */
public class GeofencingChangedBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (!geofencingEvent.hasError()) {
            int geofenceTransition = geofencingEvent.getGeofenceTransition();
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                PreferenceUtils.delete(context, PreferenceType.LastLocation);
                PreferenceUtils.delete(context, PreferenceType.LastLocationUnixTime);
                //NotificationShower.show(context, 18888, "Geofence Update Test", "exit", false);
            }
        }

        Intent geofenceIntent = new Intent(context, StartGeofencingService.class);
        context.stopService(geofenceIntent);
    }
}
