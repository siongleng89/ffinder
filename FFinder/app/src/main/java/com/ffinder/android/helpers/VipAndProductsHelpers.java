package com.ffinder.android.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.util.Pair;
import android.widget.Toast;
import com.ffinder.android.R;
import com.ffinder.android.absint.databases.FirebaseListener;
import com.ffinder.android.absint.helpers.RestfulListener;
import com.ffinder.android.enums.PreferenceType;
import com.ffinder.android.enums.Status;
import com.ffinder.android.helpers.Iab.*;
import com.ffinder.android.models.SubscriptionModel;
import com.ffinder.android.statics.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SiongLeng on 15/9/2016.
 */
public class VipAndProductsHelpers {

    private static VipAndProductsHelpers instance;
    private IabHelper iabHelper;
    private ArrayList<SubscriptionModel> subscriptionModels;
    private Context context;
    private String subscribeSku;
    private boolean subscribed;
    private boolean ready;
    private ArrayList<RunnableArgs<Boolean>> toRunRunnables;
    private static final int RC_REQUEST = 10001;
    private final String specialPromoSku = "promo_one_month";
    private Runnable onPurchaseComplete;

    public static VipAndProductsHelpers getInstance(Context context){
        if(instance == null){
            instance = new VipAndProductsHelpers(context);
        }
        return instance;
    }

    public VipAndProductsHelpers(Context context) {
        this.context = context;
        subscriptionModels = new ArrayList();
        toRunRunnables = new ArrayList();

        iabHelper = new IabHelper(context, Constants.IABKey);
        iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {
                if(result.isSuccess()){
                    FirebaseDB.getAllProducts(new FirebaseListener<ArrayList<Pair<String, Object>>>() {
                        @Override
                        public void onResult(ArrayList<Pair<String, Object>> result, Status status) {
                            if(status == Status.Success && result != null && result.size() > 0){
                                for(Pair<String, Object> pair : result){
                                    //will only have one subscribe product
                                    subscribeSku = pair.first;
                                    break;
                                }
                            }

                            List<String> moreSkus = new ArrayList<String>();
                            moreSkus.add(subscribeSku);
                            moreSkus.add(specialPromoSku);  //special handling for promo

                            try {
                                iabHelper.queryInventoryAsync(true, moreSkus, moreSkus,
                                    mGotInventoryListener);

                            } catch (IabHelper.IabAsyncInProgressException e) {
                                e.printStackTrace();
                                cannotDetermineUserSubscribed();
                            }
                        }
                    });
                }
            }
        });
    }

    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new
            IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, final Inventory inventory) {
            // Have we been disposed of in the meantime? If so, quit.
            if (iabHelper == null){
                cannotDetermineUserSubscribed();
                return;
            }

            // Is it a failure?
            if (result.isFailure()) {
                cannotDetermineUserSubscribed();
                return;
            }


            SkuDetails skuDetails = inventory.getSkuDetails(subscribeSku);
            if(skuDetails != null){
                subscriptionModels.add(new SubscriptionModel(skuDetails.getTitle(),
                        skuDetails.getPrice(), skuDetails));
            }


            Purchase subscribedModel = null;

            if(!Strings.isEmpty(subscribeSku)){
                subscribedModel = inventory.getPurchase(subscribeSku);

                if(subscribedModel == null){
                    subscribedModel = inventory.getPurchase(specialPromoSku);
                }
            }

            if(subscribedModel != null){
                final Purchase finalSubscribedModel = subscribedModel;
                RestfulService.checkSubscriptionRemainingMs(subscribedModel.getSku(),
                        subscribedModel.getToken(), new RestfulListener<String>() {
                            @Override
                            public void onResult(String result, Status status) {
                                if(Strings.isNumeric(result)){
                                    //subscribe not yet expired
                                    if(Long.valueOf(result) > 0){
                                        userIsSubscriber();
                                    }
                                    //subscribe expired
                                    else{
                                        //is auto renewing, can count as vip even is expired
                                        if(finalSubscribedModel.isAutoRenewing()){
                                            userIsSubscriber();
                                        }
                                        else{
                                            userNotSubscriber();
                                        }
                                    }
                                }
                                else{
                                    userNotSubscriber();
                                }
                            }
                        });

            }
            else{
                userNotSubscriber();
            }

        }
    };

    private void userIsSubscriber(){
        subscribed = true;
        PreferenceUtils.put(context, PreferenceType.ISVip, "1");
        onIabReady();
    }

    private void userNotSubscriber(){
        PreferenceUtils.delete(context, PreferenceType.ISVip);
        onIabReady();
    }


    //cannot determine as Iab error occurred, temporarily use cache vip value until next reboot
    private void cannotDetermineUserSubscribed(){
        String isVip = PreferenceUtils.get(context, PreferenceType.ISVip);
        if(!Strings.isEmpty(isVip) && isVip.equals("1")){
            userIsSubscriber();
        }
        else{
            userNotSubscriber();
        }
    }


    private void onIabReady(){
        ready = true;

        Threadings.postRunnable(new Runnable() {
            @Override
            public void run() {
                for(RunnableArgs<Boolean> toRun: toRunRunnables){
                    toRun.run(subscribed);
                }

                toRunRunnables.clear();
            }
        });
    }


    public void isInVipPeriod(final RunnableArgs<Boolean> onResult){
        if(!ready){
            toRunRunnables.add(onResult);
        }
        else{
            onResult.run(subscribed);
        }
    }


    public void purchaseSubscription(Activity activity, SubscriptionModel subscriptionModel,
                                     Runnable onFinish){

        this.onPurchaseComplete = onFinish;
        try {
            iabHelper.launchSubscriptionPurchaseFlow(activity,
                    subscriptionModel.getSkuDetails().getSku(),
                    RC_REQUEST, mPurchaseFinishedListener,
                    Constants.IABDeveloperPayload);
        } catch (IabHelper.IabAsyncInProgressException e) {

        }

    }

    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            // if we were disposed of in the meantime, quit.
            if (iabHelper == null) return;

            if (result.isFailure()) {
                return;
            }

            if (purchase.getDeveloperPayload() == null ||
                    !purchase.getDeveloperPayload().equals(Constants.IABDeveloperPayload)) {
                return;
            }

            PreferenceUtils.put(context, PreferenceType.ISVip, "1");
            subscribed = true;
            if(onPurchaseComplete != null) onPurchaseComplete.run();
        }
    };

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (iabHelper == null) return false;

        // Pass on the activity result to the helper for handling
        if (!iabHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            return false;
        }
        else{
            return true;
        }
    }


    public ArrayList<SubscriptionModel> getSubscriptionModels() {
        return subscriptionModels;
    }


    public void dispose(){
        if (iabHelper != null) {
            iabHelper.disposeWhenFinished();
            iabHelper = null;
        }
    }

}
