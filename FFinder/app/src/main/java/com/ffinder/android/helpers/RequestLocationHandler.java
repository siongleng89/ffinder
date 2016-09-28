package com.ffinder.android.helpers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import com.ffinder.android.absint.databases.FirebaseListener;
import com.ffinder.android.enums.AnalyticEvent;
import com.ffinder.android.enums.FCMMessageType;
import com.ffinder.android.enums.Status;
import com.ffinder.android.models.AutoNotificationModel;
import com.ffinder.android.models.LocationModel;
import com.ffinder.android.models.MyModel;
import com.ffinder.android.utils.Logs;
import com.ffinder.android.utils.Strings;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

/**
 * Created by SiongLeng on 29/8/2016.
 */
public class RequestLocationHandler implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleApiClient googleApiClient;
    private Context context;
    private MyModel myModel;
    private String fromUserId;

    public RequestLocationHandler(Context context, String fromUserId, MyModel myModel) {
        this.context = context;
        this.myModel = myModel;
        this.fromUserId = fromUserId;
    }

    public void run(){
        if(!Strings.isEmpty(myModel.getUserId())){

            if(!Strings.isEmpty(fromUserId)) respondToPing(myModel.getUserId(), fromUserId);

            googleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            googleApiClient.connect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(!checkPermission()){
            return;
        }

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(100);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, mLocationRequest, this);


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Logs.show("Fused locator connection failed: " + connectionResult);
    }


    @Override
    public void onLocationChanged(Location location) {
        if(!checkPermission()){
            return;
        }
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (mLastLocation != null) {
            String latitude = String.valueOf(mLastLocation.getLatitude());
            String longitude = String.valueOf(mLastLocation.getLongitude());
            locationSuccessfullyRetrieved(latitude, longitude);

            googleApiClient.disconnect();
        }
    }

    private boolean checkPermission(){
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    private void respondToPing(String myUserId, String fromUserId){
        FirebaseDB.respondToPing(myUserId, fromUserId, null);
    }

    private void locationSuccessfullyRetrieved(String latitude, String longitude){
        LocationModel locationModel = new LocationModel();
        locationModel.setLatitude(latitude);
        locationModel.setLongitude(longitude);

        Logs.show("Updating my location to: " + latitude + ", " + longitude);

        FirebaseDB.updateLocation(myModel.getUserId(), locationModel, new FirebaseListener() {
            @Override
            public void onResult(Object result, Status status) {
                if(status == Status.Success){
                    FirebaseDB.getAutoNotifyList(myModel.getUserId(), new FirebaseListener<ArrayList<AutoNotificationModel>>(AutoNotificationModel.class) {
                        @Override
                        public void onResult(ArrayList<AutoNotificationModel> result, Status status) {
                            if(status == Status.Success && result != null){
                                for(AutoNotificationModel autoNotificationModel : result){
                                    NotificationSender.send(myModel.getUserId(),
                                            autoNotificationModel.getWaitingUserId(), FCMMessageType.UserLocated, NotificationSender.TTL_LONG);
                                    Analytics.logEvent(AnalyticEvent.Auto_Notification_Sent);
                                }
                                FirebaseDB.clearAllAutoNotification(myModel.getUserId());
                            }
                        }
                    });
                }
            }
        });


    }

}
