package com.ffinder.android;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;
import com.ffinder.android.absint.activities.IAppsIntroductionListener;
import com.ffinder.android.absint.activities.MyActivityAbstract;
import com.ffinder.android.enums.PreferenceType;
import com.ffinder.android.helpers.LocationUpdater;
import com.ffinder.android.models.MyModel;
import com.ffinder.android.services.GcmAliveHeartbeatService;
import com.ffinder.android.statics.Vars;
import com.ffinder.android.utils.*;

import java.util.List;

/**
 * Created by SiongLeng on 30/8/2016.
 */
public class ActivityLaunch extends MyActivityAbstract implements IAppsIntroductionListener {

    private Intent intent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        restartPermissionChecking();

        //start heart beat service singleton
        Intent intent = new Intent(this, GcmAliveHeartbeatService.class);
        startService(intent);
    }

    private void restartPermissionChecking(){
        if(isLocationPermissionGranted() && isLocationServicesEnabled()){
            onPermissionFinishChecking();
        }

    }

    private void onPermissionFinishChecking(){
        populatePendingAddUserKey();

        final MyModel myModel = new MyModel(this);

        if(Strings.isEmpty(myModel.getUserId())){
            this.intent = new Intent(this, ActivitySetup.class);
        }
        else{
            this.intent = new Intent(this, ActivityMain.class);

            //always update location when launch apps
            new LocationUpdater(ActivityLaunch.this, null, null);
        }

        readyStartActivity();
    }

    private void readyStartActivity(){
        String value = PreferenceUtils.get(this, PreferenceType.SeenAppsIntroduction);

        if(Strings.isEmpty(value)){
            showIntroduction();
        }
        else{
            goToNextActivitiy();
        }
    }

    private void showIntroduction(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        FragmentAppsIntroduction fragment = new FragmentAppsIntroduction();
        fragmentTransaction.add(R.id.layoutDefault, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onCompleteAppsIntroduction() {
        goToNextActivitiy();
    }

    private void goToNextActivitiy(){
        this.startActivity(intent);
        this.overridePendingTransition(0, 0);
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
                restartPermissionChecking();
            }
            else{
                Toast.makeText(this,
                        R.string.some_permission_denied_toast_msg, Toast.LENGTH_LONG)
                        .show();
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
            AndroidUtils.showDialogWithButtonText(this, "", getString(R.string.required_location_provider_msg),
                    getString(R.string.btn_go_to_location_settings_text), new RunnableArgs<DialogInterface>() {
                        @Override
                        public void run() {
                            this.getFirstArg().dismiss();
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    }, new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    });
            return false;
        }
    }

    //user is enter FFfinder through whatsapp msg
    private void populatePendingAddUserKey(){
        final Intent intent = getIntent();
        final String action = intent.getAction();

        if (Intent.ACTION_VIEW.equals(action)) {
            final List<String> segments = intent.getData().getPathSegments();
            if (segments.size() == 2) {
                Vars.pendingAddUserKey = segments.get(1);
            }
        }
    }




}
