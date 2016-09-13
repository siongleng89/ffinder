package com.ffinder.android.absint.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.ffinder.android.ActivityMain;
import com.ffinder.android.enums.PreferenceType;
import com.ffinder.android.utils.PreferenceUtils;

/**
 * Created by SiongLeng on 6/9/2016.
 */
public class NotificationClickBroadcastReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        PreferenceUtils.delete(context, PreferenceType.AutoNotifiedReceivedIds);
        Intent myIntent = new Intent(context, ActivityMain.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(myIntent);
    }

}