package com.ffinder.android;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;
import com.ffinder.android.absint.activities.MyActivityAbstract;
import com.ffinder.android.enums.OverlayType;
import com.ffinder.android.enums.PreferenceType;
import com.ffinder.android.helpers.*;
import com.ffinder.android.models.MyModel;
import com.ffinder.android.services.GcmAliveHeartbeatService;

/**
 * Created by SiongLeng on 30/8/2016.
 */
public class ActivityLaunch extends MyActivityAbstract {

    private Intent intent;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        //start heart beat service singleton
        Intent intent = new Intent(this, GcmAliveHeartbeatService.class);
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        restartPermissionChecking();
    }

    private void restartPermissionChecking(){
        if(isLocationPermissionGranted() && isLocationServicesEnabled()){
            onPermissionFinishChecking();
        }
    }

    private void onPermissionFinishChecking(){
        final MyModel myModel = getMyModel();

        if(Strings.isEmpty(myModel.getUserId())){
            this.intent = new Intent(this, ActivitySetup.class);
        }
        else{
            this.intent = new Intent(this, ActivityMain.class);

            //always update location when launch apps
            Intent intent2 = new Intent();
            intent2.setAction("com.ffinder.android.GET_LOCATION");
            sendBroadcast(intent2);
        }

        readyStartActivity();
    }

    private void readyStartActivity(){
        String value = PreferenceUtils.get(this, PreferenceType.SeenAppsIntroduction);

        if(Strings.isEmpty(value)){
            //not yet seen introduction, change to intro page
            this.intent = new Intent(this, ActivityIntro.class);
            goToNextActivitiy();
        }
        else{
            goToNextActivitiy();
        }
    }


    private void goToNextActivitiy(){
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
        this.overridePendingTransition(0, 0);
        finish();
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
                        permission.equals(Manifest.permission.GET_ACCOUNTS) ||
                        permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        granted++;
                    }
                }
            }
            if(granted >= 4){
                restartPermissionChecking();
            }
            else{
                Toast.makeText(this,
                        R.string.some_permission_denied_toast_msg, Toast.LENGTH_LONG)
                        .show();
                finish();
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
                            == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.GET_ACCOUNTS,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }

    public boolean isLocationServicesEnabled(){
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(network_enabled || gps_enabled) {
            return true;
        }
        else{

            OverlayBuilder.build(ActivityLaunch.this)
                    .setOverlayType(OverlayType.CustomButtons)
                    .setContent(getString(R.string.required_location_provider_msg))
                    .setBtnTexts(getString(R.string.btn_go_to_location_settings_text),
                            getString(R.string.quit))
                    .setRunnables(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    }, new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    })
                    .show();

            return false;
        }
    }






}
