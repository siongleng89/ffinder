package com.ffinder.android;

import android.app.Application;
import com.ffinder.android.helpers.Analytics;
import com.ffinder.android.helpers.VipAndProductsHelpers;
import com.ffinder.android.models.MyModel;
import com.ffinder.android.helpers.Logs;
import com.ffinder.android.helpers.Threadings;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by SiongLeng on 4/9/2016.
 */
public class MyApplication extends Application {

    private Tracker mTracker;
    private VipAndProductsHelpers vipAndProductsHelpers;
    private MyModel myModel;

    @Override
    public void onCreate() {
        super.onCreate();

        Logs.registerEventCatchingIFDebug();
        Threadings.setMainTreadId();
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

    synchronized public MyModel getMyModel() {
        if (myModel == null) {
            myModel = new MyModel(this);
            myModel.load();
            myModel.loadAllFriendModels();
            myModel.loginFirebase(0, null);
        }
        return myModel;
    }





















}
