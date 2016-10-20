package com.ffinder.android.controls;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import com.ffinder.android.R;
import com.ffinder.android.absint.activities.IFriendsAdapterHolder;
import com.ffinder.android.helpers.FirebaseDB;
import com.ffinder.android.models.FriendModel;
import com.ffinder.android.models.MyModel;

/**
 * Created by SiongLeng on 1/9/2016.
 */
public class ConfirmDeleteDialog {

    private Activity activity;
    private IFriendsAdapterHolder friendsAdapterHolder;
    private FriendModel friendModel;
    private MyModel myModel;

    public ConfirmDeleteDialog(Activity activity, IFriendsAdapterHolder friendsAdapterHolder,
                               FriendModel friendModel, MyModel myModel) {
        this.activity = activity;
        this.friendModel = friendModel;
        this.myModel = myModel;
        this.friendsAdapterHolder = friendsAdapterHolder;
    }

    public void show(){
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(String.format(activity.getString(R.string.delete_user_title), friendModel.getName()))
                .setMessage(activity.getString(R.string.confirm_delete_user_msg))
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        FirebaseDB.deleteLink(myModel.getUserId(), friendModel.getUserId(), null);
                        myModel.deleteFriend(friendModel);
                        friendModel.delete(activity);
                        myModel.commitFriendUserIds();
                        friendsAdapterHolder.updateFriendsListAdapter();
                        dialog.dismiss();
                    }})
                .setNegativeButton(R.string.no, null).show();
    }

}
