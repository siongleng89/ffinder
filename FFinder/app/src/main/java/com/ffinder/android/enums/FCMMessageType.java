package com.ffinder.android.enums;

import com.ffinder.android.utils.Strings;

/**
 * Created by SiongLeng on 2/9/2016.
 */
public enum  FCMMessageType {

    UpdateLocation, UserLocated, NotifyRememberToAddFriend, FriendsAdded,
    IsAliveMsg, Nothing;

    public static FCMMessageType convertStringToFCMMessageType(String input){
        if(Strings.isEmpty(input)) return Nothing;
        else{
            for (FCMMessageType c : FCMMessageType.values()) {
                if (c.name().equals(input)) {
                    return FCMMessageType.valueOf(input);
                }
            }
        }
        return Nothing;
    }

}


