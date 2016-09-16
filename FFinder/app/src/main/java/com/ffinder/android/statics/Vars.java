package com.ffinder.android.statics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ffinder.android.helpers.AdsMediation;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by SiongLeng on 29/8/2016.
 */
public class Vars {

    private static ObjectMapper objectMapper;

    public static ObjectMapper getObjectMapper() {
        if(objectMapper == null) objectMapper = new ObjectMapper();
        return objectMapper;
    }

    public static String pendingAddUserKey;

    public static void clearPendingAddUser(){
        pendingAddUserKey = null;
    }


}
