package com.ffinder.android.models;

import android.content.Context;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ffinder.android.helpers.AndroidUtils;
import com.ffinder.android.helpers.RunnableArgs;
import com.ffinder.android.helpers.Strings;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.Map;

/**
 * Created by SiongLeng on 2/9/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LocationModel {

    private String latitude;
    private String longitude;
    private String address;
    private long timestampLastUpdated;

    public LocationModel() {
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    @JsonIgnore
    public Map<String, String> getTimestampLastUpdated() {
        return ServerValue.TIMESTAMP;
    }

    @JsonIgnore
    public void setTimestampLastUpdated(Long timestampLastUpdated) {
        this.timestampLastUpdated = timestampLastUpdated;
    }

    @Exclude
    public boolean hasCoordinates(){
        return !Strings.isEmpty(latitude) && !Strings.isEmpty(longitude);
    }

    @Exclude
    public String getAddress() {
        if(address == null) address = "";
        return address;
    }

    @Exclude
    public void setAddress(String address) {
        this.address = address;
    }

    @Exclude
    public long getTimestampLastUpdatedLong(){
        return timestampLastUpdated;
    }

    @Exclude
    public void setTimestampLastUpdatedLong(long timestampLastUpdated) {
        this.timestampLastUpdated = timestampLastUpdated;
    }


    public void geodecodeCoordinatesIfNeeded(Context context, final Runnable onFinish){
        if(!Strings.isEmpty(this.getLatitude()) && Strings.isEmpty(this.getAddress())){
            AndroidUtils.geoDecode(context, getLatitude(), getLongitude(), new RunnableArgs<String>() {
                @Override
                public void run() {
                    setAddress(this.getFirstArg());
                    onFinish.run();
                }
            });
        }
        else{
            onFinish.run();
        }
    }








}
