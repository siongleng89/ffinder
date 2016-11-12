package com.ffinder.android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.ffinder.android.absint.activities.MyActivityAbstract;
import com.ffinder.android.enums.ActionBarActionType;
import com.ffinder.android.enums.AnalyticEvent;
import com.ffinder.android.enums.OverlayType;
import com.ffinder.android.helpers.*;
import com.ffinder.android.models.SubscriptionModel;

import java.util.ArrayList;

public class ActivityVip extends MyActivityAbstract {

    private ArrayList<SubscriptionModel> subscriptionModels;
    private LinearLayout layoutSubscribe;
    private RelativeLayout layoutDummy;
    private TextView txtSubscriptionTitle, txtSubscriptionSubTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vip);

        enableCustomActionBar();
        addActionToActionBar(ActionBarActionType.Back, false, true);
        setActionBarTitle(R.string.vip_title);


        layoutDummy = (RelativeLayout) findViewById(R.id.layoutDummy);
        layoutSubscribe = (LinearLayout) findViewById(R.id.layoutSubscribe);
        txtSubscriptionTitle = (TextView) findViewById(R.id.txtSubscriptionTitle);
        txtSubscriptionSubTitle = (TextView) findViewById(R.id.txtSubscriptionSubtitle);



        subscriptionModels = (ArrayList<SubscriptionModel>)
                ((MyApplication) getApplication()).getVipAndProductsHelpers()
                        .getSubscriptionModels();

        refreshSubscribe();
    }

    //must hanle activity result for iabhelper to work
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(!((MyApplication) getApplication()).getVipAndProductsHelpers()
                .onActivityResult(requestCode, resultCode, data)){
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void refreshSubscribe(){
        layoutSubscribe.setVisibility(View.GONE);

        ((MyApplication) getApplication()).getVipAndProductsHelpers().isInVipPeriod(new RunnableArgs<Boolean>() {
            @Override
            public void run() {
                subscriptionChanged(this.getFirstArg());
            }
        });
    }

    private void subscriptionChanged(final boolean subscribed){
        Threadings.postRunnable(new Runnable() {
            @Override
            public void run() {
                if(subscribed){
                    txtSubscriptionSubTitle.setVisibility(View.GONE);
                    txtSubscriptionTitle.setText(getString(R.string.manage_subscriptions_item_title));
                }
                else{
                    txtSubscriptionSubTitle.setVisibility(View.VISIBLE);
                    txtSubscriptionTitle.setText(String.format(getString(R.string.vip_member), "?"));

                    if(subscriptionModels.size() > 0){
                        SubscriptionModel subscriptionModel = subscriptionModels.get(0);
                        txtSubscriptionTitle.setText(String.format(getString(R.string.vip_member),
                                subscriptionModel.getContent()));
                    }
                }

                AnimateBuilder.fadeIn(ActivityVip.this, layoutSubscribe);
                setListener(subscribed);
            }
        });
    }


    private void setListener(final boolean subscribed){
        layoutSubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(subscribed){
                    OverlayBuilder.build(ActivityVip.this)
                            .setOverlayType(OverlayType.OkOnly)
                            .setContent(getString(R.string.manage_subscriptions_introduction_android))
                            .show();
                }
                else{
                    if(subscriptionModels.size() > 0){
                        SubscriptionModel subscriptionModel = subscriptionModels.get(0);
                        ((MyApplication) getApplication())
                                .getVipAndProductsHelpers()
                                .purchaseSubscription(ActivityVip.this, subscriptionModel, new Runnable() {
                                    @Override
                                    public void run() {
                                        refreshSubscribe();
                                    }
                                });
                        Analytics.logEvent(AnalyticEvent.Click_Subscribe,
                                subscriptionModel.getSkuDetails().getSku());
                    }
                }
            }
        });

        layoutDummy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(subscriptionModels.size() > 0){
                    SubscriptionModel subscriptionModel = subscriptionModels.get(0);
                    ((MyApplication) getApplication()).getVipAndProductsHelpers()
                            .purchaseSubscription(ActivityVip.this, subscriptionModel, new Runnable() {
                                @Override
                                public void run() {
                                    refreshSubscribe();
                                }
                            });
                    Analytics.logEvent(AnalyticEvent.Click_Subscribe,
                            subscriptionModel.getSkuDetails().getSku());
                }
            }
        });

    }



}
