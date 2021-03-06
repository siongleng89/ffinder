package com.ffinder.android.helpers;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import com.ffinder.android.absint.databases.FirebaseListener;
import com.ffinder.android.absint.helpers.RestfulListener;
import com.ffinder.android.enums.Status;
import com.ffinder.android.models.*;
import com.ffinder.android.statics.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by SiongLeng on 30/8/2016.
 */
public class FirebaseDB {

    private static DatabaseReference database;

    public static void loginWithToken(String token, final FirebaseListener listener){
        final OnlineRequest onlineRequest = FirebaseOnlineTracker.get().requestOnline("loginWithToken");
        FirebaseAuth.getInstance().signInWithCustomToken(token)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        listener.onResult(null, task.isSuccessful() ? Status.Success : Status.Failed);
                        FirebaseOnlineTracker.get().releaseOnline(onlineRequest);
                    }
                });
    }

    public static String getNewUserId(){
        OnlineRequest onlineRequest = FirebaseOnlineTracker.get().requestOnline("getNewUserId");
        DatabaseReference db = getTable(TableName.users);
        FirebaseOnlineTracker.get().releaseOnline(onlineRequest);
        return db.push().getKey();
    }

    public static void saveNewUser(final String userId, String token, final FirebaseListener listener){
        DatabaseReference db = getTable(TableName.users);
        UserModel userModel = new UserModel();
        userModel.setToken(token);
        userModel.setPlatform("android");
        setValue(db.child(userId), userModel, listener);
    }

    public static void getUserIdByIdentifier(String identifier, final FirebaseListener<String> listener){
        identifier = identifier + "android";
        DatabaseReference db = getTable(TableName.identifierToUserIdMaps);
        getSingleData(db.child(identifier).child("userId"), listener);
    }

    public static void saveIdentifier(String userId, String identifier, final FirebaseListener listener){
        identifier = identifier + "android";
        DatabaseReference db = getTable(TableName.identifierToUserIdMaps);
        setValue(db.child(identifier).child("userId"), userId, listener);
    }

    public static void getUserData(String userId, final FirebaseListener<UserModel> listener){
        DatabaseReference db = getTable(TableName.users);
        getSingleData(db.child(userId), listener);
    }

    public static void updateMyToken(String myUserId, String newToken){
        setValue(getTable(TableName.users).child(myUserId).child("token"), newToken, null);
    }

    public static void tryInsertKey(final String myUserId, final String myUsername, final String key, final FirebaseListener<Boolean> listener){
        getSingleData(getTable(TableName.keys).child(key), new FirebaseListener<KeyModel>(KeyModel.class) {
            @Override
            public void onResult(final KeyModel keyModel, Status status) {
                final KeyModel toInsertModel = new KeyModel();
                toInsertModel.setUserId(myUserId);
                toInsertModel.setUserName(myUsername);

                if(status == Status.Success){
                    if(keyModel == null){
                        getTable(TableName.keys).child(key).setValue(toInsertModel, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                listener.onResult(true, databaseError);
                            }
                        });
                    }
                    else{
                        //already exist, check timestamp
                        getCurrentTimestamp(myUserId, new FirebaseListener<String>(String.class) {
                            @Override
                            public void onResult(String result, Status status) {
                                if(status == Status.Success && result != null){
                                    long differenceInSecs = DateTimeUtils.getDifferenceInSecs(keyModel.getInsertAtLong(), Long.valueOf(result));
                                    if(differenceInSecs > Constants.KeyExpiredTotalSecs){
                                        getTable(TableName.keys).child(key).setValue(toInsertModel, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                listener.onResult(true, databaseError);
                                            }
                                        });
                                    }
                                    else{
                                        listener.onResult(false, Status.Success);
                                    }
                                }
                                else{
                                    listener.onResult(false, Status.Success);
                                }
                            }
                        });
                    }

                }
                else{
                    listener.onResult(false, Status.Success);
                }
            }
        });
    }

    public static void removeUserKey(String userId, final String key, final FirebaseListener listener){
        checkMyKeyExist(userId, key, new FirebaseListener<KeyModel>(KeyModel.class) {
            @Override
            public void onResult(KeyModel result, Status status) {
                if(result != null && status == Status.Success){
                    //key is belong to me
                    getTable(TableName.keys).child(key).setValue(null);
                    if(listener != null) listener.onResult(null, Status.Success);
                }
                else{
                    if(listener != null) listener.onResult(null, Status.Success);
                }
            }
        });
    }


    public static void checkUserHasAnyLink(final String userId, final FirebaseListener<Boolean> listener){
        DatabaseReference db = getTable(TableName.links);
        checkExist(db.child(userId), listener);
    }

    public static void addNewLink(String myUserId, String targetUserId, String myName, String targetName, final FirebaseListener listener){
        DatabaseReference db = getTable(TableName.links);
        setValue(db.child(myUserId).child(targetUserId), targetName, null);
        setValue(db.child(targetUserId).child(myUserId), myName, listener);
    }

    public static void deleteLink(String myUserId, String targetUserId, final FirebaseListener listener){
        DatabaseReference db = getTable(TableName.links);
        setValue(db.child(myUserId).child(targetUserId), null, listener);
    }

    public static void editLinkName(String myUserId, String targetUserId, String newName, final FirebaseListener listener){
        DatabaseReference db = getTable(TableName.links);
        setValue(db.child(myUserId).child(targetUserId), newName, listener);
    }

    public static void checkLinkExist(String myUserId, String targetUserId, final FirebaseListener<Boolean> listener){
        DatabaseReference db = getTable(TableName.links);
        checkExist(db.child(targetUserId).child(myUserId), listener);
    }

    public static void getAllMyLinks(String myUserId, final FirebaseListener listener){
        DatabaseReference db = getTable(TableName.links);
        getData(db.child(myUserId), listener);
    }

    public static void checkKeyExist(final String myUserId, final String targetUserKey,
                                     final FirebaseListener<KeyModel> listener){
        getSingleData(getTable(TableName.keys).child(targetUserKey), new FirebaseListener<KeyModel>(KeyModel.class) {
            @Override
            public void onResult(final KeyModel keyModel, Status status) {
                if(status == Status.Success && keyModel != null){
                    getCurrentTimestamp(myUserId, new FirebaseListener<String>(String.class) {
                        @Override
                        public void onResult(String result, Status status) {
                            if(result != null && status == Status.Success){
                                long differenceInSecs = DateTimeUtils.
                                        getDifferenceInSecs(keyModel.getInsertAtLong(), Long.valueOf(result));
                                if(differenceInSecs < Constants.KeyExpiredTotalSecs){
                                    listener.onResult(keyModel, Status.Success);
                                }
                                else{
                                    listener.onResult(null, Status.Failed);
                                }
                            }
                            else{
                                listener.onResult(null, Status.Failed);
                            }
                        }
                    });
                }
                else{
                    listener.onResult(null, Status.Failed);
                }
            }
        });
    }

    public static void checkMyKeyExist(final String myUserId, final String myUserKey,
                                     final FirebaseListener<KeyModel> listener){
        getSingleData(getTable(TableName.keys).child(myUserKey), new FirebaseListener<KeyModel>(KeyModel.class) {
            @Override
            public void onResult(final KeyModel keyModel, Status status) {
                if(status == Status.Success && keyModel != null){
                    //make sure keymodel userId is mine
                    if(myUserId.equals(keyModel.getUserId())){
                        getCurrentTimestamp(myUserId, new FirebaseListener<String>(String.class) {
                            @Override
                            public void onResult(String result, Status status) {
                                if(result != null && status == Status.Success){
                                    long differenceInSecs = DateTimeUtils.
                                            getDifferenceInSecs(keyModel.getInsertAtLong(), Long.valueOf(result));
                                    if(differenceInSecs < Constants.KeyExpiredTotalSecs){
                                        listener.onResult(keyModel, Status.Success);
                                    }
                                    else{
                                        listener.onResult(null, Status.Failed);
                                    }
                                }
                                else{
                                    listener.onResult(null, Status.Failed);
                                }
                            }
                        });
                    }
                    else{
                        listener.onResult(null, Status.Failed);
                    }
                }
                else{
                    listener.onResult(null, Status.Failed);
                }
            }
        });
    }



    public static void changeBlockUser(String myUserId, String targetUserId, boolean block,
                                       FirebaseListener listener){
        String value = null;
        if(block) value = "1";

        setValue(getTable(TableName.blockUsers).child(myUserId).child(targetUserId), value, listener);
    }

    public static void checkMeIsBlock(String myUserId, String targetUserId,
                                      final FirebaseListener<Boolean> listener){
        getSingleData(getTable(TableName.blockUsers).child(targetUserId).child(myUserId),
                new FirebaseListener<String>(String.class) {
            @Override
            public void onResult(String result, Status status) {
                if(status == Status.Success && result != null){
                    listener.onResult(result.equals("1"), Status.Success);
                }
                else{
                    listener.onResult(false, Status.Success);
                }
            }
        });
    }


    public static void getCurrentTimestamp(final String userId, final FirebaseListener<String> listener){
        setValue(getTable(TableName.timestamps).child(userId), ServerValue.TIMESTAMP, new FirebaseListener() {
            @Override
            public void onResult(Object result, Status status) {
                if(status == Status.Failed){
                    listener.onResult("", Status.Failed);
                }
                else{
                    getSingleData(getTable(TableName.timestamps).child(userId), new FirebaseListener<Long>(Long.class) {
                        @Override
                        public void onResult(Long result, Status status) {
                            if(status == Status.Success && result != null){
                                listener.onResult(String.valueOf(result), Status.Success);
                            }
                            else{
                                listener.onResult("", Status.Failed);
                            }
                        }
                    });
                }
            }
        });
    }

    public static void setNextAdsCount(String myUserId, int newCount, final FirebaseListener listener){
        final DatabaseReference db = getTable(TableName.nextAds);
        setValue(db.child(myUserId).child("count"), newCount, listener);
    }

    public static void getNextAdsCount(final String myUserId, final FirebaseListener<Integer> listener){
        final DatabaseReference db = getTable(TableName.nextAds);
        getSingleData(db.child(myUserId).child("count"), new FirebaseListener<Integer>(Integer.class) {
            @Override
            public void onResult(Integer currentNextAdsCount, Status status) {
                if(status == Status.Success){
                    if(currentNextAdsCount == null){
                        RestfulService.adsWatched(myUserId, new RestfulListener<String>() {
                            @Override
                            public void onResult(String result, Status status) {
                                if(!Strings.isEmpty(result) && Strings.isNumeric(result) && status == Status.Success){
                                    listener.onResult(Integer.valueOf(result), Status.Success);
                                }
                                else{
                                    listener.onResult(0, Status.Success);
                                }
                            }
                        });
                    }
                    else{
                        listener.onResult(currentNextAdsCount, Status.Success);
                    }
                }
                else{
                    listener.onResult(0, Status.Success);
                }
            }
        });
    }

    public static void saveLog(String logEvent){
        final DatabaseReference db = getTable(TableName.logs);
        setValue(db.push(), logEvent, null);
    }

    public static void getAllProducts(final FirebaseListener<ArrayList<Pair<String, Object>>> listener){
        final DatabaseReference db = getTable(TableName.products);
        getData(db, listener);
    }

    private static void checkExist(DatabaseReference ref, final FirebaseListener<Boolean> listener) {
        final OnlineRequest onlineRequest = FirebaseOnlineTracker.get().requestOnline("checkExist");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                listener.onResult(snapshot.getValue() != null, Status.Success);
                FirebaseOnlineTracker.get().releaseOnline(onlineRequest);
            }
            @Override
            public void onCancelled(DatabaseError firebaseError) {
                listener.onResult(false, Status.Failed);
                FirebaseOnlineTracker.get().releaseOnline(onlineRequest);
            }
        });
    }



    private static void setValue(final DatabaseReference ref, Object value, final FirebaseListener listener){
        final OnlineRequest onlineRequest = FirebaseOnlineTracker.get().requestOnline("setValue");
        ref.setValue(value, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(listener != null) listener.onResult(databaseError);
                FirebaseOnlineTracker.get().releaseOnline(onlineRequest);
            }
        });
    }

    private static void getData(final DatabaseReference ref, final FirebaseListener listener){
        final OnlineRequest onlineRequest = FirebaseOnlineTracker.get().requestOnline("getData");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Object> results = new ArrayList<Object>();
                for(DataSnapshot postSnapShot : snapshot.getChildren()){
                    Object newPost;
                    if(listener.getType() == null){
                        Pair<String, Object> p = new Pair<String, Object>(postSnapShot.getKey(), postSnapShot.getValue());
                        newPost = p;
                    }
                    else{
                        newPost = postSnapShot.getValue(listener.getType());
                    }

                    results.add(newPost);
                }
                listener.onResult(results, Status.Success);
                FirebaseOnlineTracker.get().releaseOnline(onlineRequest);

            }
            @Override
            public void onCancelled(DatabaseError firebaseError) {
                listener.onResult(null, Status.Failed);
                FirebaseOnlineTracker.get().releaseOnline(onlineRequest);
            }
        });
    }

    private static void getSingleData(DatabaseReference ref, final FirebaseListener listener){
        final OnlineRequest onlineRequest = FirebaseOnlineTracker.get().requestOnline("getSingleData");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(listener != null) listener.onResult(snapshot.getValue(listener.getType()), Status.Success);
                }
                else{
                    if(listener != null) listener.onResult(null, Status.Success);
                }
                FirebaseOnlineTracker.get().releaseOnline(onlineRequest);
            }
            @Override
            public void onCancelled(DatabaseError firebaseError) {
                if(listener != null) listener.onResult(null, Status.Failed);
                FirebaseOnlineTracker.get().releaseOnline(onlineRequest);
            }
        });
    }


    private static ValueEventListener getSingleDataMonitor(DatabaseReference ref, final FirebaseListener listener){
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(listener.getType() != null){
                        listener.onResult(snapshot.getValue(listener.getType()), Status.Success);
                    }
                    else{
                        listener.onResult(snapshot.getValue(), Status.Success);
                    }
                }
                else{
                    listener.onResult(null, Status.Success);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onResult(null, Status.Failed);
            }
        };
        ref.addValueEventListener(valueEventListener);

        return valueEventListener;
    }


    private static DatabaseReference getTable(TableName tableName){
        return getDatabase().child(tableName.name());
    }

    public static DatabaseReference getDatabase() {
        if(database == null)
            database = FirebaseDatabase.getInstance().getReference();
        return database;
    }

    private enum TableName{
        users, links, locations, timestamps, pings, autoNotifications, keys, nextAds, showAdsIn, promoCodes, promoUsages,
        products, identifierToUserIdMaps, logs, blockUsers
    }

}
