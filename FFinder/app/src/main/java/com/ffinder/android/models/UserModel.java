package com.ffinder.android.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by sionglengho on 23/10/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserModel {

    private String token;
    private String platform;

    public UserModel() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPlatform() {
        if(platform == null){
            platform = "android";
        }
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
}
