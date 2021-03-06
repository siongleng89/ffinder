package com.ffinder.android.helpers;

import android.support.v4.util.Pair;
import com.ffinder.android.absint.databases.FirebaseListener;
import com.ffinder.android.enums.FCMMessageType;
import com.ffinder.android.enums.Status;
import com.ffinder.android.models.UserModel;
import com.ffinder.android.statics.Constants;
import com.ffinder.android.statics.Vars;
import org.json.JSONObject;

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

    //null msgId will be auto generated
    public static void sendWithUserId(final String myUserId, final String targetUserId,
                                      final FCMMessageType fcmMessageType,
                                      final int ttl, final String msgId,
                                      final boolean retryIfError,
                                      final Pair<String, String>... appendsToMapPairs){
        FirebaseDB.getUserData(targetUserId, new FirebaseListener<UserModel>(UserModel.class) {
            @Override
            public void onResult(UserModel userModel, Status status) {
                if(status == Status.Success && userModel != null){
                    sendFcm(myUserId, userModel.getToken(), fcmMessageType, ttl, msgId,
                            userModel.getPlatform(), retryIfError, 0,
                            appendsToMapPairs);
                }
            }
        });
    }

    public static void sendWithToken(final String myUserId, final String targetToken,
                                      final FCMMessageType fcmMessageType,
                                      final int ttl, String msgId,
                                     final String targetPlatform,
                                     final boolean retryIfError,
                                     final Pair<String, String>... appendsToMapPairs){
        sendFcm(myUserId, targetToken, fcmMessageType, ttl, msgId, targetPlatform,
                retryIfError, 0, appendsToMapPairs);
    }

    public static void sendToTopic(final String myUserId, final String topicId,
                                   final FCMMessageType fcmMessageType,
                                   final int ttl, String msgId,
                                   final Pair<String, String>... appendsToMapPairs){

        //we use configuration of android even sending to ios, since sendToTopic
        //normally is not urgent msg (eg. auto notification)
        sendFcm(myUserId, "/topics/" + topicId, fcmMessageType, ttl, msgId,
                "android", false, 0,
                appendsToMapPairs);
    }

    private static void sendFcm(final String myUserId, final String toToken,
                                final FCMMessageType fcmMessageType, final int ttl,
                                final String msgId,
                                final String toPlatform,
                                final boolean retryIfError,
                                final int count,
                                final Pair<String, String>... appendsToMapPairs){
        Threadings.runInBackground(new Runnable() {
            @Override
            public void run() {
                boolean succeed = false;

                try {
                    String url = "https://fcm.googleapis.com/fcm/send";
                    URL obj = null;
                    obj = new URL(url);

                    String finalMsgId = msgId;

                    if(Strings.isEmpty(msgId)){
                        finalMsgId = Strings.generateUniqueRandomKey(30);
                    }

                    HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

                    //add reuqest header
                    con.setRequestMethod("POST");
                    con.setRequestProperty("Content-Type", "application/json");
                    con.setRequestProperty("Authorization", "key=" + Constants.FcmKey);

                    HashMap<String, String> dataMap = new HashMap();
                    dataMap.put("action", fcmMessageType.name());
                    dataMap.put("senderId", myUserId);
                    dataMap.put("fromPlatform", "android");
                    dataMap.put("messageId", finalMsgId);

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
                    hashMap.put("content_available", true);

                    //sending those to android device will cause empty system tray notf bugs
                    if(!Strings.isEmpty(toPlatform) && toPlatform.toLowerCase().equals("ios")){
                        HashMap<String, Object> notificationMap = new HashMap();
                        notificationMap.put("badge", "0");
                        notificationMap.put("alert", "");
                        notificationMap.put("sound", "");
                        hashMap.put("notification", notificationMap);
                    }


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

                    JSONObject jsonObject = new JSONObject(response.toString());
                    if(jsonObject.has("success")){
                        if(jsonObject.getInt("success") == 1){
                            succeed = true;
                        }
                    }


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

                if(!succeed && retryIfError){
                    if(count < 3){
                        Threadings.sleep(1000 + count * 4000);
                        sendFcm(myUserId, toToken, fcmMessageType, ttl, msgId,
                                toPlatform, retryIfError, count + 1, appendsToMapPairs);
                    }
                }

            }
        });
    }


}
