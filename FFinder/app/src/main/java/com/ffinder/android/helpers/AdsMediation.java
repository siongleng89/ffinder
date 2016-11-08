package com.ffinder.android.helpers;


import android.app.Activity;
import com.aerserv.sdk.*;
import com.ffinder.android.absint.helpers.IAdsMediationListener;

import java.util.List;

/**
 * Created by SiongLeng on 22/8/2016.
 */
public class AdsMediation {

    private AdsMediation _this;
    private Activity activity;
    private boolean hasAds;
    private AerServInterstitial interstitial;
    private IAdsMediationListener adsMediationListener;

    public AdsMediation(Activity activity) {
        this.activity = activity;
        _this = this;

        AerServSdk.init(activity, "1001717");
    }

    public boolean onBackPressed() {
        // If ad is on the screen - close it
        return false;
    }

    public boolean hasRewardVideo(){
        return hasAds;
    }

    public void showRewardedVideo(final Activity activity, final boolean showAds, final RunnableArgs<Boolean> runnable) {
        AerServEventListener listener = new AerServEventListener() {
            @Override
            public void onAerServEvent(AerServEvent event, List<Object> args) {
                Logs.show(event.name());
                switch (event) {
                    case PRELOAD_READY:
                        hasAds = true;
                        if(showAds) interstitial.show();
                        break;

                    case AD_IMPRESSION:
                        if(runnable != null) runnable.run(true);
                        break;

                    case AD_FAILED:
                        hasAds = false;
                        if(runnable != null) runnable.run(false);
                        if(adsMediationListener != null) adsMediationListener.onResult(false);
                        break;

                    case VC_REWARDED:
                        if(adsMediationListener != null) adsMediationListener.onResult(true);
                        break;

                    case AD_DISMISSED:
                        LocaleHelper.onCreate(activity);
                        break;

                }
            }
        };

        AerServConfig config = new AerServConfig(activity, "1016571")
                .setPreload(true)
                .setEventListener(listener);

        interstitial = new AerServInterstitial(config);
    }

    public void preload(Activity activity){
        AerServConfig config = new AerServConfig(activity, "1016571")
                .setPreload(true);

        interstitial = new AerServInterstitial(config);
    }


    public void setAdsMediationListener(IAdsMediationListener adsMediationListener) {
        this.adsMediationListener = adsMediationListener;
    }
}
