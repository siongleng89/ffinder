package com.ffinder.android.tasks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Pair;
import com.ffinder.android.absint.databases.FirebaseListener;
import com.ffinder.android.absint.tasks.RequestLocationTaskFragListener;
import com.ffinder.android.enums.FCMMessageType;
import com.ffinder.android.enums.SearchResult;
import com.ffinder.android.enums.SearchStatus;
import com.ffinder.android.enums.Status;
import com.ffinder.android.helpers.FirebaseDB;
import com.ffinder.android.helpers.NotificationSender;
import com.ffinder.android.models.LocationModel;
import com.ffinder.android.models.OnlineRequest;
import com.ffinder.android.utils.*;

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
    private int timeoutSecs = 45;
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

        FirebaseDB.removeAutoNotify(targetUserId, userId);

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
                finally {
                    requestLocationTask.dispose();
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

    public void notifyProgress(SearchStatus searchStatus){
        currentStatus = searchStatus;
        if(requestLocationTaskFragListener != null) requestLocationTaskFragListener.onUpdateStatus(targetUserId, currentStatus);
    }

    public void notifyResult(LocationModel locationModel, SearchResult searchResult){
        currentResult = searchResult;
        if(requestLocationTaskFragListener != null)
            requestLocationTaskFragListener.onUpdateResult(targetUserId, locationModel, currentStatus, searchResult);

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

        public RequestLocationTask(Context context) {
            this.context = context;
        }

        @Override
        protected LocationModel doInBackground(String... params) {
            myUserId = params[0];
            myToken = params[1];
            targetUserId = params[2];

            publishProgress(SearchStatus.CheckingData);

            //check user is allowed to search the target user
            checkCanSearch(myUserId, targetUserId, new RunnableArgs<Boolean>() {
                @Override
                public void run() {
                    if(finish) return;

                    //can search, trigger fcm now
                    if(this.getFirstArg()){

                        publishProgress(SearchStatus.WaitingUserRespond);

                        NotificationSender.sendWithUserId(myUserId, targetUserId, FCMMessageType.UpdateLocation,
                                NotificationSender.TTL_INSTANT, new Pair<String, String>("senderToken", myToken));

                    }
                    else{
                        setSearchResult(SearchResult.ErrorNoLink);
                    }
                }
            });


            while (!finish){
                Threadings.sleep(500);
            }

            if(disposed) return null;

            if(searchStatus == SearchStatus.WaitingUserRespond ||
                    searchStatus == SearchStatus.WaitingUserLocation){
                FirebaseDB.autoNotifyMe(userId, targetUserId, new FirebaseListener() {
                    @Override
                    public void onResult(Object result, com.ffinder.android.enums.Status status) {
                        //sendWithUserId one long ttl msg, hopefully user will reply asap or when it has connection
                        NotificationSender.sendWithUserId(userId, targetUserId, FCMMessageType.UpdateLocation,
                                NotificationSender.TTL_LONG);
                    }
                });
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
                dispose();
            }

        }

        private void checkCanSearch(String myUserId, String targetUserId, final RunnableArgs<Boolean> toRun){
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


        public void taskTimeout() {
            //timeout, get user latest location model from database
            FirebaseDB.getUserLocation(targetUserId, new FirebaseListener<LocationModel>(LocationModel.class) {
                @Override
                public void onResult(LocationModel result, com.ffinder.android.enums.Status status) {
                    if(status == com.ffinder.android.enums.Status.Success && result != null){
                        resultLocationModel = result;
                    }


                    if(searchStatus == SearchStatus.WaitingUserLocation){
                        setSearchResult(SearchResult.ErrorTimeoutLocationDisabled);
                    }
                    else if(searchStatus == SearchStatus.CheckingData){
                        setSearchResult(SearchResult.ErrorTimeoutNoConnection);
                    }
                    else{
                        setSearchResult(SearchResult.ErrorTimeoutUnknownReason);
                    }
                }
            });
        }



        public void dispose(){
            finish = true;
            disposed = true;
        }
    }













}
