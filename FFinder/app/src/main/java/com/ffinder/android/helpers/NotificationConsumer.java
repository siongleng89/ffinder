package com.ffinder.android.helpers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ffinder.android.ActivityMain;
import com.ffinder.android.R;
import com.ffinder.android.absint.databases.FirebaseListener;
import com.ffinder.android.enums.FCMMessageType;
import com.ffinder.android.enums.PreferenceType;
import com.ffinder.android.enums.SearchResult;
import com.ffinder.android.enums.Status;
import com.ffinder.android.extentions.LimitedArrayList;
import com.ffinder.android.models.FriendModel;
import com.ffinder.android.models.LocationModel;
import com.ffinder.android.models.MyModel;
import com.ffinder.android.statics.Vars;
import com.ffinder.android.utils.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.support.v4.app.NotificationCompat.DEFAULT_LIGHTS;
import static android.support.v4.app.NotificationCompat.DEFAULT_SOUND;
import static android.support.v4.app.NotificationCompat.DEFAULT_VIBRATE;

/**
 * Created by SiongLeng on 4/9/2016.
 */
public class NotificationConsumer {

    private Context context;

    public NotificationConsumer(Context context) {
        this.context = context;
    }

    public void consume(String json){
        try {
            if(Strings.isEmpty(json)){
                consume(new HashMap<String, String>());
            }
            else{
                consume(Vars.getObjectMapper().readValue(json, Map.class));
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void consume(final Map<String, String> map){
        if(checkContainEssentialField(map)){
            final String senderId = map.get("senderId").toString();
            final FCMMessageType messageType = FCMMessageType.valueOf(map.get("action").toString());
            final MyModel myModel = new MyModel(context);
            myModel.loginFirebase(0, new RunnableArgs<Boolean>() {
                @Override
                public void run() {
                    if(this.getFirstArg()){
                        if(messageType == FCMMessageType.FriendsAdded && map.containsKey("username")){
                            String username = map.get("username");
                            showFriendAddedNotification(senderId, username);
                            Intent i = new Intent("REFRESH_FRIENDLIST");
                            context.sendBroadcast(i);
                        }
                        else if(messageType == FCMMessageType.UpdateLocation){
                            if(map.containsKey("messageId")){
                                String messageId = map.get("messageId");
                                if(!Strings.isEmpty(messageId)){
                                    LimitedArrayList<String> processedIds = getProcessedIdList();
                                    if(processedIds.contains(messageId)){
                                        Logs.show(messageId + " already processed, returning....");
                                        return;
                                    }
                                    processedIds.add(messageId);
                                    saveProcessedIdList(processedIds);
                                    Logs.show(messageId + " now is being processed");
                                }
                            }

                            RequestLocationHandler requestLocationHandler = new RequestLocationHandler(context, senderId, myModel);
                            requestLocationHandler.run();
                        }
                        else if(messageType == FCMMessageType.UserLocated){
                            FirebaseDB.getUserLocation(senderId, new FirebaseListener<LocationModel>(LocationModel.class) {
                                @Override
                                public void onResult(final LocationModel result, Status status) {
                                    if(status == Status.Success && result != null){

                                        myModel.loadFriend(senderId);
                                        final FriendModel friendModel = myModel.getFriendModelById(senderId);
                                        if(friendModel != null){
                                            AndroidUtils.geoDecode(context,
                                                    result.getLatitude(), result.getLongitude(), new RunnableArgs<String>() {
                                                        @Override
                                                        public void run() {
                                                            result.setAddress(this.getFirstArg());
                                                            friendModel.setSearchResult(SearchResult.Normal);
                                                            friendModel.setLastLocationModel(result);
                                                            friendModel.save(context);
                                                            Intent i = new Intent("REFRESH_FRIEND");
                                                            i.putExtra("userId", senderId);
                                                            context.sendBroadcast(i);
                                                            showLocatedNotification(friendModel.getUserId(), friendModel.getName());
                                                        }
                                                    });
                                        }
                                    }
                                }
                            });
                        }
                    }
                }
            });
        }
        else{
            if(map.containsKey("action")){
                final FCMMessageType messageType = FCMMessageType.valueOf(map.get("action").toString());
                final MyModel myModel = new MyModel(context);
                if(messageType == FCMMessageType.NotifyRememberToAddFriend){
                    myModel.loadAllFriendModels();
                    if(myModel.getNonSelfFriendModelsCount() == 0){
                        showRememberToAddFriendNotification();
                    }
                }
            }
        }

    }

    private boolean checkContainEssentialField(Map<String, String> map){
        return map.containsKey("action") && map.containsKey("senderId");
    }


    private void showLocatedNotification(String userId, String username){
        String currentIds = PreferenceUtils.get(context, PreferenceType.AutoNotifiedReceivedIds);
        ArrayList<String> currentIdsList = new ArrayList();
        if(!Strings.isEmpty(currentIds)){
            currentIdsList = Strings.split(currentIds, ",");
        }

        if(!currentIdsList.contains(userId)){
            currentIdsList.add(userId);
        }

        PreferenceUtils.put(context, PreferenceType.AutoNotifiedReceivedIds, Strings.joinArr(currentIdsList, ","));

        String title = context.getString(R.string.notification_user_located_title);
        String msg = String.format(context.getString(R.string.notification_x_has_been_located_msg), username);

        if(currentIdsList.size() > 1){
            msg = String.format(context.getString(R.string.notification_x_users_located_msg), currentIdsList.size());
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_stat_notification)
                        .setContentTitle(title)
                        .setContentText(msg);
        int NOTIFICATION_ID = 12345;

        Intent targetIntent = new Intent(context, ActivityMain.class);
        targetIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent intent = PendingIntent.getActivity(context, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(intent);
        builder.setAutoCancel(true);
        builder.setDefaults(DEFAULT_SOUND | DEFAULT_VIBRATE | DEFAULT_LIGHTS);
        NotificationManager nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        //nManager.cancel(NOTIFICATION_ID);
        nManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void showRememberToAddFriendNotification(){
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_stat_notification)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setContentText(context.getString(R.string.notification_remember_to_add_friend_msg))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(context.getString(R.string.notification_remember_to_add_friend_msg)));

        int NOTIFICATION_ID = 99999;

        Intent targetIntent = new Intent(context, ActivityMain.class);
        targetIntent.putExtra("shareKey", "1");

        targetIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent intent = PendingIntent.getActivity(context, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(intent);
        builder.setAutoCancel(true);
        builder.setDefaults(DEFAULT_SOUND | DEFAULT_VIBRATE | DEFAULT_LIGHTS);
        NotificationManager nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        nManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void showFriendAddedNotification(String senderId, String senderName){
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_stat_notification)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setContentText(String.format(context.getString(R.string.notification_x_added_you), senderName))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(String.format(context.getString(R.string.notification_x_added_you), senderName)));

        int NOTIFICATION_ID = 99998;

        Intent targetIntent = new Intent(context, ActivityMain.class);
        targetIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent intent = PendingIntent.getActivity(context, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(intent);
        builder.setAutoCancel(true);
        builder.setDefaults(DEFAULT_SOUND | DEFAULT_VIBRATE | DEFAULT_LIGHTS);
        NotificationManager nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        nManager.notify(NOTIFICATION_ID, builder.build());
    }

    private LimitedArrayList<String> getProcessedIdList() {
        ObjectMapper objectMapper = Vars.getObjectMapper();
        String json = PreferenceUtils.get(context, PreferenceType.ProcessedMessageIds);
        if(Strings.isEmpty(json)) return new LimitedArrayList(5);
        else{
            try {
                LimitedArrayList<String> processedIdList = objectMapper.readValue(json, LimitedArrayList.class);
                return processedIdList;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new LimitedArrayList(5);
    }

    private void saveProcessedIdList(LimitedArrayList<String> limitedArrayList) {
        ObjectMapper objectMapper = Vars.getObjectMapper();
        String json = null;
        try {
            json = objectMapper.writeValueAsString(limitedArrayList);
            Logs.show("Saving processedMessageIds: " + json);
            PreferenceUtils.put(context, PreferenceType.ProcessedMessageIds, json);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

}
