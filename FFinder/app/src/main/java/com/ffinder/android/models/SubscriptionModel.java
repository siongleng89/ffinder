package com.ffinder.android.models;

import com.anjlab.android.iab.v3.SkuDetails;

/**
 * Created by SiongLeng on 15/9/2016.
 */
public class SubscriptionModel {

    private String title;
    private String content;
    private SkuDetails skuDetails;

    public SubscriptionModel(String title, String content, SkuDetails skuDetails) {
        this.title = title;
        this.content = content;
        this.skuDetails = skuDetails;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public SkuDetails getSkuDetails() {
        return skuDetails;
    }

    public void setSkuDetails(SkuDetails skuDetails) {
        this.skuDetails = skuDetails;
    }
}
