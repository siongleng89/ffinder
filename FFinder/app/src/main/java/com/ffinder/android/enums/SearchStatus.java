package com.ffinder.android.enums;

import android.content.Context;
import com.ffinder.android.R;

/**
 * Created by SiongLeng on 1/9/2016.
 */
public enum SearchStatus {
    Starting,
    CheckingData,
    WaitingUserRespond,
    WaitingUserLocation,
    Geocoding,
    End;

    public String getMessage(Context context){
        switch (this){
            case Starting:
                return context.getString(R.string.search_status_starting_msg);
            case CheckingData:
                return context.getString(R.string.search_status_checking_data_msg);
            case WaitingUserRespond:
                return context.getString(R.string.search_status_waiting_user_respond_msg);
            case WaitingUserLocation:
                return context.getString(R.string.search_status_waiting_user_location_msg);
            case Geocoding:
                return context.getString(R.string.search_status_geodecoding_msg);
            case End:
                return null;
        }

        return this.name();
    }


}
