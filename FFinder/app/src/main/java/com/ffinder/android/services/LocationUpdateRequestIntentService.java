package com.ffinder.android.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.util.Pair;
import com.ffinder.android.absint.receivers.AutoSearchWakefulBroadcastReceiver;
import com.ffinder.android.absint.receivers.LocationUpdateRequestWakefulBroadcastReceiver;
import com.ffinder.android.helpers.LocationUpdater;
import com.ffinder.android.helpers.Logs;
import com.ffinder.android.helpers.RunnableArgs;
import com.ffinder.android.helpers.Threadings;
import com.ffinder.android.statics.Constants;
import com.ffinder.android.tasks.LocationUpdateTask;
import com.ffinder.android.tasks.RequestLocationTaskFrag;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by sionglengho on 20/11/16.
 */
public class LocationUpdateRequestIntentService extends IntentService {
    public LocationUpdateRequestIntentService() {
        super("LocationUpdateRequestIntentService");
    }



    @Override
    protected void onHandleIntent(final Intent intent) {

        Logs.show("Receive update location request");

        String senderId = null;
        String senderToken = null;
        String fromPlatform = null;

        if(intent != null && intent.hasExtra("senderId")){
            senderId = intent.getStringExtra("senderId");
        }
        if(intent != null && intent.hasExtra("senderToken")){
            senderToken = intent.getStringExtra("senderToken");
        }
        if(intent != null && intent.hasExtra("fromPlatform")){
            fromPlatform = intent.getStringExtra("fromPlatform");
        }

        final String finalSenderToken = senderToken;
        final String finalFromPlatform = fromPlatform;
        final String finalSenderId = senderId;
        Threadings.runInBackground(new Runnable() {
            @Override
            public void run() {
                LocationUpdateTask locationUpdateTask = new LocationUpdateTask(
                        LocationUpdateRequestIntentService.this.getBaseContext(),
                        finalSenderId,
                        finalSenderToken,
                        finalFromPlatform);

                boolean succeed = false;

                try {
                    if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
                        locationUpdateTask
                                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                                .get(Constants.RequestLocationTimeoutSecs, TimeUnit.SECONDS);

                    }
                    else {
                        locationUpdateTask.execute()
                                .get(Constants.RequestLocationTimeoutSecs, TimeUnit.SECONDS);
                    }

                    succeed = true;


                } catch (InterruptedException e) {
                } catch (ExecutionException e) {
                } catch (TimeoutException e) {
                }
                finally {
                    locationUpdateTask.terminate();
                }


                if(!succeed){
                    Intent autoSearchIntent = new Intent();
                    autoSearchIntent.setAction("com.ffinder.android.AUTO_SEARCH");
                    sendBroadcast(autoSearchIntent);
                }

                LocationUpdateRequestWakefulBroadcastReceiver.completeWakefulIntent(intent);

            }
        });
    }

}
