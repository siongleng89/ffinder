package com.ffinder.android.absint.activities;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import com.ffinder.android.enums.BroadcastEvent;
import com.ffinder.android.helpers.Analytics;
import com.ffinder.android.helpers.BroadcasterHelper;
import com.ffinder.android.utils.RunnableArgs;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by SiongLeng on 16/9/2016.
 */
public class MyActivityAbstract extends AppCompatActivity {

    protected boolean paused;
    private HashMap<BroadcastEvent, BroadcastReceiver> broadcastReceiverHashMap;

    public MyActivityAbstract() {
        broadcastReceiverHashMap = new HashMap();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Analytics.logToScreen(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        paused = false;
    }

    public boolean isPaused() {
        return paused;
    }



    @Override
    protected void onStart() {
        super.onStart();

        for(BroadcastEvent broadcastEvent : broadcastReceiverHashMap.keySet()){
            BroadcasterHelper.register(this, broadcastEvent, broadcastReceiverHashMap.get(broadcastEvent));
        }

    }

    @Override
    protected void onStop() {
        super.onStop();

        for(BroadcastReceiver broadcastReceiver : broadcastReceiverHashMap.values()){
            BroadcasterHelper.unregister(this, broadcastReceiver);
        }
    }


    protected void registerBroadcastReceiver(BroadcastEvent broadcastEvent, RunnableArgs<Intent> onResult){
        if(broadcastReceiverHashMap.containsKey(broadcastEvent)){   //prevent duplicate registrations
            return;
        }
        else{
            BroadcastReceiver broadcastReceiver = BroadcasterHelper.register(this, broadcastEvent, onResult);
            broadcastReceiverHashMap.put(broadcastEvent, broadcastReceiver);
        }
    }

    protected void removeBroadcastReceiver(BroadcastEvent broadcastEvent){
        if(broadcastReceiverHashMap.containsKey(broadcastEvent)){
            BroadcasterHelper.unregister(this, broadcastReceiverHashMap.get(broadcastEvent));
            broadcastReceiverHashMap.remove(broadcastEvent);
        }
    }
}
