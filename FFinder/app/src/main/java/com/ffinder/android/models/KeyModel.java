package com.ffinder.android.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.Map;

/**
 * Created by SiongLeng on 6/9/2016.
 */
public class KeyModel {

    private String userId;
    private String userName;
    private long insertAt;

    public KeyModel() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @JsonIgnore
    public Map<String, String> getInsertAt() {
        return ServerValue.TIMESTAMP;
    }

    @JsonIgnore
    public void setInsertAt(Long insertAt) {
        this.insertAt = insertAt;
    }

    @Exclude
    public long getInsertAtLong(){
        return insertAt;
    }

}
