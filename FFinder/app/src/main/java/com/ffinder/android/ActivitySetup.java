package com.ffinder.android;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.ffinder.android.absint.activities.MyActivityAbstract;
import com.ffinder.android.absint.databases.FirebaseListener;
import com.ffinder.android.enums.Status;
import com.ffinder.android.helpers.FirebaseDB;
import com.ffinder.android.models.FriendModel;
import com.ffinder.android.models.MyModel;
import com.ffinder.android.tasks.AdsIdTask;
import com.ffinder.android.utils.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.*;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by SiongLeng on 30/8/2016.
 */
public class ActivitySetup extends MyActivityAbstract {

    private GoogleApiClient googleApiClient;
    private TextView txtStatus;
    private String currentStatus;
    private boolean failed;
    private AnimationDrawable frameAnimation;
    private ImageView imgViewLoading;
    private Button btnRetry;
    private MyModel myModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setup);

        myModel = new MyModel(this);

        txtStatus = (TextView) findViewById(R.id.txtStatus);
        setCurrentStatus(currentStatus);

        btnRetry = (Button) findViewById(R.id.btnRetry);

        imgViewLoading = (ImageView)findViewById(R.id.imgViewLoading);
        imgViewLoading.setBackgroundResource(R.drawable.loading_animation);

        frameAnimation = (AnimationDrawable) imgViewLoading.getBackground();

        setFailed(failed);

        setListeners();

        //start the whole process by checking google service availability
        checkGoogleServiceAvailable();
    }

    private void checkGoogleServiceAvailable(){
        setCurrentStatus(getString(R.string.checking_google_service_availability));

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {

                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                })
                .build();

        googleApiClient.connect();

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final com.google.android.gms.common.api.Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        onFinishChecking();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    ActivitySetup.this, 7);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Toast.makeText(ActivitySetup.this, getString(R.string.no_google_service_error_msg),
                                Toast.LENGTH_LONG)
                                .show();

                        finish();   //quit FFfinder
                        break;
                }
            }
        });
    }

    private void onFinishChecking(){
        googleApiClient.disconnect();

        //finish google service availability checking, can start the setup user process now
        startProcess();
    }


    private void startProcess(){
        setFailed(false);
        setCurrentStatus(getString(R.string.retrieving_token));
        if(!DeviceInfo.isNetworkAvailable(this)){
            failed(FailedCode.NoConnection);
            return;
        }

        setCurrentStatus(getString(R.string.initializing_user));

        tryRecoverUserByGoogleAdsId(new RunnableArgs<String>() {
            @Override
            public void run() {
                if(!Strings.isEmpty(this.getFirstArg())){     //successfully recovered
                    String recoverUserId = this.getFirstArg();
                    myModel.setUserId(recoverUserId);
                    myModel.save();
                    myModel.loginFirebase(0, new RunnableArgs<Boolean>() {
                        @Override
                        public void run() {
                            if(this.getFirstArg()){
                                finishProcessAndStartActivity();
                            }
                            else{
                                failed(FailedCode.DBFailed);
                            }
                        }
                    });
                }
                else{
                    myModel.delete();
                    final String userId = FirebaseDB.getNewUserId();
                    myModel.setUserId(userId);
                    myModel.save();

                    myModel.loginFirebase(0, new RunnableArgs<Boolean>() {
                        @Override
                        public void run() {
                            if(this.getFirstArg()){
                                String refreshedToken = FirebaseInstanceId.getInstance().getToken();
                                FirebaseDB.saveNewUser(userId, refreshedToken, new FirebaseListener() {
                                    @Override
                                    public void onResult(Object result, Status status) {
                                        if(status == Status.Success){
                                            finishProcessAndStartActivity();
                                        }
                                        else{
                                            failed(FailedCode.DBFailed);
                                        }
                                    }
                                });
                            }
                            else{
                                failed(FailedCode.DBFailed);
                            }
                        }
                    });
                }
            }
        });
    }


    private void tryRecoverUserByGoogleAdsId(final RunnableArgs<String> onResult){
        Threadings.runInBackground(new Runnable() {
            @Override
            public void run() {
                AdsIdTask adsIdTask = new AdsIdTask(ActivitySetup.this, null);
                try {

                    String adsId = adsIdTask.execute().get(3, TimeUnit.SECONDS);
                    FirebaseDB.getUserIdByIdentifier(adsId, new FirebaseListener<String>(String.class) {
                        @Override
                        public void onResult(String recoverUserId, Status status) {
                            if(status == Status.Success && !Strings.isEmpty(recoverUserId)){
                                onResult.run(recoverUserId);
                            }
                            else{
                                onResult.run(null);
                            }
                        }
                    });


                } catch (InterruptedException e) {
                    e.printStackTrace();
                    onResult.run(null);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    onResult.run(null);
                } catch (TimeoutException e) {
                    e.printStackTrace();
                    onResult.run(null);
                }
            }
        });
    }

    private void failed(FailedCode failedCode){
        String msg = "";
        switch (failedCode){
            case NoConnection:
                msg = getString(R.string.no_connection_msg);
                break;
            case DBFailed:
                msg = getString(R.string.db_failed_msg);
                break;
        }

        setCurrentStatus(msg);
        setFailed(true);

    }

    private void finishProcessAndStartActivity(){
        FirebaseDB.checkUserHasAnyLink(myModel.getUserId(), new FirebaseListener<Boolean>() {
            @Override
            public void onResult(Boolean result, Status status) {
                if(result != null && !result){
                    FirebaseDB.addNewLink(myModel.getUserId(), myModel.getUserId(),
                            getString(R.string.address_myself),
                            getString(R.string.address_myself), new FirebaseListener() {
                        @Override
                        public void onResult(Object result, Status status) {
                            FriendModel myOwnModel = new FriendModel();
                            myOwnModel.setUserId(myModel.getUserId());
                            myOwnModel.setName(getString(R.string.address_myself));
                            myOwnModel.save(ActivitySetup.this);
                            myModel.addFriendModel(myOwnModel);
                            myModel.commitFriendUserIds();

                            setCurrentStatus(getString(R.string.initialization_done));
                            Intent k = new Intent(ActivitySetup.this, ActivityMain.class);
                            k.putExtra("firstRun", "1");
                            startActivity(k);
                        }
                    });
                }
                else{
                    setCurrentStatus(getString(R.string.initialization_done));
                    Intent k = new Intent(ActivitySetup.this, ActivityMain.class);
                    startActivity(k);
                }
            }
        });


    }

    public void setListeners(){
        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startProcess();
            }
        });
    }

    private enum FailedCode{
        NoConnection, DBFailed
    }

    public void setCurrentStatus(final String currentStatus) {
        this.currentStatus = currentStatus;
        if(txtStatus != null && currentStatus != null){
            System.out.println(this.currentStatus);
            Threadings.postRunnable(this, new Runnable() {
                @Override
                public void run() {
                    txtStatus.setText(currentStatus);
                }
            });
        }
    }

    public void setFailed(final boolean failed) {
        this.failed = failed;

        if(imgViewLoading != null){
            Threadings.postRunnable(this, new Runnable() {
                @Override
                public void run() {
                    if(!failed){
                        frameAnimation.run();
                    }
                    else{
                        frameAnimation.stop();
                    }

                    btnRetry.setVisibility(failed ? View.VISIBLE : View.GONE);
                }
            });
        }

    }




}
