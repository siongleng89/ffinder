package com.ffinder.android.models;

import android.content.Context;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.ffinder.android.absint.models.FriendModelChangedListener;
import com.ffinder.android.enums.PreferenceType;
import com.ffinder.android.enums.SearchResult;
import com.ffinder.android.enums.SearchStatus;
import com.ffinder.android.statics.Vars;
import com.ffinder.android.utils.Logs;
import com.ffinder.android.utils.PreferenceUtils;
import com.ffinder.android.utils.Strings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by SiongLeng on 30/8/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FriendModel {

    private String userId;
    private String userKey;
    private String name;
    private String yourName;
    private LocationModel lastLocationModel;
    private SearchStatus searchStatus;
    private SearchResult searchResult;
    private FriendModelChangedListener friendModelChangedListener;
    private boolean notifyMeWhenLocated;

    public FriendModel() {
        searchResult = SearchResult.Normal;
    }

    public void load(Context context){
        String json = PreferenceUtils.get(context, this.getUserId());
        if(!Strings.isEmpty(json)){
            try {

                FriendModel retrievedFriendModel = Vars.getObjectMapper().readValue(json, FriendModel.class);
                copyToThis(retrievedFriendModel);
                onModelChanged();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void save(Context context){
        String json = null;
        try {
            json = Vars.getObjectMapper().writeValueAsString(this);
            PreferenceUtils.put(context, getUserId(), json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void delete(Context context){
        PreferenceUtils.delete(context, getUserId());
    }

    private void copyToThis(FriendModel friendModel){
        this.setUserKey(friendModel.getUserKey());
        this.setName(friendModel.getName());
        this.setYourName(friendModel.getYourName());
        this.setLastLocationModel(friendModel.getLastLocationModel());
        this.setSearchResult(friendModel.getSearchResult());
        this.setSearchStatus(friendModel.getSearchStatus());
        this.setNotifyMeWhenLocated(friendModel.isNotifyMeWhenLocated());
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @JsonIgnore
    public String getUserKey() {
        return userKey;
    }

    @JsonIgnore
    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    @JsonIgnore
    public String getYourName() {
        return yourName;
    }

    @JsonIgnore
    public void setYourName(String yourName) {
        this.yourName = yourName;
    }

    public LocationModel getLastLocationModel() {
        if(lastLocationModel == null){
            lastLocationModel = new LocationModel();
            lastLocationModel.setLongitude("");
            lastLocationModel.setLongitude("");
        }
        return lastLocationModel;
    }

    public void setLastLocationModel(LocationModel newLastLocationModel) {
        if(newLastLocationModel != null
                && !Strings.isEmpty(newLastLocationModel.getLongitude())
                && !Strings.isEmpty(newLastLocationModel.getLatitude())
                && (!newLastLocationModel.getLatitude().equals(this.getLastLocationModel().getLatitude())
                || (!newLastLocationModel.getLongitude().equals(this.getLastLocationModel().getLongitude()))
                || (!newLastLocationModel.getAddress().equals(this.getLastLocationModel().getAddress())))){
            this.lastLocationModel = newLastLocationModel;
            onModelChanged();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        onModelChanged();
    }

    public boolean isNotifyMeWhenLocated() {
        return notifyMeWhenLocated;
    }

    public void setNotifyMeWhenLocated(boolean notifyMeWhenLocated) {
        this.notifyMeWhenLocated = notifyMeWhenLocated;
        onModelChanged();
    }

    @JsonIgnore
    public SearchStatus getSearchStatus() {
        if(searchStatus == null) searchStatus = SearchStatus.End;
        return searchStatus;
    }

    @JsonIgnore
    public void setSearchStatus(SearchStatus newSearchStatus) {
        if(this.searchStatus != newSearchStatus){
            this.searchStatus = newSearchStatus;
            onModelChanged();
            Logs.show("New search status for: " + getName() + " is " + this.searchStatus);
        }
    }

    public SearchResult getSearchResult() {
        return searchResult;
    }

    public void setSearchResult(SearchResult newSearchResult) {
        if(this.searchResult != newSearchResult){
            this.searchResult = newSearchResult;
            onModelChanged();
            Logs.show("New search result for : " + getName() + " is " + this.searchResult);
        }
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof FriendModel && !Strings.isEmpty(this.getUserId()) && !Strings.isEmpty(((FriendModel) o).getUserId())){
            return ((FriendModel) o).getUserId().equals(this.getUserId());
        }
        else return super.equals(o);
    }

    @JsonIgnore
    public FriendModelChangedListener getFriendModelChangedListener() {
        return friendModelChangedListener;
    }

    @JsonIgnore
    public void setFriendModelChangedListener(FriendModelChangedListener friendModelChangedListener) {
        this.friendModelChangedListener = friendModelChangedListener;
    }

    private void onModelChanged(){
        if(getFriendModelChangedListener() != null){
            friendModelChangedListener.onChanged();
        }
    }

}
