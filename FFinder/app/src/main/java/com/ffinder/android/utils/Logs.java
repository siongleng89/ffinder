package com.ffinder.android.utils;

import android.content.Intent;
import com.ffinder.android.BuildConfig;
import com.ffinder.android.helpers.FirebaseDB;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by SiongLeng on 2/9/2016.
 */
public class Logs {

    public static void show(Object data){
        if(BuildConfig.DEBUG_MODE) System.out.println(data);
    }

    public static void registerEventCatchingIFDebug(){
        if(BuildConfig.DEBUG_MODE){
            Thread.setDefaultUncaughtExceptionHandler (new Thread.UncaughtExceptionHandler()
            {
                @Override
                public void uncaughtException (Thread thread, Throwable e)
                {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    System.out.println(sw.toString());
                    FirebaseDB.saveLog(sw.toString());
                    System.exit(1);
                }
            });
        }
    }

}
