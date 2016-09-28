package com.ffinder.android.helpers;

import android.util.Pair;
import com.ffinder.android.absint.databases.FirebaseListener;
import com.ffinder.android.enums.FCMMessageType;
import com.ffinder.android.enums.Status;
import com.ffinder.android.statics.Constants;
import com.ffinder.android.statics.Vars;
import com.ffinder.android.utils.Logs;
import com.ffinder.android.utils.Strings;
import com.ffinder.android.utils.Threadings;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by SiongLeng on 5/9/2016.
 */
public class NotificationSender {

    public static final int TTL_LONG = 1814399;
    public static final int TTL_INSTANT = 0;

    public static void send(final String myUserId, final String targetUserId,
                            final FCMMessageType fcmMessageType,
                            final int ttl, final Pair<String, String>... appendsToMapPairs){
        FirebaseDB.getUserToken(targetUserId, new FirebaseListener<String>(String.class) {
            @Override
            public void onResult(String token, Status status) {
                if(status == Status.Success && !Strings.isEmpty(token)){
                    sendFcm(myUserId, token, fcmMessageType, ttl, appendsToMapPairs);
                }
            }
        });


    }

//    private static void sendOneSignal(String myUserId, String targetUserId, final FCMMessageType fcmMessageType, final int ttl){
//        try {
//
//            HashMap<String, String> contentMap = new HashMap();
//            contentMap.put("en", "msg");
//            HashMap<String, String> dataMap = new HashMap();
//            dataMap.put("action", fcmMessageType.name());
//            dataMap.put("senderId", myUserId);
//
//            String[] playerIds = new String[1];
//            playerIds[0] = targetUserId;
//
//            HashMap<String, Object> sendingMap = new HashMap();
//            sendingMap.put("contents", contentMap);
//            sendingMap.put("data", dataMap);
//            sendingMap.put("include_player_ids", playerIds);
//            sendingMap.put("priority", 10);
//            sendingMap.put("ttl", ttl);
//            String json = Vars.getObjectMapper().writeValueAsString(sendingMap);
//            Logs.show("One signal sending json: "  + json);
//
//            OneSignal.postNotification(json, new OneSignal.PostNotificationResponseHandler() {
//                @Override
//                public void onSuccess(JSONObject response) {
//                    Logs.show("Successfully sent one signal");
//                }
//
//                @Override
//                public void onFailure(JSONObject response) {
//                    Logs.show("Failed to sent one signal");
//                }
//            });
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }
//    }


    private static void sendFcm(final String myUserId, final String toToken, final FCMMessageType fcmMessageType, final int ttl,
                                final Pair<String, String>... appendsToMapPairs){
        Threadings.runInBackground(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = "https://fcm.googleapis.com/fcm/send";
                    URL obj = null;
                    obj = new URL(url);

                    HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

                    //add reuqest header
                    con.setRequestMethod("POST");
                    con.setRequestProperty("Content-Type", "application/json");
                    con.setRequestProperty("Authorization", "key=" + Constants.FcmKey);

                    HashMap<String, String> dataMap = new HashMap();
                    dataMap.put("action", fcmMessageType.name());
                    dataMap.put("senderId", myUserId);
                    dataMap.put("fromPlatform", "Firebase");
                    dataMap.put("messageId", Strings.generateUniqueRandomKey(30));

                    if(appendsToMapPairs != null){
                        for(Pair<String, String> pair: appendsToMapPairs){
                            dataMap.put(pair.first, pair.second);
                        }
                    }

                    HashMap<String, Object> hashMap = new HashMap();
                    hashMap.put("data", dataMap);
                    hashMap.put("to", toToken);
                    hashMap.put("priority", "high");
                    hashMap.put("delay_while_idle", false);
                    hashMap.put("time_to_live", ttl);

                    String json = Vars.getObjectMapper().writeValueAsString(hashMap);

                    // Send post request
                    con.setDoOutput(true);
                    DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                    wr.writeBytes(json);
                    wr.flush();
                    wr.close();

                    int responseCode = con.getResponseCode();

                    Logs.show("sent FCM post response code :" + responseCode);

                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    Logs.show("FCM response :" + response);


                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }


}
