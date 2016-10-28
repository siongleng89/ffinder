package com.ffinder.android.enums;

import com.ffinder.android.helpers.Strings;

/**
 * Created by SiongLeng on 19/9/2016.
 */
public enum PhoneBrand {

    Xiaomi, Huawei, Sony, UnknownPhoneBrand;

    public static PhoneBrand convertStringToPhoneBrand(String input){
        if(Strings.isEmpty(input)) return UnknownPhoneBrand;
        else{
            for (PhoneBrand c : PhoneBrand.values()) {
                if (c.name().toLowerCase().equals(input.toLowerCase())) {
                    return c;
                }
            }
        }
        return UnknownPhoneBrand;
    }


}
