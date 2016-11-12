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
    private AerServInterstitial interstitial;
    private IAdsMediationListener adsMediationListener;
    private boolean preloaded;

    public AdsMediation(Activity activity) {
        this.activity = activity;
        _this = this;

        AerServSdk.init(activity, "1001717");
    }

    public void showRewardedVideo(final Activity activity) {
        final boolean[] adsIsLoaded = new boolean[1];

        AerServEventListener listener = new AerServEventListener() {
            @Override
            public void onAerServEvent(AerServEvent event, List<Object> args) {
                Logs.show(event.name());
                switch (event) {
                    case PRELOAD_READY:
                        interstitial.show();
                        break;

                    case AD_LOADED:
                        Logs.show("Ads loaded");
                        adsIsLoaded[0] = true;
                        break;

                    case AD_IMPRESSION:
                        break;

                    case AD_FAILED:
                        Threadings.delay(1000, new Runnable() {
                            @Override
                            public void run() {
                                if(adsIsLoaded[0]) return;
                                Logs.show("Ads failed");
                                if(adsMediationListener != null) adsMediationListener.onResult(false);
                            }
                        });

                        break;

                    case VC_REWARDED:
                        if(adsMediationListener != null) adsMediationListener.onResult(true);
                        break;

                    case AD_DISMISSED:
                        break;

                }
            }
        };

        AerServConfig config = new AerServConfig(activity, "1016571")
                .setEventListener(listener);

        if(!preloaded){
            config.setPreload(true);
            preloaded = true;
            interstitial = new AerServInterstitial(config);
        }
        else{
            interstitial = new AerServInterstitial(config);
            interstitial.show();
        }


    }

    public void preload(){
        if(preloaded) return;

        preloaded = true;
        AerServConfig config = new AerServConfig(activity, "1016571")
                .setPreload(true);
        interstitial = new AerServInterstitial(config);
    }

    public void setAdsMediationListener(IAdsMediationListener adsMediationListener) {
        this.adsMediationListener = adsMediationListener;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }
}
