package com.ffinder.android;

import android.app.Application;
import com.ffinder.android.helpers.Analytics;
import com.ffinder.android.helpers.FirebaseOnlineTracker;
import com.ffinder.android.helpers.LocaleHelper;
import com.ffinder.android.helpers.VipAndProductsHelpers;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Created by SiongLeng on 4/9/2016.
 */
public class MyApplication extends Application {

    private Tracker mTracker;
    private VipAndProductsHelpers vipAndProductsHelpers;

    @Override
    public void onCreate() {
        super.onCreate();
        LocaleHelper.onCreate(this);
    }

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if(BuildConfig.DEBUG_MODE) return null;

        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
            mTracker.enableExceptionReporting(true);
            Analytics.setTracker(mTracker);
        }
        return mTracker;
    }


    synchronized public VipAndProductsHelpers getVipAndProductsHelpers() {
        if (vipAndProductsHelpers == null) {
            vipAndProductsHelpers = VipAndProductsHelpers.getInstance(this);
        }
        return vipAndProductsHelpers;
    }























}
