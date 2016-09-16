package com.ffinder.android.helpers;

import android.support.annotation.IntegerRes;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import com.ffinder.android.absint.databases.FirebaseListener;
import com.ffinder.android.absint.helpers.RestfulListener;
import com.ffinder.android.enums.Status;
import com.ffinder.android.models.AutoNotificationModel;
import com.ffinder.android.models.KeyModel;
import com.ffinder.android.models.LocationModel;
import com.ffinder.android.models.NextAdsModel;
import com.ffinder.android.utils.DateTimeUtils;
import com.ffinder.android.utils.RunnableArgs;
import com.ffinder.android.utils.Strings;
import com.firebase.client.*;
import com.firebase.client.Query;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by SiongLeng on 30/8/2016.
 */
public class FirebaseDB {

    private static DatabaseReference database;

    public static void loginWithToken(String token, final FirebaseListener listener){
        FirebaseAuth.getInstance().signInWithCustomToken(token)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        listener.onResult(null, task.isSuccessful() ? Status.Success : Status.Failed);
                    }
                });
    }

    public static void saveNewUser(final String userId, final FirebaseListener<String> listener){
        DatabaseReference db = getTable(TableName.users);
        HashMap<String, String> map = new HashMap();
        map.put("dummy", "1");
        db.child(userId).setValue(map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(listener != null) listener.onResult(userId, databaseError);
            }
        });
    }

    public static void autoNotifyMe(String myUserId, String targetUserId, final FirebaseListener listener){
        AutoNotificationModel autoNotificationModel = new AutoNotificationModel();
        autoNotificationModel.setWaitingUserId(myUserId);
        DatabaseReference db = getTable(TableName.autoNotifications);
        db.child(targetUserId).child(myUserId).setValue(autoNotificationModel, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(listener != null) listener.onResult(null, databaseError);
            }
        });
    }

    public static void removeAutoNotify(String myUserId, String senderUserId){
        DatabaseReference db = getTable(TableName.autoNotifications);
        db.child(myUserId).child(senderUserId).setValue(null);
    }

    public static void getAutoNotifyList(String myUserId, FirebaseListener<ArrayList<AutoNotificationModel>> listener){
        DatabaseReference db = getTable(TableName.autoNotifications);
        getData(db.child(myUserId), listener);
    }

    public static void clearAllAutoNotification(String myUserId){
        DatabaseReference db = getTable(TableName.autoNotifications);
        db.child(myUserId).setValue(null);
    }

    public static void updateMyToken(String myUserId, String newToken){
        getTable(TableName.users).child(myUserId).child("token").setValue(newToken);
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
                                    if(differenceInSecs > 70 * 60){
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

    public static void addNewLink(String myUserId, String targetUserId, String myName, String targetName, final FirebaseListener listener){
        DatabaseReference db = getTable(TableName.links);
        db.child(myUserId).child(targetUserId).setValue(targetName);
        db.child(targetUserId).child(myUserId).setValue(myName, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(listener != null) listener.onResult(databaseError);
            }
        });
    }

    public static void deleteLink(String myUserId, String targetUserId, final FirebaseListener listener){
        DatabaseReference db = getTable(TableName.links);
        db.child(myUserId).child(targetUserId).setValue(null, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(listener != null) listener.onResult(databaseError);
            }
        });
    }

    public static void editLinkName(String myUserId, String targetUserId, String newName, final FirebaseListener listener){
        DatabaseReference db = getTable(TableName.links);
        db.child(myUserId).child(targetUserId).setValue(newName, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(listener != null) listener.onResult(databaseError);
            }
        });
    }

    public static void checkLinkExist(String myUserId, String targetUserId, final FirebaseListener<Boolean> listener){
        DatabaseReference db = getTable(TableName.links);
        checkExist(db.child(targetUserId).child(myUserId), listener);
    }

    public static void checkKeyExist(final String userId, final String userKey, final FirebaseListener<KeyModel> listener){
        getSingleData(getTable(TableName.keys).child(userKey), new FirebaseListener<KeyModel>(KeyModel.class) {
            @Override
            public void onResult(final KeyModel keyModel, Status status) {
                if(status == Status.Success && keyModel != null){
                    getCurrentTimestamp(userId, new FirebaseListener<String>(String.class) {
                        @Override
                        public void onResult(String result, Status status) {
                            if(result != null && status == Status.Success){
                                long differenceInSecs = DateTimeUtils.getDifferenceInSecs(keyModel.getInsertAtLong(), Long.valueOf(result));
                                if(differenceInSecs < 65 * 60){
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

    public static void updateLocation(String userId, LocationModel locationModel, final FirebaseListener listener){
        getTable(TableName.locations).child(userId).setValue(locationModel, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(listener != null) listener.onResult(databaseError);
            }
        });
    }

    public static void respondToPing(String myUserId, String fromUserId, final  FirebaseListener listener){
        getTable(TableName.pings).child(myUserId).child(fromUserId).setValue(ServerValue.TIMESTAMP, listener);
    }

    public static void getCurrentTimestamp(final String userId, final FirebaseListener<String> listener){
        getTable(TableName.timestamps).child(userId).setValue(ServerValue.TIMESTAMP, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(databaseError != null){
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

    public static void getUserLocation(String targetUserId, final FirebaseListener<LocationModel> listener){
        getSingleData(getTable(TableName.locations).child(targetUserId), listener);
    }


    public static void getAllMyLinks(String myUserId, final FirebaseListener listener){
        DatabaseReference db = getTable(TableName.links);
        getData(db.child(myUserId), listener);
    }

    public static void setNextAdsCount(String myUserId, int newCount, final FirebaseListener listener){
        final DatabaseReference db = getTable(TableName.nextAds);
        db.child(myUserId).child("count").setValue(newCount, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(listener != null) listener.onResult(null, databaseError);
            }
        });
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


    public static void getAllPromoCodeUsages(String userId,  final FirebaseListener<ArrayList<Pair<String, Object>>> listener){
        final DatabaseReference db = getTable(TableName.promoUsages);
        getData(db.child(userId), listener);
    }

    public static void getAllProducts(final FirebaseListener<ArrayList<Pair<String, Object>>> listener){
        final DatabaseReference db = getTable(TableName.products);
        getData(db, listener);
    }

    private static void checkExist(DatabaseReference ref, final FirebaseListener<Boolean> listener) {
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                listener.onResult(snapshot.getValue() != null, Status.Success);
            }
            @Override
            public void onCancelled(DatabaseError firebaseError) {
                listener.onResult(false, Status.Failed);
            }
        });
    }

    public static ValueEventListener monitorUserLocation(String targetUserId, final FirebaseListener<LocationModel> listener){
        return getSingleDataMonitor(getTable(TableName.locations).child(targetUserId), listener);
    }

    public static ValueEventListener monitorUserPing(String myUserId, String targetUserId, final FirebaseListener listener){
        return getSingleDataMonitor(getTable(TableName.pings).child(targetUserId).child(myUserId), listener);
    }

    public static void deleteMonitorUserLocation(String targetUserId, ValueEventListener valueEventListener){
        getTable(TableName.locations).child(targetUserId).removeEventListener(valueEventListener);
    }

    public static void deleteMonitorUserPing(String myUserId, String targetUserId, ValueEventListener valueEventListener){
        getTable(TableName.pings).child(targetUserId).child(myUserId).removeEventListener(valueEventListener);
    }

    private static void getData(final DatabaseReference ref, final FirebaseListener listener){
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

            }
            @Override
            public void onCancelled(DatabaseError firebaseError) {
                listener.onResult(null, Status.Failed);
            }
        });
    }

    private static void getSingleData(DatabaseReference ref, final FirebaseListener listener){
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(listener != null) listener.onResult(snapshot.getValue(listener.getType()), Status.Success);
                }
                else{
                    if(listener != null) listener.onResult(null, Status.Success);
                }

            }
            @Override
            public void onCancelled(DatabaseError firebaseError) {
                if(listener != null) listener.onResult(null, Status.Failed);
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
        products
    }

}
