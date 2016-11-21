package com.ffinder.android.helpers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.util.Pair;
import com.ffinder.android.enums.FCMMessageType;
import com.ffinder.android.enums.PreferenceType;
import com.ffinder.android.models.LocationModel;
import com.ffinder.android.models.MyModel;
import com.ffinder.android.statics.Constants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by SiongLeng on 29/8/2016.
 */
public class LocationUpdater implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, android.location.LocationListener {

    private static long lastLastUpdatedMiliSecs;
    private GoogleApiClient googleApiClient;
    private Context context;
    private MyModel myModel;
    private String fromUserId;
    private String fromUserToken;
    private String fromPlatform;
    private boolean locationSharingAllowed;
    private RunnableArgs<Pair<String, String>> callback;
    private Location lastLocation;
    private boolean finish;
    private LocationManager locationManager;

    public LocationUpdater(Context context, String fromUserId,
                           String fromPlatform, String fromUserToken) {
        this.context = context;
        this.fromUserId = fromUserId;
        this.fromUserToken = fromUserToken;
        this.fromPlatform = fromPlatform;
        this.myModel = new MyModel(context);

        run();
    }

    public LocationUpdater(Context context, RunnableArgs<Pair<String, String>> callback){
        this.context = context;
        this.callback = callback;

        run();
    }


    private void run(){

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
                            0, 0, LocationUpdater.this, Looper.getMainLooper());
                }


            }
            catch (SecurityException ex){

            }


        }

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

    //monitor for some time, if still cannot get location,
    //resort to gps location manager
    private void startMonitoring(){
        Threadings.runInBackground(new Runnable() {
            @Override
            public void run() {

                Threadings.sleep(2000);
                if(finish) return;

                //resort one, use gps location manager
                try{
                    if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                0, 0, LocationUpdater.this, Looper.getMainLooper());
                    }
                }
                catch (SecurityException ex){

                }

                Threadings.sleep(3000);
                if(finish) return;

                //resort two, use cache location
                String coords = PreferenceUtils.get(context, PreferenceType.LastLocation);
                if(!Strings.isEmpty(coords)){
                    String[] coordsArr = coords.split(",");
                    if(coordsArr.length >= 2){
                        onLocationChanged(coordsArr[0], coordsArr[1]);
                    }
                }

            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {
        Logs.show("Fused connection suspended: " + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Logs.show("Fused locator connection failed: " + connectionResult);
    }


    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            //accuracy is too bad, discard it now
            if (location.hasAccuracy() && location.getAccuracy() > 1000){
                return;
            }
            String latitude = String.valueOf(location.getLatitude());
            String longitude = String.valueOf(location.getLongitude());
            onLocationChanged(latitude, longitude);
            PreferenceUtils.put(context, PreferenceType.LastLocation,
                    latitude + "," + longitude);
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
        finish = true;

        //instant callback type location updater
        if(callback != null){
            callback.run(new Pair<String, String>(latitude, longitude));
        }
        else{

            final LocationModel locationModel = new LocationModel();
            locationModel.setLatitude(latitude);
            locationModel.setLongitude(longitude);

            Logs.show("Updating my location to: " + latitude + ", " + longitude);

            //immediately reply fcm with current location
            if(!Strings.isEmpty(fromUserToken)){
                NotificationSender.sendWithToken(myModel.getUserId(), fromUserToken,
                        FCMMessageType.UserLocated, NotificationSender.TTL_INSTANT,
                        null, fromPlatform, true,
                        new Pair<String, String>("latitude", latitude),
                        new Pair<String, String>("longitude", longitude),
                        //is auto notificaiton decide whether show push notification on user tray
                        new Pair<String, String>("isAutoNotification", "0"));

            }

            long current = System.currentTimeMillis();
            //five seconds window, make sure dont keep sending
            if(current - lastLastUpdatedMiliSecs > 1000 * 5){
                lastLastUpdatedMiliSecs = current;

                //send to those waiting auto notificaiton my userid topic subscribers
                NotificationSender.sendToTopic(myModel.getUserId(),
                        myModel.getUserId(),
                        FCMMessageType.UserLocated, NotificationSender.TTL_LONG,
                        null,
                        new Pair<String, String>("latitude", latitude),
                        new Pair<String, String>("longitude", longitude),
                        new Pair<String, String>("isAutoNotification", "1"));

                Logs.show("Send to topic");
            }
        }
    }

}
