package com.ffinder.android.absint.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.ffinder.android.enums.PreferenceType;
import com.ffinder.android.helpers.PreferenceUtils;
import com.ffinder.android.helpers.Strings;

/**
 * Created by sionglengho on 29/11/16.
 */
public class ReferrerBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent != null && intent.getExtras() != null){
            Bundle extras = intent.getExtras();
            String referrerString = extras.getString("referrer");

            if(referrerString != null && Strings.isNumeric(referrerString.replace("-", ""))){
                PreferenceUtils.put(context, PreferenceType.ReferrerKey, referrerString);
                System.out.println("REFERRER IS" + referrerString);

            }
        }

    }
}
