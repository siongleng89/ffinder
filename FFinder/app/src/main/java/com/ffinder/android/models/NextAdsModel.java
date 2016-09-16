package com.ffinder.android.models;

/**
 * Created by SiongLeng on 14/9/2016.
 */
public class NextAdsModel {

    private int remainingCount;
    private boolean showAdsNow;

    public NextAdsModel(int remainingCount, boolean showAdsNow) {
        this.remainingCount = remainingCount;
        this.showAdsNow = showAdsNow;
    }

    public int getRemainingCount() {
        return remainingCount;
    }

    public void setRemainingCount(int remainingCount) {
        this.remainingCount = remainingCount;
    }

    public boolean isShowAdsNow() {
        return showAdsNow;
    }

    public void setShowAdsNow(boolean showAdsNow) {
        this.showAdsNow = showAdsNow;
    }
}
