package com.ffinder.android.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by SiongLeng on 3/9/2016.
 */
public class RequestLocationTaskFrag extends Fragment {

    private RequestLocationTaskFragListener requestLocationTaskFragListener;
    private String userId, targetUserId;
    private SearchStatus currentStatus;
    private SearchResult currentResult;
    //private int reuseToleranceSecs = 60;
    private int reuseToleranceSecs = 1;
    private int timeoutSecs = 45;

    public static RequestLocationTaskFrag newInstance(String userId, String targetUserId) {
        RequestLocationTaskFrag f = new RequestLocationTaskFrag();
        Bundle args = new Bundle();
        args.putString("userId", userId);
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
        targetUserId = bundle.getString("targetUserId");
        FirebaseDB.removeAutoNotify(targetUserId, userId);

        Threadings.runInBackground(new Runnable() {
            @Override
            public void run() {
                RequestLocationTask requestLocationTask = new RequestLocationTask(getContext());

                try {
                    requestLocationTask
                            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, userId, targetUserId)
                            .get(timeoutSecs, TimeUnit.SECONDS);

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
        private boolean finish;
        private LocationModel resultLocationModel;
        private OnlineRequest locationOnlineRequest, pingOnlineRequest;
        private String targetUserId, myUserId;
        private Context context;
        private SearchStatus searchStatus;
        private SearchResult searchResult;

        public RequestLocationTask(Context context) {
            this.context = context;
        }

        @Override
        protected LocationModel doInBackground(String... params) {
            myUserId = params[0];
            targetUserId = params[1];

            requestLocationPart();

            while (!finish){
                Threadings.sleep(500);
            }

            return null;
        }

        private void requestLocationPart(){
            publishProgress(SearchStatus.CheckingData);

            checkCanSearch(myUserId, targetUserId, new RunnableArgs<Boolean>() {
                @Override
                public void run() {
                    if(finish) return;

                    if(this.getFirstArg()){
                        checkHasRecentlyLastUpdated(myUserId, targetUserId, new RunnableArgs<LocationModel>() {
                            @Override
                            public void run() {
                                if(finish) return;

                                //recently not updated
                                if(this.getFirstArg() == null){

                                    monitorUserIsAlive(myUserId, targetUserId, new OneTimeRunnableArgs<Boolean>() {
                                        @Override
                                        public void run() {
                                            if(finish) return;
                                            if(this.getFirstArg()){

                                                publishProgress(SearchStatus.WaitingUserRespond);

                                                sendRequestLocation(myUserId, targetUserId, new OneTimeRunnableArgs<LocationModel>() {
                                                    @Override
                                                    public void run() {
                                                        if(finish) return;

                                                        if(this.getFirstArg() != null){
                                                            resultLocationModel = this.getFirstArg();
                                                            geodecodePart(true, null);
                                                        }
                                                        else{
                                                            setSearchResult(SearchResult.ErrorUnknown);
                                                        }
                                                    }
                                                });
                                            }
                                            else{
                                                setSearchResult(SearchResult.ErrorUnknown);
                                            }
                                        }
                                    });

                                }
                                else{
                                    //recently updated, no need request again
                                    resultLocationModel = this.getFirstArg();
                                    geodecodePart(true, null);
                                }
                            }
                        });
                    }
                    else{
                        setSearchResult(SearchResult.ErrorNoLink);
                    }
                }
            });
        }

        private void geodecodePart(final boolean notify, final Runnable onFinish){

            if(notify) publishProgress(SearchStatus.Geocoding);

            AndroidUtils.geoDecode(context, resultLocationModel.getLatitude(), resultLocationModel.getLongitude(), new RunnableArgs<String>() {
                @Override
                public void run() {
                    addressRetrieved(this.getFirstArg(), notify, onFinish);
                }
            });
        }

        private void addressRetrieved(String address, boolean notify, Runnable onFinish){
            if(finish) return;

            resultLocationModel.setAddress(address);

            if(notify){
                publishProgress(SearchStatus.End);
                setSearchResult(SearchResult.Normal);
            }

            if(onFinish != null) onFinish.run();

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
            notifyResult(resultLocationModel, searchResult);
            dispose();
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


        private void checkHasRecentlyLastUpdated(String myUserId, final String targetUserId, final RunnableArgs<LocationModel> toRun){
            FirebaseDB.getCurrentTimestamp(myUserId, new FirebaseListener<String>() {
                @Override
                public void onResult(final String currentTimeStampString, com.ffinder.android.enums.Status status) {
                    if(status == com.ffinder.android.enums.Status.Success && currentTimeStampString != null){
                        FirebaseDB.getUserLocation(targetUserId, new FirebaseListener<LocationModel>(LocationModel.class) {
                            @Override
                            public void onResult(LocationModel result, com.ffinder.android.enums.Status status) {
                                if(status == com.ffinder.android.enums.Status.Success && result != null){
                                    resultLocationModel = result;

                                    long previousTimestamp = result.getTimestampLastUpdatedLong() / 1000;
                                    long currentTimeStamp = Long.valueOf(currentTimeStampString) / 1000;

                                    double differenceInMin = (((double) currentTimeStamp - previousTimestamp) / 60);
                                    Logs.show("Comparing previous: " + previousTimestamp + " with current: " + currentTimeStamp);
                                    Logs.show("Compare result is " + differenceInMin);
                                    if(differenceInMin * 60 > reuseToleranceSecs){
                                        toRun.run(null);
                                    }
                                    else{
                                        toRun.run(result);
                                    }
                                }
                                else{
                                    toRun.run(null);
                                }
                            }
                        });
                    }
                    else{
                        toRun.run(null);
                    }
                }
            });
        }

        private void monitorUserIsAlive(String myUserId, String targetUserId, final OneTimeRunnableArgs<Boolean> onAttach){
            final int[] triggeredCount = {0};
            pingOnlineRequest = FirebaseDB.monitorUserPing(myUserId, targetUserId, new FirebaseListener() {
                @Override
                public void onResult(Object result, com.ffinder.android.enums.Status status) {
                    if(status == com.ffinder.android.enums.Status.Success){
                        if(triggeredCount[0] == 0){
                            triggeredCount[0]++;
                        }
                        else{
                            if(triggeredCount[0] == 1){
                                publishProgress(SearchStatus.WaitingUserLocation);
                                triggeredCount[0]++;
                            }
                        }
                        onAttach.run(true);
                    }
                    else{
                        onAttach.run(false);
                    }
                }
            });
        }

        private void sendRequestLocation(final String myUserId, final String targetUserId, final OneTimeRunnableArgs<LocationModel> onFinish){
            final boolean[] triggeredBefore = new boolean[1];

            locationOnlineRequest = FirebaseDB.monitorUserLocation(targetUserId, new FirebaseListener<LocationModel>(LocationModel.class) {
                @Override
                public void onResult(LocationModel result, com.ffinder.android.enums.Status status) {
                    if(finish) return;
                    else{
                        if(!triggeredBefore[0]) {
                            triggeredBefore[0] = true;
                            notifyTarget(myUserId, targetUserId);
                            return;
                        }

                        if(status == com.ffinder.android.enums.Status.Success && result != null){
                            onFinish.run(result);
                        }
                        else{
                            onFinish.run(null);
                        }
                    }
                }
            });
        }

        private void notifyTarget(final String myUserId, final String targetUserId){
            Threadings.runInBackground(new Runnable() {
                @Override
                public void run() {
                    int tries = 2;
                    while (tries > 0){
                        if(searchStatus != SearchStatus.WaitingUserRespond || finish){
                            break;
                        }
                        else{
                            if(tries == 2){
                                NotificationSender.send(myUserId, targetUserId, FCMMessageType.UpdateLocation,
                                        NotificationSender.TTL_INSTANT);
                            }
                            else{
                                autoNotifyMe();
                            }
                        }
                        tries--;
                        Logs.show("Sending notification try left: " + tries);
                        Threadings.sleep(20 * 1000);
                    }
                }
            });
        }

        public void taskTimeout() {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if(searchStatus == SearchStatus.WaitingUserLocation){
                        autoNotifyMe();
                        setSearchResult(SearchResult.ErrorTimeoutLocationDisabled);
                    }
                    else if(searchStatus == SearchStatus.CheckingData){
                        setSearchResult(SearchResult.ErrorTimeoutNoConnection);
                    }
                    else{
                        setSearchResult(SearchResult.ErrorTimeoutUnknownReason);
                    }
                }
            };

            if(resultLocationModel != null && Strings.isEmpty(resultLocationModel.getAddress()) &&
                    !Strings.isEmpty(resultLocationModel.getLatitude())){
                geodecodePart(false, runnable);
            }
            else{
                runnable.run();
            }

        }

        private void autoNotifyMe(){
            FirebaseDB.autoNotifyMe(userId, targetUserId, new FirebaseListener() {
                @Override
                public void onResult(Object result, com.ffinder.android.enums.Status status) {
                    //send one long ttl msg, hopefully user will reply asap or when it has connection
                    NotificationSender.send(userId, targetUserId, FCMMessageType.UpdateLocation,
                            NotificationSender.TTL_LONG);
                }
            });
        }

        public void dispose(){
            if(locationOnlineRequest != null){
                FirebaseDB.deleteMonitorUserLocation(targetUserId, locationOnlineRequest);
                locationOnlineRequest = null;
            }

            if(pingOnlineRequest != null){
                FirebaseDB.deleteMonitorUserPing(myUserId, targetUserId, pingOnlineRequest);
                pingOnlineRequest = null;
            }

        }
    }













}
