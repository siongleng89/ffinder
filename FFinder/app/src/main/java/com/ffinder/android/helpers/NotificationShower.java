package com.ffinder.android.helpers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import com.ffinder.android.ActivityMain;
import com.ffinder.android.R;

import static android.support.v4.app.NotificationCompat.DEFAULT_LIGHTS;
import static android.support.v4.app.NotificationCompat.DEFAULT_SOUND;
import static android.support.v4.app.NotificationCompat.DEFAULT_VIBRATE;

/**
 * Created by sionglengho on 7/10/16.
 */
public class NotificationShower {

    public static int FriendsAddedNotificationId = 10000;
    public static int UserLocatedNotificationId = 20000;
    public static int RememberToAddFriendNotificationId = 30000;


    public static void show(Context context, int notificationId,
                            String title, String content, boolean useBigText){
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_stat_notification)
                        .setContentTitle(title)
                        .setContentText(content);

        if(useBigText){
            builder.setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(content));
        }

        Intent targetIntent = new Intent(context, ActivityMain.class);
        targetIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent intent = PendingIntent.getActivity(context, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(intent);
        builder.setAutoCancel(true);
        builder.setDefaults(DEFAULT_SOUND | DEFAULT_VIBRATE | DEFAULT_LIGHTS);
        NotificationManager nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        nManager.notify(notificationId, builder.build());
    }



}
