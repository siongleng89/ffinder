package com.ffinder.android.tasks;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.widget.Toast;
import com.ffinder.android.R;
import com.ffinder.android.absint.helpers.IAdsMediationListener;
import com.ffinder.android.enums.AnalyticEvent;
import com.ffinder.android.helpers.AdsMediation;
import com.ffinder.android.helpers.Analytics;
import com.ffinder.android.helpers.Threadings;

/**
 * Created by SiongLeng on 15/9/2016.
 */
public class AdsFrag extends Fragment {

    private AdsMediation adsMediation;
    private Activity activity;
    private boolean preloaded;

    public static AdsFrag newInstance(Activity activity) {
        AdsFrag f = new AdsFrag();
        f.init(activity);
        return f;
    }



    public void init(Activity activity){
        this.activity = activity;
        adsMediation = new AdsMediation(activity);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void preload(){
        if(preloaded){
            return;
        }

        preloaded = true;
        adsMediation.preload();
    }

    public void showAds(final Runnable onFinishAds){
        adsMediation.setAdsMediationListener(new IAdsMediationListener() {
            @Override
            public void onResult(boolean success) {
                if(!success){
                    Threadings.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), R.string.no_ads_available_toast_msg, Toast.LENGTH_LONG).show();
                            Analytics.logEvent(AnalyticEvent.No_Ads_Available);
                        }
                    });
                }
                onFinishAds.run();
            }
        });
        adsMediation.showRewardedVideo(activity);
    }


    public void setActivity(Activity activity) {
        this.activity = activity;
        adsMediation.setActivity(activity);
    }
}
