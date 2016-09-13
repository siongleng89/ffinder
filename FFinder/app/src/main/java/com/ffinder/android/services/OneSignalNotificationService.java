package com.ffinder.android.services;

import com.ffinder.android.enums.FCMMessageType;
import com.ffinder.android.helpers.NotificationConsumer;
import com.ffinder.android.helpers.RequestLocationHandler;
import com.ffinder.android.utils.Logs;
import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationReceivedResult;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by SiongLeng on 4/9/2016.
 */
public class OneSignalNotificationService extends NotificationExtenderService {
    @Override
    protected boolean onNotificationProcessing(OSNotificationReceivedResult notification) {
        Logs.show("One signal notification received");
        JSONObject jsonObject =  notification.payload.additionalData;
        Logs.show("One signal Data is: " + jsonObject.toString());

        new NotificationConsumer(this).consume(jsonObject.toString());

        return true;
    }
}
