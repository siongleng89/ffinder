package com.ffinder.android.helpers;

import android.app.Activity;
import android.content.Context;

/**
 * Created by SiongLeng on 9/12/2015.
 */
public class Threadings {

    private static long mainTreadId;

    public static void setMainTreadId(){
        mainTreadId = Thread.currentThread().getId();
    }

    public static Thread runInBackground(Runnable toRun){
        Thread t = new Thread(toRun);
        t.start();
        return t;
    }


    public static void delay(final long timeInMs, final Activity activity, final Runnable toRun){
        runInBackground(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(timeInMs);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                postRunnable(activity, toRun);
            }
        });
    }

    public static void delayNoPost(final long timeInMs, final Runnable toRun){
        runInBackground(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(timeInMs);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                toRun.run();
            }
        });
    }

    public static void sleep(long time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void postRunnable(Activity context, Runnable runnable){
        if(Thread.currentThread().getId() != mainTreadId){
            context.runOnUiThread(runnable);
        }
        else{
            runnable.run();
        }
    }


    public static class ThreadFragment{

        boolean finished;

        public boolean isFinished() {
            return finished;
        }

        public void setFinished(boolean finished) {
            this.finished = finished;
        }
    }



}


