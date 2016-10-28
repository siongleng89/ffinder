package com.ffinder.android.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import com.ffinder.android.absint.databases.FirebaseListener;
import com.ffinder.android.absint.tasks.RequestLocationTaskFragListener;
import com.ffinder.android.enums.FCMMessageType;
import com.ffinder.android.enums.SearchResult;
import com.ffinder.android.enums.SearchStatus;
import com.ffinder.android.helpers.*;
import com.ffinder.android.models.FriendModel;
import com.ffinder.android.models.LocationModel;
import com.ffinder.android.models.MyModel;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by SiongLeng on 3/9/2016.
 */
public class RequestLocationTaskFrag extends Fragment {

    private RequestLocationTaskFragListener requestLocationTaskFragListener;
    private String userId, myToken, targetUserId;
    private SearchStatus currentStatus;
    private SearchResult currentResult;
    public static int timeoutSecs = 30;
    private RequestLocationTask requestLocationTask;

    public static RequestLocationTaskFrag newInstance(String userId, String myToken, String targetUserId) {
        RequestLocationTaskFrag f = new RequestLocationTaskFrag();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        args.putString("myToken", myToken);
        args.putString("targetUserId", targetUserId);
        f.setArguments(args);
        return f;
    }

    public RequestLocationTaskFrag() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        Bundle bundle = this.getArguments();

        userId = bundle.getString("userId");
        myToken = bundle.getString("myToken");
        targetUserId = bundle.getString("targetUserId");

        //unsubscribe auto notification topic
        FirebaseMessaging.getInstance().unsubscribeFromTopic(targetUserId);

