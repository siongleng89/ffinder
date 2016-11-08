package com.ffinder.android.enums;

import android.content.Context;
import com.ffinder.android.R;

/**
 * Created by SiongLeng on 3/9/2016.
 */
public enum SearchResult {
    Normal,
    ErrorNoLink, ErrorTimeoutUnknownReason,
    ErrorLocationDisabled,
    ErrorTimeoutNoConnection, ErrorUserBlocked;


    public String getMessage(Context context){
        switch (this){
            case ErrorNoLink:
                return context.getString(R.string.error_no_link_msg);
            case ErrorTimeoutUnknownReason:
                return context.getString(R.string.error_timeout_msg);
            case ErrorLocationDisabled:
                return context.getString(R.string.error_location_disabled_msg);
            case ErrorTimeoutNoConnection:
                return context.getString(R.string.error_timeout_no_connection_msg);
            case ErrorUserBlocked:
                return context.getString(R.string.error_user_blocked_msg);
        }


        return this.name();
    }

    public boolean isError(){
        return (this.name().startsWith("Error"));
    }

    public boolean errorTriggeredAutoNotification(){
        return this == ErrorTimeoutUnknownReason;
    }

}
