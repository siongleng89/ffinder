package com.ffinder.android.absint.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.ffinder.android.R;
import com.ffinder.android.helpers.AddFriendReminder;
import com.ffinder.android.helpers.NotificationShower;
import com.ffinder.android.models.MyModel;

/**
 * Created by sionglengho on 5/12/16.
 */
public class AddFriendReminderBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        MyModel myModel = new MyModel(context);
        myModel.loadAllFriendModels();

        if(myModel.getNonSelfFriendModelsCount() == 0){
            String title = context.getString(R.string.app_name);
            String content = context.getString(R.string.notification_remember_to_add_friend_msg);

            NotificationShower.show(context,
                    NotificationShower.RememberToAddFriendNotificationId, title, content, true);

        }
        else{
            AddFriendReminder.disable(context);
        }


    }
}