        Threadings.runInBackground(new Runnable() {
            @Override
            public void run() {
                requestLocationTask = new RequestLocationTask(getContext());

                try {
                    if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
                        requestLocationTask
                                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, userId, myToken, targetUserId)
                                .get(timeoutSecs, TimeUnit.SECONDS);

                    }
                    else {
                        requestLocationTask.execute(userId, myToken, targetUserId)
                                .get(timeoutSecs, TimeUnit.SECONDS);
                    }


                } catch (InterruptedException e) {
                } catch (ExecutionException e) {
                } catch (TimeoutException e) {
                    requestLocationTask.taskTimeout();
                }
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void setRequestLocationTaskFragListener(RequestLocationTaskFragListener requestLocationTaskFragListener) {
        this.requestLocationTaskFragListener = requestLocationTaskFragListener;
    }

    //phase 0, 1, 2
    public void notifyTimeoutPhase(int newPhase){
        if(requestLocationTaskFragListener != null)
            requestLocationTaskFragListener.onTimeoutPhaseChanged(targetUserId, newPhase);
    }

    public void notifyProgress(SearchStatus searchStatus){
        currentStatus = searchStatus;
        if(requestLocationTaskFragListener != null) requestLocationTaskFragListener.onUpdateStatus(targetUserId, currentStatus);
    }

    public void notifyResult(LocationModel locationModel, SearchResult searchResult){
        currentResult = searchResult;
        if(requestLocationTaskFragListener != null)
            requestLocationTaskFragListener.onUpdateResult(targetUserId, locationModel, currentStatus, searchResult);

        terminate();
    }

    public void terminate(){
        //already gotten the result from push notification, can terminate the task now
        if(requestLocationTask != null) requestLocationTask.dispose();
    }


    public SearchStatus getCurrentStatus() {
        return currentStatus;
    }

    public SearchResult getCurrentResult() {
        return currentResult;
    }

    /////////////////////////////////////////////////////////////////////////
    //the task
    /////////////////////////////////////////////////////////////////////////

    public class RequestLocationTask extends AsyncTask<String, SearchStatus, LocationModel> {
        private boolean finish, disposed;
        private LocationModel resultLocationModel;
        private String targetUserId, myUserId, myToken;
        private Context context;
        private SearchStatus searchStatus;
        private SearchResult searchResult;
        private String msgId;

        public RequestLocationTask(Context context) {
            this.context = context;
            this.msgId = Strings.generateUniqueRandomKey(30);
        }

        @Override
        protected LocationModel doInBackground(String... params) {
            myUserId = params[0];
            myToken = params[1];
            targetUserId = params[2];

            publishProgress(SearchStatus.CheckingData);

            monitorTimeoutPhase();

            //check target user has me in his/her friend list
            checkHasLink(myUserId, targetUserId, new RunnableArgs<Boolean>() {
                @Override
                public void run() {
                    if(finish) return;

                    //has me in his/her friend list
                    if(this.getFirstArg()){

                        //check me is not blocked
                        checkMeIsBlocked(myUserId, targetUserId, new RunnableArgs<Boolean>() {
                            @Override
                            public void run() {
                                if(finish) return;

                                //not blocked, can search, trigger fcm now
                                if(!this.getFirstArg()){
                                    searchStatus = SearchStatus.WaitingUserRespond;
                                    publishProgress(SearchStatus.WaitingUserRespond);

                                    NotificationSender.sendWithUserId(myUserId, targetUserId,
                                            FCMMessageType.UpdateLocation,
                                            RequestLocationTaskFrag.timeoutSecs, msgId,
                                            new Pair<String, String>("senderToken", myToken));

//                                    Threadings.runInBackground(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            while (searchStatus == SearchStatus.WaitingUserRespond){
//                                                NotificationSender.sendWithUserId(myUserId, targetUserId,
//                                                        FCMMessageType.UpdateLocation,
//                                                        RequestLocationTaskFrag.timeoutSecs, msgId,
//                                                        new Pair<String, String>("senderToken", myToken));
//                                                Threadings.sleep(5000);
//
//                                                if (finish){
//                                                    break;
//                                                }
//                                            }
//                                        }
//                                    });

                                }
                                else{
                                    setSearchResult(SearchResult.ErrorUserBlocked);
                                }
                            }
                        });
                    }
                    else{
                        setSearchResult(SearchResult.ErrorNoLink);
                    }
                }
            });


            while (!finish){
                Threadings.sleep(500);
                if(disposed) return null;
            }

            return null;
        }



        private void setSearchResult(SearchResult newSearchResult){
            Logs.show("Finish search " + newSearchResult);
            searchResult = newSearchResult;
            finish = true;
        }

        @Override
        protected void onProgressUpdate(SearchStatus... values) {
            super.onProgressUpdate(values);
            searchStatus = values[0];
            notifyProgress(values[0]);
        }

        @Override
        protected void onPostExecute(LocationModel locationModel) {
            super.onPostExecute(locationModel);

            if(!disposed){
                notifyResult(resultLocationModel, searchResult);
            }
        }

        private void checkHasLink(String myUserId, String targetUserId, final RunnableArgs<Boolean> toRun){
            FirebaseDB.checkLinkExist(myUserId, targetUserId, new FirebaseListener<Boolean>() {
                @Override
                public void onResult(Boolean result, com.ffinder.android.enums.Status status) {
                    if(status == com.ffinder.android.enums.Status.Success && result != null){
                        toRun.run(result);
                    }
                    else{
                        toRun.run(false);
                    }
                }
            });
        }

        private void checkMeIsBlocked(String myUserId, String targetUserId, final RunnableArgs<Boolean> toRun){
            FirebaseDB.checkMeIsBlock(myUserId, targetUserId, new FirebaseListener<Boolean>() {
                @Override
                public void onResult(Boolean result, com.ffinder.android.enums.Status status) {
                    if(status == com.ffinder.android.enums.Status.Success && result != null){
                        toRun.run(result);
                    }
                    else{
                        toRun.run(true);
                    }
                }
            });
        }

        private void monitorTimeoutPhase(){
            Threadings.runInBackground(new Runnable() {
                @Override
                public void run() {
                    int sleepDuration = 1000;
                    int totalSleepDuration = 0;
                    int phase = 0;
                    while (!finish){
                        if((totalSleepDuration > (timeoutSecs * 1000 * 0.33))
                                && phase == 0){
                            phase = 1;
                            notifyTimeoutPhase(phase);
                        }
                        else if((totalSleepDuration > (timeoutSecs * 1000 * 0.66))
                                && phase == 1){
                            phase = 2;
                            notifyTimeoutPhase(phase);
                        }

                        Threadings.sleep(sleepDuration);
                        totalSleepDuration += sleepDuration;
                    }

                }
            });
        }

        public void taskTimeout() {
            MyModel myModel = new MyModel(context);
            myModel.loadFriend(targetUserId);
            FriendModel friendModel = myModel.getFriendModelById(targetUserId);


            //update task search status with friend model one, as friend model always hold latest status
            searchStatus = friendModel.getSearchStatus();


            //only trigger auto notify when error is waiting user response
            boolean shouldAutoNotify = false;


            if(searchStatus == SearchStatus.WaitingUserLocation){
                setSearchResult(SearchResult.ErrorTimeoutLocationDisabled);
                shouldAutoNotify = true;
            }
            else if(searchStatus == SearchStatus.WaitingUserRespond){
                setSearchResult(SearchResult.ErrorTimeoutUnknownReason);
                shouldAutoNotify = true;
            }
            else if(searchStatus == SearchStatus.CheckingData){
                setSearchResult(SearchResult.ErrorTimeoutNoConnection);
                shouldAutoNotify = false;
            }

            if(shouldAutoNotify){
                FirebaseMessaging.getInstance().subscribeToTopic(targetUserId);

                //sendWithUserId one long ttl msg, hopefully user will reply asap or
                // when it has connection
                NotificationSender.sendWithUserId(userId, targetUserId,
                        FCMMessageType.UpdateLocation,
                        NotificationSender.TTL_LONG, msgId,
                        new Pair<String, String>("senderToken", myToken));
            }
        }


        public void dispose(){
            finish = true;
            disposed = true;
        }
    }













}
