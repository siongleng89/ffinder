package com.ffinder.android.helpers;

import android.content.Context;
import android.content.Intent;
import android.support.v4.util.Pair;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ffinder.android.R;
import com.ffinder.android.enums.*;
import com.ffinder.android.extensions.LimitedArrayList;
import com.ffinder.android.models.FriendModel;
import com.ffinder.android.models.LocationModel;
import com.ffinder.android.models.MyModel;
import com.ffinder.android.statics.Vars;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by SiongLeng on 4/9/2016.
 */
public class NotificationConsumer {

    private Context context;

    public NotificationConsumer(Context context) {
        this.context = context;
    }

    public void consume(final Map<String, String> map){
        if(map.containsKey("action")){

            final FCMMessageType messageType = FCMMessageType.convertStringToFCMMessageType(map.get("action"));

            //my friend has added me, broadcast refresh my friend list event
            if(messageType == FCMMessageType.FriendsAdded){
                String username = map.get("username");
                final String senderId = map.get("senderId");


                MyModel myModel = new MyModel(context);
                myModel.load();
                myModel.loadAllFriendModels();

                if(myModel.getFriendModelById(senderId) == null){
                    FriendModel newFriendModel = new FriendModel();
                    newFriendModel.setUserId(senderId);
                    newFriendModel.setName(Strings.pickNonEmpty(username, ""));

                    myModel.addFriendModel(newFriendModel);
                    myModel.sortFriendModels();
                    newFriendModel.save(context);
                    myModel.commitFriendUserIds();

                    showFriendAddedNotification(senderId, username);
                    BroadcasterHelper.broadcast(context, BroadcastEvent.RefreshNewlyAddedFriend,
                            new Pair<String, String>("userId", senderId));
                }

            }

            //someone has asked for my location, update location
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

                String senderId = map.get("senderId");
                String senderToken = map.get("senderToken");
                String fromPlatform = map.get("fromPlatform");

                Intent intent = new Intent();
                intent.setAction("com.ffinder.android.GET_LOCATION");
                intent.putExtra("senderId", senderId);
                intent.putExtra("senderToken", senderToken);
                intent.putExtra("fromPlatform", fromPlatform);
                context.sendBroadcast(intent);
            }

            //auto user located notification received,
            // broadcast refresh single friend model to trigger update design
            else if(messageType == FCMMessageType.UserLocated){
                final String senderId = map.get("senderId");
                String latitude = map.get("latitude");
                final String longitude = map.get("longitude");
                final String isAutoNotification = map.get("isAutoNotification");
                MyModel myModel = new MyModel(context);

                myModel.loadFriend(senderId);
                final FriendModel friendModel = myModel.getFriendModelById(senderId);

                if(friendModel != null){
                    final LocationModel locationModel = new LocationModel();
                    locationModel.setLatitude(latitude);
                    locationModel.setLongitude(longitude);
                    locationModel.setTimestampLastUpdatedLong(System.currentTimeMillis());
                    friendModel.setSearchResult(SearchResult.Normal);
                    friendModel.setSearchStatus(SearchStatus.End);

                    locationModel.geodecodeCoordinatesIfNeeded(context, new Runnable() {
                        @Override
                        public void run() {
                            friendModel.setLastLocationModel(locationModel);
                            friendModel.save(context);

                            BroadcasterHelper.broadcast(context, BroadcastEvent.RefreshFriend,
                                    new Pair<String, String>("userId", senderId));

                            BroadcasterHelper.broadcast(context, BroadcastEvent.SearchSuccess,
                                    new Pair<String, String>("userId", senderId));

                            //only show notification on user system tray if it is from auto notification
                            if(isAutoNotification.equals("1")){
                                showLocatedNotification(friendModel.getUserId(), friendModel.getName());
                                FirebaseMessaging.getInstance().unsubscribeFromTopic(senderId);
                            }


                        }
                    });
                }
            }

            //user is alive msg
            //broadcast refresh single friend model to trigger update design
            else if(messageType == FCMMessageType.IsAliveMsg){
                String senderId = map.get("senderId");
                boolean locationDisabled = false;
                if(map.containsKey("locationDisabled")){
                    locationDisabled = map.get("locationDisabled").equals("1");
                }

                //must save to preference to preserve state when apps is in background
                MyModel myModel = new MyModel(context);
                myModel.loadFriend(senderId);
                final FriendModel friendModel = myModel.getFriendModelById(senderId);
                if(friendModel != null){
                    //if not waiting user respond, this msg is useless, should discarded
                    if(friendModel.getSearchStatus() == SearchStatus.WaitingUserRespond){
                        //location is disabled, straight stop the process
                        if(locationDisabled){
                            friendModel.setSearchStatus(SearchStatus.End);
                            friendModel.setSearchResult(SearchResult.ErrorLocationDisabled);
                        }
                        else{
                            friendModel.setSearchStatus(SearchStatus.WaitingUserLocation);
                        }
                        friendModel.save(context);
                        BroadcasterHelper.broadcast(context, BroadcastEvent.RefreshFriend,
                                new Pair<String, String>("userId", senderId));
                    }
                }

            }

            //push notification received for add friend reminder
            else if(messageType == FCMMessageType.NotifyRememberToAddFriend){
                MyModel myModel = new MyModel(context);
                myModel.loadAllFriendModels();
                if(myModel.getNonSelfFriendModelsCount() == 0){
                    showRememberToAddFriendNotification();
                }
            }

        }
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
        String content = String.format(context.getString(R.string.notification_x_has_been_located_msg), username);

        if(currentIdsList.size() > 1){
            content = String.format(context.getString(R.string.notification_x_users_located_msg), currentIdsList.size());
        }

        NotificationShower.show(context,
                NotificationShower.UserLocatedNotificationId, title, content, false);
    }

    private void showRememberToAddFriendNotification(){
        String title = context.getString(R.string.app_name);
        String content = context.getString(R.string.notification_remember_to_add_friend_msg);

        NotificationShower.show(context,
                NotificationShower.RememberToAddFriendNotificationId, title, content, true);
    }

    private void showFriendAddedNotification(String senderId, String senderName){
        String title = context.getString(R.string.app_name);
        String content = String.format(context.getString(R.string.notification_x_added_you), senderName);

        NotificationShower.show(context, NotificationShower.FriendsAddedNotificationId, title, content, true);
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
