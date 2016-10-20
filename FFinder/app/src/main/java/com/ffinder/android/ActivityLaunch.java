package com.ffinder.android;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.ffinder.android.absint.activities.IAppsIntroductionListener;
import com.ffinder.android.absint.activities.MyActivityAbstract;
import com.ffinder.android.enums.AnimateType;
import com.ffinder.android.enums.PreferenceType;
import com.ffinder.android.helpers.AnimateBuilder;
import com.ffinder.android.helpers.LocationUpdater;
import com.ffinder.android.models.MyModel;
import com.ffinder.android.services.GcmAliveHeartbeatService;
import com.ffinder.android.statics.Vars;
import com.ffinder.android.utils.*;

import java.util.List;

/**
 * Created by SiongLeng on 30/8/2016.
 */
public class ActivityLaunch extends MyActivityAbstract {

    private Intent intent;
    private ImageView imgViewIcon, imgViewBg, imgViewNextIcon;
    private RelativeLayout layoutDefault, layoutWelcome, layoutIntroduction, layoutNext;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        imgViewIcon = (ImageView) findViewById(R.id.imgViewIcon);
        imgViewBg = (ImageView) findViewById(R.id.imgViewBg);
        imgViewNextIcon = (ImageView) findViewById(R.id.imgViewNextIcon);
        layoutDefault = (RelativeLayout) findViewById(R.id.layoutDefault);
        layoutWelcome = (RelativeLayout) findViewById(R.id.layoutWelcome);
        layoutIntroduction = (RelativeLayout) findViewById(R.id.layoutIntroduction);
        layoutNext = (RelativeLayout) findViewById(R.id.layoutNext);

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
        imgViewBg.setVisibility(View.VISIBLE);
        imgViewIcon.setVisibility(View.VISIBLE);

        final ViewTreeObserver vto = imgViewIcon.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                imgViewIcon.getViewTreeObserver().removeOnPreDrawListener(this);
                float heightInDp = AndroidUtils.pxToDp(ActivityLaunch.this, imgViewIcon.getHeight());

                float beforeDp = AndroidUtils.getScreenDpHeight(ActivityLaunch.this) / 2 - heightInDp / 2;
                float finalDp = 110;
                final float moveByDp = beforeDp - finalDp;

                AnimateBuilder.build(ActivityLaunch.this, imgViewIcon).setAnimateType(AnimateType.moveByY)
                        .setDurationMs(0).setValue(moveByDp).setFinishCallback(new Runnable() {
                    @Override
                    public void run() {
                        AnimateBuilder.build(ActivityLaunch.this, imgViewIcon).setAnimateType(AnimateType.moveByY)
                                .setDurationMs(1000).setValue(-moveByDp).setFinishCallback(new Runnable() {
                            @Override
                            public void run() {
                                AnimateBuilder.build(ActivityLaunch.this, layoutWelcome)
                                        .setAnimateType(AnimateType.alpha).setDurationMs(700)
                                        .setValue(1).setFinishCallback(new Runnable() {
                                    @Override
                                    public void run() {
                                        AnimateBuilder.build(ActivityLaunch.this, layoutIntroduction)
                                                .setAnimateType(AnimateType.alpha)
                                                .setValue(1).setDurationMs(700)
                                                .setFinishCallback(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        AnimateBuilder.build(ActivityLaunch.this, layoutNext)
                                                                .setAnimateType(AnimateType.alpha)
                                                                .setValue(1).setDurationMs(700)
                                                                .setFinishCallback(new Runnable() {
                                                                    @Override
                                                                    public void run() {

                                                                    }
                                                                }).start();

                                                        AnimateBuilder.build(ActivityLaunch.this, imgViewNextIcon)
                                                                .setAnimateType(AnimateType.moveByX)
                                                                .setValue(-3).setDurationMs(400)
                                                                .setRepeat(true)
                                                                .start();
                                                    }
                                                }).start();
                                    }
                                }).start();
                            }
                        }).start();
                    }
                }).start();


                return true;
            }
        });

        setListeners();
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

    private void setListeners(){
        layoutNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutNext.setOnClickListener(null);
                PreferenceUtils.put(ActivityLaunch.this, PreferenceType.SeenAppsIntroduction, "1");

                AnimateBuilder.build(ActivityLaunch.this, layoutIntroduction)
                        .setAnimateType(AnimateType.alpha).setDurationMs(600)
                        .setValue(0).setFinishCallback(new Runnable() {
                    @Override
                    public void run() {
                        goToNextActivitiy();
                    }
                }).start();

                AnimateBuilder.build(ActivityLaunch.this, layoutNext)
                        .setAnimateType(AnimateType.alpha).setDurationMs(600)
                        .setValue(0).start();

            }
        });
    }



}
