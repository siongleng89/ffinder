package com.ffinder.android.helpers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.ffinder.android.absint.receivers.AddFriendReminderBroadcastReceiver;

import java.util.Calendar;

/**
 * Created by sionglengho on 5/12/16.
 */
public class AddFriendReminder {

    public static void setup(Context context){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 19); // For 7 PM or 8 PM
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_YEAR, 1);

        PendingIntent pi = PendingIntent.getBroadcast(context, 0,
                new Intent(context, AddFriendReminderBroadcastReceiver.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                2 * AlarmManager.INTERVAL_DAY, pi);     //every two days
    }

    public static void disable(Context context){
        PendingIntent pi = PendingIntent.getBroadcast(context, 0,
                new Intent(context, AddFriendReminderBroadcastReceiver.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
    }




}
