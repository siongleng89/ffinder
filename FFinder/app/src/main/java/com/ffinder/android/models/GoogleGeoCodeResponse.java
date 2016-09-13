package com.ffinder.android.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ffinder.android.utils.Strings;

import java.util.ArrayList;

/**
 * Created by SiongLeng on 3/9/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleGeoCodeResponse {

    public String status ;
    public results[] results ;
    public GoogleGeoCodeResponse() {

    }

    @JsonIgnore
    public String getFirstAddress(){
        for(results result : results){
            return result.formatted_address;
        }
        return null;
    }

}

@JsonIgnoreProperties(ignoreUnknown = true)
class results{
    public String formatted_address ;
    public geometry geometry ;
    public String[] types;
    public address_component[] address_components;

    @JsonIgnore
    public String getFormattedAddress(){
        ArrayList<String> result = new ArrayList();
        for(address_component component : address_components){
            boolean skip = false;
            if(component.types.length > 0){
                for(String type : component.types){
                    if(type.equals("street_number")){
                        skip = true;
                    }
                }
            }

            if(!skip){
                result.add(component.long_name);
            }
        }
        return Strings.joinArr(result, ", ");
    }

}

@JsonIgnoreProperties(ignoreUnknown = true)
class geometry{
    public bounds bounds;
    public String location_type ;
    public location location;
    public bounds viewport;
}

@JsonIgnoreProperties(ignoreUnknown = true)
class bounds {

    public location northeast ;
    public location southwest ;
}

@JsonIgnoreProperties(ignoreUnknown = true)
class location{
    public String lat ;
    public String lng ;
}

@JsonIgnoreProperties(ignoreUnknown = true)
class address_component{
    public String long_name;
    public String short_name;
    public String[] types ;
}

