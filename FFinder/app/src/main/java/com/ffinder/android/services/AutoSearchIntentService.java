package com.ffinder.android.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import com.ffinder.android.absint.receivers.AutoSearchWakefulBroadcastReceiver;
import com.ffinder.android.helpers.Logs;
import com.ffinder.android.helpers.Threadings;
import com.ffinder.android.statics.Constants;
import com.ffinder.android.tasks.LocationUpdateTask;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by sionglengho on 21/11/16.
 */
//this service is triggered when user location cannot get in time
public class AutoSearchIntentService extends IntentService {

    public AutoSearchIntentService() {
        super("AutoSearchIntentService");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        int count = 0;
        if(intent != null && intent.hasExtra("count")){
            count = intent.getIntExtra("count", 0);
        }

        final long nextAutoSearchMs;

        if(count == 0){
            nextAutoSearchMs = 5 * 60 * 1000;      //5mins
        }
        else if(count == 1){
            nextAutoSearchMs = 10 * 60 * 1000;      //10mins
        }
        else if(count == 2){
            nextAutoSearchMs = 30 * 60 * 1000;      //30mins
        }
        else if(count == 3){
            nextAutoSearchMs = 60 * 60 * 1000;      //1 hour
        }
        else if(count == 4){
            nextAutoSearchMs = 2 * 60 * 60 * 1000;      //2 hour
        }
        else if(count == 5){
            nextAutoSearchMs = 4 * 60 * 60 * 1000;      //4 hour
        }
        else{
            nextAutoSearchMs = 8 * 60 * 60 * 1000;      //8 hour
        }

        Logs.show("Auto Searching Triggered");

        final int finalCount = count;
        Threadings.runInBackground(new Runnable() {
            @Override
            public void run() {
                LocationUpdateTask locationUpdateTask = new LocationUpdateTask(
                        AutoSearchIntentService.this.getBaseContext(), null);

                boolean succeed = false;

                try {
                    if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
                        locationUpdateTask
                                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                                .get(90, TimeUnit.SECONDS);

                    }
                    else {
                        locationUpdateTask.execute()
                                .get(90, TimeUnit.SECONDS);
                    }

                    succeed = true;

                } catch (InterruptedException e) {
                } catch (ExecutionException e) {
                } catch (TimeoutException e) {
                }
                finally {
                    locationUpdateTask.terminate();
                }

                //if not succeed, set alarm again, if succceed,
                // alarm is removed at locationUpdateTask
                if(!succeed){
                    AlarmManager alarmMgr =
                            (AlarmManager) getBaseContext().getSystemService(Context.ALARM_SERVICE);
                    Intent searchIntent = new Intent(getBaseContext(),
                            AutoSearchWakefulBroadcastReceiver.class);
                    searchIntent.putExtra("count", String.valueOf(finalCount + 1));

                    PendingIntent alarmIntent = PendingIntent.getBroadcast(getBaseContext(), 0,
                            searchIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);

                    alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            SystemClock.elapsedRealtime() + nextAutoSearchMs, alarmIntent);

                    Logs.show("Auto Searching Failed, next searched after " + nextAutoSearchMs + "ms");
                }


                AutoSearchWakefulBroadcastReceiver.completeWakefulIntent(intent);

            }
        });






    }

}
