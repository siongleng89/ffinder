package com.ffinder.android.models;

import android.content.Context;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.ffinder.android.enums.PreferenceType;
import com.ffinder.android.statics.Vars;
import com.ffinder.android.helpers.PreferenceUtils;
import com.ffinder.android.helpers.Strings;

import java.io.IOException;

/**
 * Created by SiongLeng on 8/9/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInfoModel {

    private String language;

    public UserInfoModel() {
    }


    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void save(Context context){
        try {
            String json = Vars.getObjectMapper().writeValueAsString(this);
            PreferenceUtils.put(context, PreferenceType.UserInfo, json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void load(Context context){
        String json = PreferenceUtils.get(context, PreferenceType.UserInfo);
        if(!Strings.isEmpty(json)){
            try {
                UserInfoModel userInfoModel = Vars.getObjectMapper().readValue(json, UserInfoModel.class);
                copyToThis(userInfoModel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void copyToThis(UserInfoModel userInfoModel){
        this.setLanguage(userInfoModel.getLanguage());
    }

    public void delete(Context context){
        PreferenceUtils.delete(context, PreferenceType.UserInfo);
    }

}
