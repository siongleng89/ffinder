package com.ffinder.android.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import com.ffinder.android.ActivityDummy;
import com.ffinder.android.utils.Logs;
import com.google.android.gms.gcm.GcmReceiver;

/**
 * Created by SiongLeng on 18/9/2016.
 */
public class TestService extends Service {


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        Intent myIntent = new Intent();
        myIntent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
        myIntent.setClassName("com.ffinder.android", "com.ffinder.android.ActivityDummy");
        startActivity(myIntent);

    }
}
