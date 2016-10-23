package com.ffinder.android.helpers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.util.Pair;
import com.ffinder.android.enums.BroadcastEvent;

/**
 * Created by sionglengho on 7/10/16.
 */
public class BroadcasterHelper {

    public static void broadcast(Context context, BroadcastEvent broadcastEvent,
                                 Pair<String, String>... extras){

        Intent i = new Intent(broadcastEvent.name());
        if(extras != null){
            for(Pair<String, String> extra : extras){
                i.putExtra(extra.first, extra.second);
            }
        }
        context.sendBroadcast(i);
    }

    public static BroadcastReceiver register(Activity activity, BroadcastEvent broadcastEvent,
                                final RunnableArgs<Intent> onReceived){
        IntentFilter filter = new IntentFilter();
        filter.addAction(broadcastEvent.name());

        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onReceived.run(intent);
            }
        };
        activity.registerReceiver(broadcastReceiver, filter);
        return broadcastReceiver;
    }

    //registered before, but is required to register again as it is unregister onStop
    public static BroadcastReceiver register(Activity activity,
                                             BroadcastEvent broadcastEvent,
                                             BroadcastReceiver broadcastReceiver){
        IntentFilter filter = new IntentFilter();
        filter.addAction(broadcastEvent.name());
        activity.registerReceiver(broadcastReceiver, filter);
        return broadcastReceiver;
    }

    public static void unregister(Activity activity, BroadcastReceiver broadcastReceiver){
        activity.unregisterReceiver(broadcastReceiver);
    }


}
