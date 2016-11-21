package com.ffinder.android.tasks;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import com.ffinder.android.absint.receivers.AutoSearchWakefulBroadcastReceiver;
import com.ffinder.android.enums.FCMMessageType;
import com.ffinder.android.enums.PreferenceType;
import com.ffinder.android.helpers.*;
import com.ffinder.android.models.MyModel;
import com.ffinder.android.services.StartGeofencingService;
import com.ffinder.android.statics.Constants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by sionglengho on 20/11/16.
 */
public class LocationUpdateTask extends AsyncTask<String, String, Pair<String, String>>
                    implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, android.location.LocationListener{

    private Context context;
    private MyModel myModel;
    private String fromUserId;
    private String fromUserToken;
    private String fromPlatform;
    private boolean locationSharingAllowed;
    private Location lastLocation;
    private boolean finish;
    private LocationManager locationManager;
    private GoogleApiClient googleApiClient;
    private String finalLatitude, finalLongitude;
    private RunnableArgs<Pair<String, String>> callback;

    public LocationUpdateTask(Context context, String
            fromUserId, String fromUserToken, String fromPlatform) {
        this.context = context;
        this.fromUserId = fromUserId;
        this.fromUserToken = fromUserToken;
        this.fromPlatform = fromPlatform;
        this.myModel = new MyModel(context);
        this.finalLatitude = "";
        this.finalLongitude = "";
    }

    public LocationUpdateTask(Context context, RunnableArgs<Pair<String, String>> onFinish) {
        this.context = context;
        this.myModel = new MyModel(context);
        this.finalLatitude = "";
        this.finalLongitude = "";
        this.callback = onFinish;
    }

    @Override
    protected Pair<String, String> doInBackground(String... strings) {


        locationSharingAllowed = AndroidUtils.checkLocationSharingAllowed(context);

        //if from usertoken not empty, means it is through normal search, reply alive msg
        if(!Strings.isEmpty(fromUserToken)) {
            replyAliveMsg(myModel.getUserId(), fromUserToken);
        }

        if(locationSharingAllowed){
            //start retrieve location
            googleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            googleApiClient.connect();


            try{

                locationManager = (LocationManager)
                        context.getSystemService(Context.LOCATION_SERVICE);

                if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            0, 0, LocationUpdateTask.this, Looper.getMainLooper());
                }


            }
            catch (SecurityException ex){

            }

        }
        else{
            finish = true;
            removeAutoSearchAlarmManager();
        }

        while (!finish){
            Threadings.sleep(500);
        }


        if(this.callback != null){
            Threadings.postRunnable(new Runnable() {
                @Override
                public void run() {
                    callback.run(new Pair<String, String>(finalLatitude, finalLongitude));
                }
            });
        }

        //wait for 10 secs, to ensure all notification is sent
        Threadings.sleep(10 * 1000);

        return new Pair<String, String>(finalLatitude, finalLongitude);
    }

    //monitor for some time, if still cannot get location,
    //resort to gps location manager
    private void startMonitoring(){
        Threadings.runInBackground(new Runnable() {
            @Override
            public void run() {

                Threadings.sleep(5000);
                if(finish) return;

                //resort one, use gps location manager
                try{
                    if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                0, 0, LocationUpdateTask.this, Looper.getMainLooper());
                    }
                }
                catch (SecurityException ex){

                }

                Threadings.sleep(5000);
                if(finish) return;

                //resort two, use cache location
                String coords = PreferenceUtils.get(context, PreferenceType.LastLocation);
                String lastUpdateMs = PreferenceUtils.get(context, PreferenceType.LastLocationUnixTime);

                if(!Strings.isEmpty(coords) && Strings.isNumeric(lastUpdateMs)){
                    if(System.currentTimeMillis() - Long.valueOf(lastUpdateMs) <
                                                            Constants.GeofenceExpiredMs){
                        String[] coordsArr = coords.split(",");
                        if(coordsArr.length >= 2){
                            onLocationChanged(coordsArr[0], coordsArr[1]);
                        }
                    }
                }

            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(2000);
        mLocationRequest.setFastestInterval(50);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        try{
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

            LocationServices.FusedLocationApi.requestLocationUpdates(
                    googleApiClient, mLocationRequest, this);
            startMonitoring();
        }
        catch (SecurityException ex){

        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null && !finish) {
            String latitude = String.valueOf(location.getLatitude());
            String longitude = String.valueOf(location.getLongitude());

            PreferenceUtils.put(context, PreferenceType.LastLocation,
                    latitude + "," + longitude);
            PreferenceUtils.put(context, PreferenceType.LastLocationUnixTime,
                    String.valueOf(System.currentTimeMillis()));

            //start geofencing cache expired awareness service
            Intent intent = new Intent(context, StartGeofencingService.class);
            intent.putExtra("latitude", latitude);
            intent.putExtra("longitude", longitude);
            context.startService(intent);


            onLocationChanged(latitude, longitude);
        }
    }

    public void onLocationChanged(String latitude, String longitude){
        if(googleApiClient != null) {
            googleApiClient.disconnect();
        }
        if(locationManager != null) {
            try{
                locationManager.removeUpdates(this);
            }
            catch(SecurityException ex){

            }
        }

        locationSuccessfullyRetrieved(latitude, longitude);
    }


    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }


    private void replyAliveMsg(String myUserId, String toUserToken){
        NotificationSender.sendWithToken(myUserId, toUserToken,
                FCMMessageType.IsAliveMsg, NotificationSender.TTL_INSTANT, null,
                fromPlatform, false, new Pair<String, String>("locationDisabled",
                        locationSharingAllowed ? "0" : "1"));
    }

    private void locationSuccessfullyRetrieved(final String latitude, final String longitude){
        if(finish){
            return;
        }

        finalLatitude = latitude;
        finalLongitude = longitude;
        finish = true;

        Logs.show("Updating my location to: " + latitude + ", " + longitude);

        //instant callback type location updater
        if(!Strings.isEmpty(fromUserToken)){
            //immediately reply fcm with current location
            NotificationSender.sendWithToken(myModel.getUserId(), fromUserToken,
                    FCMMessageType.UserLocated, NotificationSender.TTL_INSTANT,
                    null, fromPlatform, true,
                    new Pair<String, String>("latitude", latitude),
                    new Pair<String, String>("longitude", longitude),
                    //is auto notificaiton decide whether show push notification on user tray
                    new Pair<String, String>("isAutoNotification", "0"));

        }


        if(this.myModel != null){
            long current = System.currentTimeMillis();
            String lastLastUpdatedMiliSecs =
                    PreferenceUtils.get(context, PreferenceType.LastSentAutoNotification);

            //five seconds window, make sure dont keep sending
            if(Strings.isNumeric(lastLastUpdatedMiliSecs)){
                if(current - Long.valueOf(lastLastUpdatedMiliSecs) < 1000 * 5){
                    return;
                }
            }

            //send to those waiting auto notificaiton my userid topic subscribers
            NotificationSender.sendToTopic(myModel.getUserId(),
                    myModel.getUserId(),
                    FCMMessageType.UserLocated, NotificationSender.TTL_LONG,
                    null,
                    new Pair<String, String>("latitude", latitude),
                    new Pair<String, String>("longitude", longitude),
                    new Pair<String, String>("isAutoNotification", "1"));

            Logs.show("Send to topic");
            PreferenceUtils.put(context, PreferenceType.LastSentAutoNotification,
                    String.valueOf(current));
        }

        removeAutoSearchAlarmManager();

    }

    private void removeAutoSearchAlarmManager(){
        AlarmManager alarmMgr =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent searchIntent = new Intent(context,
                AutoSearchWakefulBroadcastReceiver.class);

        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, searchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        alarmMgr.cancel(alarmIntent);
    }

    public void terminate(){
        finish = true;
        if(googleApiClient != null) {
            googleApiClient.disconnect();
        }
        if(locationManager != null) {
            try{
                locationManager.removeUpdates(this);
            }
            catch(SecurityException ex){

            }
        }
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
