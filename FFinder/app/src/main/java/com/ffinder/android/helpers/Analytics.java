package com.ffinder.android.helpers;

import android.app.Activity;
import com.ffinder.android.MyApplication;
import com.google.android.gms.analytics.HitBuilders;

/**
 * Created by SiongLeng on 13/9/2016.
 */
public class Analytics {

    public static void logToScreen(Activity activity){
        MyApplication application = (MyApplication) activity.getApplication();
        application.getDefaultTracker().setScreenName("Launching Activity");
        application.getDefaultTracker().send(new HitBuilders.ScreenViewBuilder().build());
    }

    public static void logEvent(Activity activity){

    }


}
