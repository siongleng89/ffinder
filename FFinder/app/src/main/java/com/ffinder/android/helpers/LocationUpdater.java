package com.ffinder.android.helpers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Pair;
import com.ffinder.android.absint.databases.FirebaseListener;
import com.ffinder.android.enums.FCMMessageType;
import com.ffinder.android.enums.Status;
import com.ffinder.android.models.AutoNotificationModel;
import com.ffinder.android.models.LocationModel;
import com.ffinder.android.models.MyModel;
import com.ffinder.android.utils.Logs;
import com.ffinder.android.utils.RunnableArgs;
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
public class LocationUpdater implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleApiClient googleApiClient;
    private Context context;
    private MyModel myModel;
    private String fromUserId;
    private String fromUserToken;

    public LocationUpdater(Context context, String fromUserId, String fromUserToken) {
        this.context = context;
        this.fromUserId = fromUserId;
        this.fromUserToken = fromUserToken;
        this.myModel = new MyModel(context);

        run();
    }

    private void run(){

        //if from usertoken not empty, means it is through normal search, reply alive msg
        if(!Strings.isEmpty(fromUserToken)) {
            replyAliveMsg(myModel.getUserId(), fromUserToken);
        }

        //start retrieve location
        googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(!checkPermission()){
            return;
        }

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(500);
        mLocationRequest.setFastestInterval(50);
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

    private void replyAliveMsg(String myUserId, String toUserToken){
        NotificationSender.sendWithToken(myUserId, toUserToken,
                FCMMessageType.IsAliveMsg, NotificationSender.TTL_INSTANT);
    }

    private void locationSuccessfullyRetrieved(final String latitude, final String longitude){
        final LocationModel locationModel = new LocationModel();
        locationModel.setLatitude(latitude);
        locationModel.setLongitude(longitude);

        Logs.show("Updating my location to: " + latitude + ", " + longitude);

        //immediately reply fcm with current location, then save to firebase later,
        //to avoid firebase login delay
        if(!Strings.isEmpty(fromUserToken)){
            NotificationSender.sendWithToken(myModel.getUserId(), fromUserToken,
                    FCMMessageType.UserLocated, NotificationSender.TTL_INSTANT,
                    new Pair<String, String>("latitude", latitude),
                    new Pair<String, String>("longitude", longitude),
                    //is auto notificaiton decide whether show push notification on user tray
                    new Pair<String, String>("isAutoNotification", "0"));

        }

        //now we can slowly update firebase
        myModel.loginFirebase(0, new RunnableArgs<Boolean>() {
            @Override
            public void run() {
                if(this.getFirstArg()){     //login success
                    FirebaseDB.updateLocation(myModel.getUserId(), locationModel, new FirebaseListener() {
                        @Override
                        public void onResult(Object result, Status status) {
                            if(status == Status.Success){
                                FirebaseDB.getAutoNotifyList(myModel.getUserId(), new FirebaseListener<ArrayList<AutoNotificationModel>>(AutoNotificationModel.class) {
                                    @Override
                                    public void onResult(ArrayList<AutoNotificationModel> result, Status status) {
                                        if(status == Status.Success && result != null){
                                            for(AutoNotificationModel autoNotificationModel : result){
                                                if(autoNotificationModel.getWaitingUserId().equals(fromUserId)){
                                                    //already sent at above, no need resend again
                                                    continue;
                                                }

                                                NotificationSender.sendWithUserId(myModel.getUserId(),
                                                        autoNotificationModel.getWaitingUserId(),
                                                        FCMMessageType.UserLocated, NotificationSender.TTL_LONG,
                                                        new Pair<String, String>("latitude", latitude),
                                                        new Pair<String, String>("longitude", longitude),
                                                        new Pair<String, String>("isAutoNotification", "1"));
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
        });



    }

}
