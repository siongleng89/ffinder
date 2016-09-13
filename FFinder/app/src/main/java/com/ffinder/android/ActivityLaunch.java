package com.ffinder.android;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.RelativeLayout;
import com.ffinder.android.absint.activities.IAppsIntroductionListener;
import com.ffinder.android.enums.PreferenceType;
import com.ffinder.android.models.MyModel;
import com.ffinder.android.statics.Vars;
import com.ffinder.android.utils.PreferenceUtils;
import com.ffinder.android.utils.Strings;
import com.ffinder.android.utils.Threadings;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.*;
import com.onesignal.OneSignal;

import java.util.List;

/**
 * Created by SiongLeng on 30/8/2016.
 */
public class ActivityLaunch extends AppCompatActivity implements IAppsIntroductionListener {

    private ActivityLaunch _this;
    private GoogleApiClient googleApiClient;
    private boolean quitOnResume;
    private RelativeLayout layoutDefault;
    private Intent intent;

    public ActivityLaunch() {
        _this = this;
        Threadings.setMainTreadId();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        layoutDefault = (RelativeLayout) findViewById(R.id.layoutDefault);

        if(isLocationPermissionGranted()){
            checkGoogleServiceAvailable();
        }
    }

    private void checkGoogleServiceAvailable(){
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
                final Status status = result.getStatus();
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
                                    _this, 7);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        quit(QuitCode.NoGoogleService);
                        break;
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 7:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        onFinishChecking();
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        quit(QuitCode.NoGoogleService);
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    private void onFinishChecking(){
        final Intent intent = getIntent();
        final String action = intent.getAction();

        if (Intent.ACTION_VIEW.equals(action)) {
            final List<String> segments = intent.getData().getPathSegments();
            if (segments.size() == 1) {
                Vars.pendingAddUserKey = segments.get(0);
            }
        }

        googleApiClient.disconnect();

        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
            @Override
            public void idsAvailable(String userId, String registrationId) {
                Bundle b = new Bundle();
                b.putString("userId", userId);

                MyModel myModel = new MyModel(_this);

                if(Strings.isEmpty(myModel.getUserId()) || !userId.equals(myModel.getUserId())){
                    Intent k = new Intent(_this, ActivitySetup.class);
                    k.putExtras(b);
                    readyStartActivity(k);
                }
                else{
                    Intent k = new Intent(_this, ActivityMain.class);
                    k.putExtras(b);
                    readyStartActivity(k);
                }
            }
        });
    }

    private void readyStartActivity(Intent intent){
        String value = PreferenceUtils.get(this, PreferenceType.SeenAppsIntroduction);
        if(Strings.isEmpty(value)){
            this.intent = intent;
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            FragmentAppsIntroduction fragment = new FragmentAppsIntroduction();
            fragmentTransaction.add(R.id.layoutDefault, fragment);
            fragmentTransaction.commit();
        }
        else{
            startActivity(intent);
        }
    }

    @Override
    public void onCompleteAppsIntroduction() {
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1){
            int granted = 0;
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];

                if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION ) ||
                        permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION) ||
                        permission.equals(Manifest.permission.GET_ACCOUNTS)) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        granted++;
                    } else {
                        granted++;
                    }
                    break;
                }
            }
            if(granted >= 3){
                checkGoogleServiceAvailable();
            }
            else{
                quit(QuitCode.NoPermission);
            }

        }
    }

    public  boolean isLocationPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS)
                            == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.GET_ACCOUNTS}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }

    private void quit(QuitCode quitCode){
        String msg = "";

        switch (quitCode){
            case NoGoogleService:
                msg = getString(R.string.no_google_service_error_msg);
                break;
            case NoPermission:
                msg = getString(R.string.no_permission_error_msg);
                break;
        }

        AlertDialog.Builder dlg = new AlertDialog.Builder(this);
        dlg.setTitle(getString(R.string.error));
        dlg.setMessage(msg);
        dlg.setCancelable(false);
        dlg.setPositiveButton(getString(R.string.quit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                quitOnResume = true;
                dialog.dismiss();
            }
        });
        dlg.create();
        dlg.show();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(quitOnResume){
            finish();
        }
    }



    private enum QuitCode{
        NoPermission, NoGoogleService
    }

}
