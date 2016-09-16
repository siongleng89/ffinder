package com.ffinder.android.utils;

import com.ffinder.android.BuildConfig;

/**
 * Created by SiongLeng on 2/9/2016.
 */
public class Logs {

    public static void show(Object data){
        if(BuildConfig.DEBUG_MODE) System.out.println(data);
    }

}
