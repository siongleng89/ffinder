package com.ffinder.android.helpers;

import android.app.Activity;
import android.content.Context;
import android.support.v4.util.Pair;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.ffinder.android.R;
import com.ffinder.android.absint.databases.FirebaseListener;
import com.ffinder.android.enums.Status;
import com.ffinder.android.models.SubscriptionModel;
import com.ffinder.android.statics.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SiongLeng on 15/9/2016.
 */
public class VipAndProductsHelpers implements BillingProcessor.IBillingHandler  {

    private static VipAndProductsHelpers instance;
    private BillingProcessor bp;
    private ArrayList<SubscriptionModel> subscriptionModels;
    private Context context;
    private Boolean subscribed;

    public static VipAndProductsHelpers getInstance(Context context){
        if(instance == null){
            instance = new VipAndProductsHelpers(context);
        }
        return instance;
    }

    public VipAndProductsHelpers(Context context) {
        this.context = context;
        subscriptionModels = new ArrayList();
        subscriptionModels.add(new SubscriptionModel(context.getString(R.string.manage_subscriptions_item_title), "", null));
        bp = new BillingProcessor(context, Constants.IABKey, this);
    }

    public void isInVipPeriod(final RunnableArgs<Boolean> onResult){
        //onResult.run(true);

        if(subscribed == null){
            refreshIsSubscribed(new Runnable() {
                @Override
                public void run() {
                    onResult.run(subscribed);
                }
            });
        }
        else{
            onResult.run(subscribed);
        }
    }

    public Boolean getSubscribed() {
        return subscribed;
    }

    public void purchaseSubscription(Activity activity, SubscriptionModel subscriptionModel){
        bp.subscribe(activity, subscriptionModel.getSkuDetails().productId);
    }

    public void refreshIsSubscribed(final Runnable onDone){
        Threadings.runInBackground(new Runnable() {
            @Override
            public void run() {
                int count = 0;

                while (!bp.loadOwnedPurchasesFromGoogle()){
                    Threadings.sleep(500);
                    count++;
                    if(count > 10) break;
                }

                List<String> list = bp.listOwnedSubscriptions();
                if(list.size() > 0){
                    subscribed = true;
                    subscriptionModels.get(0).setContent(context.getString(R.string.subscribed));
                }
                else{
                    subscribed = false;
                    subscriptionModels.get(0).setContent(context.getString(R.string.not_subscribed));
                }

                if(onDone != null) onDone.run();

            }
        });
    }


    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
    }

    @Override
    public void onPurchaseHistoryRestored() {
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
    }

    @Override
    public void onBillingInitialized() {
        FirebaseDB.getAllProducts(new FirebaseListener<ArrayList<Pair<String, Object>>>() {
            @Override
            public void onResult(ArrayList<Pair<String, Object>> result, Status status) {
                ArrayList<String> subscriptionIds = new ArrayList();
                if(status == Status.Success && result != null && result.size() > 0){
                    for(Pair<String, Object> pair : result){
                        subscriptionIds.add(pair.first);
                    }
                }

                List<SkuDetails> skuDetails = bp.getSubscriptionListingDetails(subscriptionIds);
                for(SkuDetails skuDetail : skuDetails){
                    if(skuDetail != null && !Strings.isEmpty(skuDetail.description) &&
                            !Strings.isEmpty(skuDetail.priceText)){
                        subscriptionModels.add(new SubscriptionModel(
                                skuDetail.description, skuDetail.priceText, skuDetail));
                    }
                }

            }
        });

        refreshIsSubscribed(null);
    }

    public ArrayList<SubscriptionModel> getSubscriptionModels() {
        return subscriptionModels;
    }


    public void dispose(){
        if (bp != null)
            bp.release();
    }

}
