package com.ffinder.android.helpers;

import com.ffinder.android.models.OnlineRequest;
import com.ffinder.android.utils.Logs;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by SiongLeng on 19/9/2016.
 */
public class FirebaseOnlineTracker {
    private static FirebaseOnlineTracker INSTANCE;

    private ScheduledThreadPoolExecutor threadPoolExecutor;
    private Set<OnlineRequest> requests = new HashSet<>();
    private ScheduledFuture<Void> goingOffline;
    private boolean online;

    private FirebaseOnlineTracker(ScheduledThreadPoolExecutor spe) {
        this.threadPoolExecutor = spe;
    }

    public static void init(ScheduledThreadPoolExecutor spe) {
        if(INSTANCE == null) {
            INSTANCE = new FirebaseOnlineTracker(spe);
            Logs.show("FirebaseOnlineTracker initialized");
        } else {
            Logs.show("FirebaseOnlineTracker already initialized");
        }
    }

    public static FirebaseOnlineTracker get() {
        if(INSTANCE == null){
            init(new ScheduledThreadPoolExecutor(5));
        }
        return INSTANCE;
    }

    public synchronized OnlineRequest requestOnline(String reason) {
        OnlineRequest request =  new OnlineRequest(reason);

        if(requests.isEmpty()) {
            if(goingOffline!=null) {
                goingOffline.cancel(true);
                Logs.show("Cancelling going offline due to " + reason);
                goingOffline = null;
            }

            FirebaseDatabase.getInstance().goOnline();
            boolean wasOnline = online;
            online = true;
            if(wasOnline)
                Logs.show("Asking firebase to go online for " + reason);
        } else {
            Logs.show("Increasing reference count due to %s. Already online " + reason);
        }

        requests.add(request);
        return request;
    }

    public synchronized void releaseOnline(OnlineRequest request) {
        boolean removed = requests.remove(request);
        if(!removed)
            Logs.show("OnlineRequest not already present");

        if(requests.isEmpty()) {
            if(goingOffline==null) {
                Logs.show("No active online requests. Will schedule go offline in 1 minute due to " + request.getReason());
                goingOffline = threadPoolExecutor.schedule(goOfflineCallable, 1, TimeUnit.MINUTES);
            } else {
                Logs.show("No need to schedule offline. There is already a scheduled job. Request due to " + request.getReason());
            }
        }
    }

    private final Callable<Void> goOfflineCallable = new Callable<Void>() {
        @Override
        public Void call() throws Exception {
            synchronized (FirebaseOnlineTracker.this) {
                Logs.show("Asking firebase to go offline");
                FirebaseDatabase.getInstance().goOffline();
                online = false;
                goingOffline = null;
                return null;
            }
        }
    };


}
