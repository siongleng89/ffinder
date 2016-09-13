package com.ffinder.android.enums;

import android.content.Context;
import com.ffinder.android.R;

/**
 * Created by SiongLeng on 3/9/2016.
 */
public enum SearchResult {
    Normal,
    ErrorNoLink, ErrorUnknown, ErrorTimeoutUnknownReason, ErrorTimeoutLocationDisabled,
    ErrorTimeoutNoConnection;


    public String getMessage(Context context){
        switch (this){
            case ErrorNoLink:
                return context.getString(R.string.error_no_link_msg);
            case ErrorTimeoutUnknownReason:
                return context.getString(R.string.error_timeout_msg);
            case ErrorTimeoutLocationDisabled:
                return context.getString(R.string.error_timeout_msg);
            case ErrorTimeoutNoConnection:
                return context.getString(R.string.error_timeout_no_connection_msg);
        }


        return this.name();
    }

    public boolean isError(){
        return (this.name().startsWith("Error"));
    }

}
