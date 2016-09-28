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
import android.support.v4.content.WakefulBroadcastReceiver;
import com.ffinder.android.absint.receivers.GcmAliveHeartbeatBroadcastReceiver;
import com.ffinder.android.enums.FCMMessageType;
import com.ffinder.android.helpers.NotificationConsumer;
import com.ffinder.android.statics.Constants;
import com.ffinder.android.utils.Logs;
import com.ffinder.android.utils.Strings;
import com.ffinder.android.utils.Threadings;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by SiongLeng on 18/9/2016.
 */
public class GcmAliveHeartbeatService extends Service {

    private GcmAliveHeartbeatBroadcastReceiver gcmAliveHeartbeatBroadcastReceiver;

    @Override
    public void onCreate() {
        System.out.println("onCreate Triggered!");
        super.onCreate();

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.google.android.c2dm.intent.RECEIVE");

        gcmAliveHeartbeatBroadcastReceiver = new GcmAliveHeartbeatBroadcastReceiver();
        registerReceiver(gcmAliveHeartbeatBroadcastReceiver, filter);

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
        if(gcmAliveHeartbeatBroadcastReceiver != null)
        unregisterReceiver(gcmAliveHeartbeatBroadcastReceiver);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Logs.show("onTaskRemoved Triggered!");
        Intent restartServiceIntent = new Intent(getApplicationContext(),
                this.getClass());
        restartServiceIntent.setPackage(getPackageName());

        PendingIntent restartServicePendingIntent = PendingIntent.getService(
                getApplicationContext(), 1, restartServiceIntent,
                PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext()
                .getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartServicePendingIntent);
        super.onTaskRemoved(rootIntent);
    }
}
