package com.ffinder.android.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;
import com.ffinder.android.absint.receivers.GeofencingChangedBroadcastReceiver;
import com.ffinder.android.absint.receivers.StopGeofencingReceiver;
import com.ffinder.android.helpers.Strings;
import com.ffinder.android.statics.Constants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

/**
 * Created by sionglengho on 20/11/16.
 */
public class StartGeofencingService extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient googleApiClient;
    private String latitude, longitude;
    private PendingIntent mGeofencePendingIntent;
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null && intent.hasExtra("latitude") && intent.hasExtra("longitude")){
            latitude = intent.getStringExtra("latitude");
            longitude = intent.getStringExtra("longitude");

            if(googleApiClient == null){
                googleApiClient = new GoogleApiClient.Builder(getBaseContext())
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build();
                googleApiClient.connect();
            }
            else{
                if(!googleApiClient.isConnected()){
                    googleApiClient.connect();
                }
                else{
                    removeGeofencing();
                    addGeofencing();
                }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        addGeofencing();
    }

    public void addGeofencing(){
        if(Strings.isEmpty(latitude) || Strings.isEmpty(longitude)){
            return;
        }


        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_EXIT);
        ArrayList<Geofence> mGeofenceList = new ArrayList();
        mGeofenceList.add(new Geofence.Builder()
                .setRequestId("geofence_id")
                .setCircularRegion(
                        Double.valueOf(latitude),
                        Double.valueOf(longitude),
                        300
                )
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
                .setExpirationDuration(Constants.GeofenceExpiredMs)
                .build());

        builder.addGeofences(mGeofenceList);
        GeofencingRequest request =  builder.build();

        try {
            LocationServices.GeofencingApi.addGeofences(
                    googleApiClient,
                    request,
                    getGeofencePendingIntent()
            );


            alarmMgr = (AlarmManager) getBaseContext().getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(getBaseContext(), StopGeofencingReceiver.class);
            alarmIntent = PendingIntent.getBroadcast(getBaseContext(), 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() +
                            Constants.GeofenceExpiredMs, alarmIntent);

            //Toast.makeText(getBaseContext(), "Geofencing starting...", Toast.LENGTH_LONG).show();


        } catch (SecurityException securityException) {

        }
    }

    public void removeGeofencing(){
        try {
            LocationServices.GeofencingApi.removeGeofences(
                    googleApiClient,
                    getGeofencePendingIntent()
            );
        } catch (SecurityException securityException) {
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Toast.makeText(getBaseContext(), "Geofencing destroying...", Toast.LENGTH_LONG).show();

        if(googleApiClient != null){
            if(googleApiClient.isConnected()){
                removeGeofencing();
            }
            googleApiClient.disconnect();
            googleApiClient = null;
        }
        if (alarmMgr!= null) {
            alarmMgr.cancel(alarmIntent);
        }

    }

    private PendingIntent getGeofencePendingIntent() {
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }

        Intent intent = new Intent(this, GeofencingChangedBroadcastReceiver.class);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

}
