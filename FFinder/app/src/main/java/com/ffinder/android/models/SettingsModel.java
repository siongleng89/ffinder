package com.ffinder.android.models;

/**
 * Created by SiongLeng on 8/9/2016.
 */
public class SettingsModel {

    private String title;

    public SettingsModel(String title) {
        this.title = title;
    }

    public SettingsModel() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
