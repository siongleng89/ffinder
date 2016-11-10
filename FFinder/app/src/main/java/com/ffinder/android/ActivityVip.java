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
import com.ffinder.android.helpers.Analytics;
import com.ffinder.android.helpers.AnimateBuilder;
import com.ffinder.android.helpers.OverlayBuilder;
import com.ffinder.android.helpers.Threadings;
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
        monitorSubscriptionModels();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        refreshSubscribe();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void monitorSubscriptionModels(){
        Threadings.runInBackground(new Runnable() {
            @Override
            public void run() {
                while (subscriptionModels.size() == 0){
                    if(disposed) return;
                    else{
                        Threadings.sleep(1000);
                    }
                }

                Threadings.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        refreshSubscribe();
                    }
                });

            }
        });
    }

    private void refreshSubscribe(){
        layoutSubscribe.setVisibility(View.GONE);

        ((MyApplication) getApplication()).getVipAndProductsHelpers().refreshIsSubscribed(new Runnable() {
            @Override
            public void run() {
                subscriptionChanged(((MyApplication) getApplication())
                        .getVipAndProductsHelpers().getSubscribed());
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
                        ((MyApplication) getApplication()).getVipAndProductsHelpers().purchaseSubscription(ActivityVip.this, subscriptionModel);
                        Analytics.logEvent(AnalyticEvent.Click_Subscribe, subscriptionModel.getSkuDetails().productId);
                    }
                }
            }
        });

        layoutDummy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(subscriptionModels.size() > 0){
                    SubscriptionModel subscriptionModel = subscriptionModels.get(0);
                    ((MyApplication) getApplication()).getVipAndProductsHelpers().purchaseSubscription(ActivityVip.this, subscriptionModel);
                    Analytics.logEvent(AnalyticEvent.Click_Subscribe, subscriptionModel.getSkuDetails().productId);
                }
            }
        });

    }



}
