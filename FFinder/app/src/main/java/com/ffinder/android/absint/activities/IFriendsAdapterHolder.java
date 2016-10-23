package com.ffinder.android.absint.activities;

import com.ffinder.android.models.FriendModel;

/**
 * Created by sionglengho on 8/10/16.
 */
public interface IFriendsAdapterHolder {

    void updateFriendsListAdapter();

    void updateFriendsListAdapter(String friendId);

    void onAddFriend(FriendModel friendModel);

    void onDeleteFriend(FriendModel friendModel);

    void onEditFriend(FriendModel friendModel, String newName);

}
