package com.ffinder.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.ffinder.android.absint.activities.MyActivityAbstract;
import com.ffinder.android.adapters.VipAdapter;
import com.ffinder.android.enums.AnalyticEvent;
import com.ffinder.android.helpers.Analytics;
import com.ffinder.android.models.SubscriptionModel;
import com.ffinder.android.utils.*;

import java.util.ArrayList;

public class ActivityVip extends MyActivityAbstract {

    private ListView listViewVip;
    private String userId;
    private VipAdapter vipAdapter;
    private ArrayList<SubscriptionModel> subscriptionModels;
    private Boolean originalIsSubscribed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vip);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(R.string.vip_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userId = getIntent().getStringExtra("userId");

        listViewVip = (ListView) findViewById(R.id.listViewVip);

        subscriptionModels = (ArrayList<SubscriptionModel>) ((MyApplication) getApplication()).getVipAndProductsHelpers().getSubscriptionModels().clone();

        vipAdapter = new VipAdapter(this, R.layout.lvitem_vip, subscriptionModels);
        listViewVip.setAdapter(vipAdapter);
        setListeners();
        refreshVIPInList();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ((MyApplication) getApplication()).getVipAndProductsHelpers().refreshIsSubscribed(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshVIPInList();
    }

    private void refreshVIPInList(){
        for(SubscriptionModel subscriptionModel : subscriptionModels){
            if(subscriptionModel.getTitle().equals(getString(R.string.manage_subscriptions_item_title))){
                if(((MyApplication) getApplication()).getVipAndProductsHelpers().getSubscribed()){
                    if(originalIsSubscribed != null && !originalIsSubscribed){
                        Analytics.logEvent(AnalyticEvent.User_Subscribed);
                    }
                    originalIsSubscribed = true;
                    subscriptionModel.setContent(getString(R.string.subscribed));
                }
                else{
                    originalIsSubscribed = false;
                    subscriptionModel.setContent(getString(R.string.not_subscribed));
                }
            }
        }

        vipAdapter.notifyDataSetChanged();
    }

    private void setListeners(){
        listViewVip.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SubscriptionModel subscriptionModel = subscriptionModels.get(position);
                if(subscriptionModel.getSkuDetails() != null){
                    ((MyApplication) getApplication()).getVipAndProductsHelpers().purchaseSubscription(ActivityVip.this, subscriptionModel);
                    Analytics.logEvent(AnalyticEvent.Click_Subscribe, subscriptionModel.getSkuDetails().productId);
                }
                else{
                    AndroidUtils.showDialog(ActivityVip.this, "", getString(R.string.manage_subscriptions_introduction), null);
                }
            }
        });
    }



}
