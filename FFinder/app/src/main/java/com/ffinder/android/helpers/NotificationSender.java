package com.ffinder.android.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ffinder.android.enums.FCMMessageType;
import com.ffinder.android.statics.Vars;
import com.ffinder.android.utils.Logs;
import com.onesignal.OneSignal;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by SiongLeng on 5/9/2016.
 */
public class NotificationSender {

    public static void send(String myUserId, String targetUserId,
                      FCMMessageType fcmMessageType,
                      int ttl){
        sendOneSignal(myUserId, targetUserId, fcmMessageType, ttl);
    }

    private static void sendOneSignal(String myUserId, String targetUserId, final FCMMessageType fcmMessageType, final int ttl){
        try {

            HashMap<String, String> contentMap = new HashMap();
            contentMap.put("en", "msg");
            HashMap<String, String> dataMap = new HashMap();
            dataMap.put("action", fcmMessageType.name());
            dataMap.put("senderId", myUserId);

            String[] playerIds = new String[1];
            playerIds[0] = targetUserId;

            HashMap<String, Object> sendingMap = new HashMap();
            sendingMap.put("contents", contentMap);
            sendingMap.put("data", dataMap);
            sendingMap.put("include_player_ids", playerIds);
            sendingMap.put("priority", 10);
            sendingMap.put("ttl", ttl);
            String json = Vars.getObjectMapper().writeValueAsString(sendingMap);
            Logs.show("One signal sending json: "  + json);

            OneSignal.postNotification(json, new OneSignal.PostNotificationResponseHandler() {
                @Override
                public void onSuccess(JSONObject response) {
                    Logs.show("Successfully sent one signal");
                }

                @Override
                public void onFailure(JSONObject response) {
                    Logs.show("Failed to sent one signal");
                }
            });
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }


//    private static void sendFcm(final String myUserId, final String toToken, final FCMMessageType fcmMessageType, final int ttl){
//        Threadings.runInBackground(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    String url = "https://fcm.googleapis.com/fcm/send";
//                    URL obj = null;
//                    obj = new URL(url);
//
//                    HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
//
//                    //add reuqest header
//                    con.setRequestMethod("POST");
//                    con.setRequestProperty("Content-Type", "application/json");
//                    con.setRequestProperty("Authorization", "key=" + Constants.FcmKey);
//
//                    HashMap<String, String> dataMap = new HashMap();
//                    dataMap.put("action", fcmMessageType.name());
//                    dataMap.put("senderId", myUserId);
//                    dataMap.put("fromPlatform", "Firebase");
//
//                    HashMap<String, Object> hashMap = new HashMap();
//                    hashMap.put("data", dataMap);
//                    hashMap.put("to", toToken);
//                    hashMap.put("priority", "high");
//                    hashMap.put("delay_while_idle", false);
//                    hashMap.put("time_to_live", ttl);
//
//                    String json = Vars.getObjectMapper().writeValueAsString(hashMap);
//
//                    // Send post request
//                    con.setDoOutput(true);
//                    DataOutputStream wr = new DataOutputStream(con.getOutputStream());
//                    wr.writeBytes(json);
//                    wr.flush();
//                    wr.close();
//
//                    int responseCode = con.getResponseCode();
//
//                    Logs.show("sent FCM post response code :" + responseCode);
//
//                    BufferedReader in = new BufferedReader(
//                            new InputStreamReader(con.getInputStream()));
//                    String inputLine;
//                    StringBuffer response = new StringBuffer();
//
//                    while ((inputLine = in.readLine()) != null) {
//                        response.append(inputLine);
//                    }
//                    in.close();
//
//                    Logs.show("FCM response :" + response);
//
//
//                } catch (MalformedURLException e) {
//                    e.printStackTrace();
//                } catch (ProtocolException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                catch (Exception e){
//                    e.printStackTrace();
//                }
//            }
//        });
//    }


}
