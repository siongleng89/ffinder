package com.ffinder.android.utils;

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


    public static ThreadFragment delay(final long timeInMs, final Runnable toRun){
        final ThreadFragment delayFrag = new ThreadFragment();
        runInBackground(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(timeInMs);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                Gdx.app.postRunnable(new Runnable() {
//                    @Override
//                    public void run() {
//                        toRun.run();
//                        delayFrag.setFinished(true);
//                    }
//                });
            }
        });
        return delayFrag;
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

    public static void postRunnable(Runnable runnable){
        if(Thread.currentThread().getId() != mainTreadId){
          //  Gdx.app.postRunnable(runnable);
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


