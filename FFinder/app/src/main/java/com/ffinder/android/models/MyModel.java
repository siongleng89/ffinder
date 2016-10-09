package com.ffinder.android.models;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.ffinder.android.absint.databases.FirebaseListener;
import com.ffinder.android.absint.helpers.RestfulListener;
import com.ffinder.android.absint.models.FriendModelChangedListener;
import com.ffinder.android.absint.models.MyModelChangedListener;
import com.ffinder.android.enums.PreferenceType;
import com.ffinder.android.enums.Status;
import com.ffinder.android.helpers.FirebaseDB;
import com.ffinder.android.helpers.RestfulService;
import com.ffinder.android.statics.Constants;
import com.ffinder.android.statics.Vars;
import com.ffinder.android.utils.*;
import com.firebase.client.Firebase;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * Created by SiongLeng on 1/9/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MyModel implements Serializable {

    private String userId;
    private String userKey;
    private UserInfoModel userInfoModel;
    private long userKeyGeneratedUnixTime;
    private Context context;
    private ArrayList<FriendModel> friendModels;
    private boolean cancelGenerateKey;
    private boolean firebaseLogon;

    public MyModel() {
    }

    public MyModel(Context context) {
        this.context = context;
        load();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    @JsonIgnore
    public UserInfoModel getUserInfoModel() {
        if(userInfoModel == null){
            userInfoModel = new UserInfoModel();
            userInfoModel.load(context);
        }
        return userInfoModel;
    }

    @JsonIgnore
    public void setUserInfoModel(UserInfoModel userInfoModel) {
        this.userInfoModel = userInfoModel;
    }

    public long getUserKeyGeneratedUnixTime() {
        return userKeyGeneratedUnixTime;
    }

    @JsonIgnore
    public long getUserKeyExpiredUnixTime(){
        return userKeyGeneratedUnixTime + (Constants.KeyExpiredTotalSecs);
    }

    public void setUserKeyGeneratedUnixTime(long userKeyGeneratedUnixTime) {
        this.userKeyGeneratedUnixTime = userKeyGeneratedUnixTime;
    }

    public void load(){
        try {
            String json = PreferenceUtils.get(context, PreferenceType.MyModel);
            if(!Strings.isEmpty(json)){
                MyModel myModel = Vars.getObjectMapper().readValue(json, MyModel.class);
                copyToThis(myModel);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyToThis(MyModel myModel){
        this.setUserId(myModel.getUserId());
        this.setUserKey(myModel.getUserKey());
        this.setUserKeyGeneratedUnixTime(myModel.getUserKeyGeneratedUnixTime());
    }

    public void save(){
        try {
            String json = Vars.getObjectMapper().writeValueAsString(this);
            PreferenceUtils.put(context, PreferenceType.MyModel, json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void delete(){
        PreferenceUtils.delete(context, PreferenceType.MyModel);
        PreferenceUtils.delete(context, PreferenceType.FriendUserIds);
        PreferenceUtils.delete(context, PreferenceType.FirebaseToken);
        getUserInfoModel().delete(context);
        FirebaseAuth.getInstance().signOut();
        firebaseLogon = false;
    }

    public void loginFirebase(final int count, final RunnableArgs<Boolean> onFinish){
        if(firebaseLogon){
            if(onFinish != null) onFinish.run(true);
            return;
        }

        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            firebaseLogon = true;
            if(onFinish != null) onFinish.run(true);
            return;
        }


        String token = PreferenceUtils.get(context, PreferenceType.FirebaseToken);
        if(Strings.isEmpty(token)){
            RestfulService.getToken(getUserId(), new RestfulListener<String>() {
                @Override
                public void onResult(String result, Status status) {
                    if(status == Status.Success && !Strings.isEmpty(result)){
                        PreferenceUtils.put(context, PreferenceType.FirebaseToken, result);
                        loginFirebaseWithToken(count, result, onFinish);
                    }
                }
            });
        }
        else{
            loginFirebaseWithToken(count, token, onFinish);
        }
    }

    private void loginFirebaseWithToken(final int count, String token, final RunnableArgs<Boolean> onFinish){
        FirebaseDB.loginWithToken(token, new FirebaseListener() {
            @Override
            public void onResult(Object result, Status status) {
                if(status == Status.Success){
                    firebaseLogon = true;
                    if(onFinish != null) onFinish.run(true);
                }
                else{
                    if(count == 0){
                        //token might be expired, re-get and login again
                        PreferenceUtils.delete(context, PreferenceType.FirebaseToken);
                        loginFirebase(count + 1, onFinish);
                    }
                    else{
                        if(onFinish != null) onFinish.run(false);
                    }
                }
            }
        });
    }


    public void regenerateUserKey(final int counter, final RunnableArgs<String> onFinish){
        if(counter == 0){
            cancelGenerateKey = false;
        }

        if(cancelGenerateKey) return;

        final String newKey = Strings.generateUserKey();

        FirebaseDB.tryInsertKey(getUserId(), AndroidUtils.getUsername(context), newKey, new FirebaseListener<Boolean>() {
            @Override
            public void onResult(Boolean result, Status status) {
                if(status == Status.Success && result != null && result){
                    setUserKey(newKey);
                    setUserKeyGeneratedUnixTime(System.currentTimeMillis() / 1000L);
                    save();
                    onFinish.run(newKey);
                }
                else{
                    regenerateUserKey(counter + 1, onFinish);
                }
            }
        });
    }

    public void cancelRegenerateUserKey(){
        cancelGenerateKey = true;
    }

    public boolean checkUserKeyValid(){
        return !(System.currentTimeMillis() / 1000L - userKeyGeneratedUnixTime > (Constants.KeyExpiredTotalSecs) || Strings.isEmpty(userKey));
    }


    @JsonIgnore
    public ArrayList<FriendModel> getFriendModels() {
        if(friendModels == null) friendModels = new ArrayList();
        return friendModels;
    }

    @JsonIgnore
    public int getNonSelfFriendModelsCount() {
        if(friendModels == null) friendModels = new ArrayList();
        if(friendModels.size() == 1 && getFriendModelById(getUserId()) != null){
            return 0;
        }
        else{
            return friendModels.size();
        }
    }

    @JsonIgnore
    public FriendModel getFriendModelById(String userId){
        for(FriendModel friendModel : getFriendModels()){
            if(friendModel.getUserId().equals(userId)){
                return friendModel;
            }
        }
        return null;
    }

    public boolean checkFriendExist(String userId){
        FriendModel friendModel = getFriendModelById(userId);
        return (friendModel != null);
    }

    public void commitFriendUserIds(){
        try {
            ArrayList<String> userIds = new ArrayList();
            for(FriendModel friendModel : getFriendModels()){
                if(!userIds.contains(friendModel.getUserId())){
                    userIds.add(friendModel.getUserId());
                }
            }

            String json = Vars.getObjectMapper().writeValueAsString(userIds);
            PreferenceUtils.put(context, PreferenceType.FriendUserIds, json);
            Logs.show("Friends user ids committed: " + json);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void loadAllFriendModels(){
        String json = PreferenceUtils.get(context, PreferenceType.FriendUserIds);
        if(!Strings.isEmpty(json)){
            try {
                ArrayList<String> userIds = Vars.getObjectMapper().readValue(json, ArrayList.class);

                for(String userId : userIds){
                    if(getFriendModelById(userId) == null){
                        FriendModel friendModel = new FriendModel();
                        friendModel.setUserId(userId);
                        friendModel.load(context);
                        addFriendModel(friendModel);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadFriend(String friendId){
        String json = PreferenceUtils.get(context, PreferenceType.FriendUserIds);
        try {
            ArrayList<String> userIds = Vars.getObjectMapper().readValue(json, ArrayList.class);
            if(userIds.contains(friendId)){
                FriendModel friendModel = new FriendModel();
                friendModel.setUserId(friendId);
                friendModel.load(context);
                addFriendModel(friendModel);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addFriendModel(FriendModel friendModel){
        getFriendModels().add(friendModel);
    }

    public void deleteFriend(FriendModel friendModel){
        FriendModel toDeleteModel = getFriendModelById(friendModel.getUserId());
        if(toDeleteModel != null){
            getFriendModels().remove(toDeleteModel);
        }
    }

    public void sortFriendModels(){
        Collections.sort(getFriendModels(), new Comparator<FriendModel>(){
            public int compare(FriendModel o1, FriendModel o2)
            {
                return o1.getName().compareTo(o2.getName());
            }
        });
    }


}
