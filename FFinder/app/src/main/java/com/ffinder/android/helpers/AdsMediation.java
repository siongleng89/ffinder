package com.ffinder.android.helpers;


import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;
import com.appodeal.ads.Appodeal;
import com.appodeal.ads.InterstitialCallbacks;
import com.appodeal.ads.utils.Log;
import com.ffinder.android.R;
import com.ffinder.android.absint.helpers.IAdsMediationListener;
import com.ffinder.android.enums.OverlayType;
import com.ffinder.android.statics.Constants;
import com.google.android.gms.ads.AdRequest;

import java.util.List;

/**
 * Created by SiongLeng on 22/8/2016.
 */
public class AdsMediation {

    private static Activity myActivity;

    public static void init(Activity activity){
        myActivity = activity;

        Appodeal.disableNetwork(myActivity, "chartboost");
        Appodeal.disableNetwork(myActivity, "cheetah");

        Appodeal.initialize(myActivity, Constants.AppoDealKey,
                Appodeal.INTERSTITIAL);

        Appodeal.setLogLevel(Log.LogLevel.debug);
    }

    public static void preload(){
        if(Appodeal.isLoaded(Appodeal.INTERSTITIAL)) {
            return;
        }

        Appodeal.cache(myActivity, Appodeal.INTERSTITIAL);
    }

    public static void showAds(final IAdsMediationListener adsMediationListener){
        Appodeal.setInterstitialCallbacks(new InterstitialCallbacks() {
            @Override
            public void onInterstitialLoaded(boolean isPrecache) {
                Logs.show("Ads loaded successfully!");
            }

            @Override
            public void onInterstitialFailedToLoad() {
                if(adsMediationListener != null) adsMediationListener.onResult(false);
            }

            @Override
            public void onInterstitialShown() {
                if(adsMediationListener != null) adsMediationListener.onResult(true);
            }

            @Override
            public void onInterstitialClicked() {
            }

            @Override
            public void onInterstitialClosed() {

            }
        });


        if(!Appodeal.isLoaded(Appodeal.INTERSTITIAL)){
            final boolean[] cancel = {false};

            final AlertDialog dialog = OverlayBuilder.build(myActivity)
                    .setOverlayType(OverlayType.Loading)
                    .setContent(myActivity.getString(R.string.loading))
                    .setOnDismissRunnable(new Runnable() {
                        @Override
                        public void run() {
                            cancel[0] = true;
                        }
                    })
                    .show();


            Threadings.runInBackground(new Runnable() {
                @Override
                public void run() {
                    int i = 0;
                    while (!Appodeal.isLoaded(Appodeal.INTERSTITIAL)){
                        preload();
                        Threadings.sleep(3000);
                        i++;
                        if(cancel[0]){
                            return;
                        }
                        if(i == 10){
                            break;
                        }
                    }

                    Threadings.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            if(!cancel[0]){
                                if(Appodeal.isLoaded(Appodeal.INTERSTITIAL)){
                                    Appodeal.show(myActivity, Appodeal.INTERSTITIAL);
                                }
                                else{
                                    if(adsMediationListener != null)
                                        adsMediationListener.onResult(false);
                                }

                            }
                            dialog.dismiss();
                        }
                    });

                }
            });
        }
        else{
            Appodeal.show(myActivity, Appodeal.INTERSTITIAL);
        }

    }


}
