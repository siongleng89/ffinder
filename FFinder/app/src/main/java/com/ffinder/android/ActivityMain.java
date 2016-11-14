package com.ffinder.android;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.Pair;
import android.support.v7.widget.*;
import android.view.ContextMenu;
import android.view.View;
import android.widget.LinearLayout;
import com.ffinder.android.absint.activities.IFriendsAdapterHolder;
import com.ffinder.android.absint.activities.IProfileImagePickerListener;
import com.ffinder.android.absint.activities.MyActivityAbstract;
import com.ffinder.android.absint.adapters.IFriendItemListener;
import com.ffinder.android.absint.controls.ISearchFailedListener;
import com.ffinder.android.absint.databases.FirebaseListener;
import com.ffinder.android.absint.tasks.RequestLocationTaskFragListener;
import com.ffinder.android.adapters.FriendsAdapter;
import com.ffinder.android.controls.SearchFailedDialog;
import com.ffinder.android.enums.*;
import com.ffinder.android.helpers.*;
import com.ffinder.android.models.FriendModel;
import com.ffinder.android.models.LocationModel;
import com.ffinder.android.models.MyModel;
import com.ffinder.android.statics.Vars;
import com.ffinder.android.tasks.AdsIdTask;
import com.ffinder.android.tasks.RequestLocationTaskFrag;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ActivityMain extends MyActivityAbstract implements
        IFriendItemListener, IFriendsAdapterHolder, IProfileImagePickerListener {

    private LinearLayout layoutBottom;
    private LayoutNextAdsCd layoutNextAdsCd;
    private MyModel myModel;
    private RecyclerView listFriends;
    private FriendsAdapter friendsAdapter;
    private final int pickImageCode = 100;
    private String pickingProfileImageForFriendId;

    public ActivityMain() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        enableCustomActionBar();

        setActionBarTitle(R.string.app_name);

        addActionToActionBar(ActionBarActionType.ShareKey, false, false);
        addActionToActionBar(ActionBarActionType.Overflow, false, false);
        addActionToOverflow(getString(R.string.add_new_member_manually_title));
        addActionToOverflow(getString(R.string.settings_activity_title));

        myModel = getMyModel();

        layoutBottom = (LinearLayout) findViewById(R.id.layoutBottom);
        layoutNextAdsCd = new LayoutNextAdsCd(this, myModel);
        layoutBottom.addView(layoutNextAdsCd.getView());

        listFriends = (RecyclerView) findViewById(R.id.listFriends);
        friendsAdapter = new FriendsAdapter(this, myModel.getFriendModels(), this, this, this);
        listFriends.setAdapter(friendsAdapter);
        listFriends.setItemAnimator(new DefaultItemAnimator());
        ((DefaultItemAnimator) listFriends.getItemAnimator()).setSupportsChangeAnimations(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setAutoMeasureEnabled(false);
        listFriends.setLayoutManager(layoutManager);

        setListeners();

        Threadings.runInBackground(new Runnable() {
            @Override
            public void run() {
                if(myModel.loadAllFriendModels()){
                    myModel.sortFriendModels();
                }

                //reload(recreate/remove) task fragments for friend searching (mostly for rotating device)
                reloadFriendsDesignByRequestLocationTaskFrags();

                //refresh friend list from database
                refreshFriendList();

                //save adsId to database to persist user even uninstalling apps
                AdsIdTask adsIdTask = new AdsIdTask(ActivityMain.this, myModel.getUserId());
                adsIdTask.execute();

                //save refresh token provided token refresh called in service is called before user login
                //which firebase will block it from saving
                String refreshedToken = FirebaseInstanceId.getInstance().getToken();
                if(!Strings.isEmpty(refreshedToken)) FirebaseDB.updateMyToken(myModel.getUserId(), refreshedToken);

                //some of the devices have known issues, notify the user to follow setup guides
                checkKnownIssuePhones();

                //subscribe for push notifications
                FirebaseMessaging.getInstance().subscribeToTopic("allDevices");
            }
        });

        onNewIntent(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();

        layoutNextAdsCd.onResume();

        if(myModel.loadAllFriendModels()){
            myModel.sortFriendModels();
        }

        reloadFriendsDesignByRequestLocationTaskFrags();
        PreferenceUtils.delete(this, PreferenceType.AutoNotifiedReceivedIds);

        //check if pending to add user, if yes, pop add user dialog automatically
        checkHasPendingToAddUser();


        //auto run search myself the first time user run apps

        Bundle extras = getIntent().getExtras();
        if(extras !=null && extras.containsKey("firstRun")) {
            getIntent().removeExtra("firstRun");
            Threadings.delay(1000, new Runnable() {
                @Override
                public void run() {

                    if(getMyModel().getNonSelfFriendModelsCount() == 0){
                        FriendModel myOwnModel = getMyModel().getFriendModelById(getMyModel().getUserId());
                        searchNow(myOwnModel);
                    }

                }
            });
        }

    }

    @Override
    public void backPressed() {
        super.backPressed();
        FragmentManager fm = getSupportFragmentManager();
        for(FriendModel friendModel : getMyModel().getFriendModels()){
            if(!Strings.isEmpty(friendModel.getUserId())){
                RequestLocationTaskFrag taskFragment = (RequestLocationTaskFrag)
                        fm.findFragmentByTag(friendModel.getUserId());
                // terminate all tasks, as user is quitting already
                if (taskFragment != null) {
                    taskFragment.terminate();
                    getSupportFragmentManager().beginTransaction().remove(taskFragment).commit();
                }
            }
        }
    }

    @Override
    public void onActionButtonClicked(ActionBarActionType actionBarActionType) {
        super.onActionButtonClicked(actionBarActionType);

        switch (actionBarActionType){
            case ShareKey:
                Intent intent = new Intent(this, ActivityShareKey.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onOverflowActionClicked(String title, int position) {
        super.onOverflowActionClicked(title, position);

        if(position == 0){
            Intent intent = new Intent(this, ActivityAddFriend.class);
            startActivity(intent);
        }
        else if(position == 1){
            Intent intent = new Intent(this, ActivitySettings.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle extras = intent.getExtras();
        //open apps by clicking share key reminder push notification
        if(extras !=null && extras.containsKey("shareKey")) {
            Intent intent2 = new Intent(this, ActivityShareKey.class);
            startActivity(intent2);
        }

    }

    //auto add user if there is pending add user key
    private void checkHasPendingToAddUser(){
        if(!Strings.isEmpty(Vars.pendingAddUserKey)){
            Intent intent = new Intent(ActivityMain.this, ActivityAddFriend.class);
            startActivity(intent);
        }
    }

    //reload friends adapter design by checking retained fragments
    private void reloadFriendsDesignByRequestLocationTaskFrags(){
        Threadings.runInBackground(new Runnable() {
            @Override
            public void run() {
                FragmentManager fm = getSupportFragmentManager();

                for(FriendModel friendModel : getMyModel().getFriendModels()){
                    if(!Strings.isEmpty(friendModel.getUserId())){
                        RequestLocationTaskFrag taskFragment = (RequestLocationTaskFrag)
                                fm.findFragmentByTag(friendModel.getUserId());

                        friendModel.load(ActivityMain.this);

                        // If the Fragment is non-null, then it is currently being
                        // retained across a configuration change.
                        if (taskFragment != null) {
                            if(friendModel.getSearchStatus() == SearchStatus.End){
                                taskFragment.terminate();
                                getSupportFragmentManager().beginTransaction().remove(taskFragment).commit();
                            }
                            else{
                                //reset listener
                                setRequestLocationTaskFragListener(taskFragment);
                            }
                        }
                        //check if there is any leftoever stuck search, if there is
                        //overwrite the search status
                        else{
                            if(friendModel.getSearchStatus() != SearchStatus.End){
                                friendModel.setSearchStatus(SearchStatus.End);
                                friendModel.save(ActivityMain.this);
                            }
                        }
                    }
                }
                ActivityMain.this.updateFriendsListAdapter();
            }
        });
    }

    private void createRequestLocationTaskFrag(final FriendModel friendModel){
        FragmentManager fm = getSupportFragmentManager();
        RequestLocationTaskFrag taskFragment = (RequestLocationTaskFrag)
                                                    fm.findFragmentByTag(friendModel.getUserId());
        //not in search
        if (taskFragment == null || taskFragment.getCurrentResult() != null) {
            if(friendModel.getSearchResult() != null){
                if(friendModel.getSearchResult() == SearchResult.ErrorTimeoutUnknownReason){

                    new SearchFailedDialog(ActivityMain.this,
                            friendModel).show();
                    return;
                }
            }

            searchNow(friendModel);
        }
    }

    private void searchNow(final FriendModel friendModel){
        Threadings.postRunnable(new Runnable() {
            @Override
            public void run() {
                layoutNextAdsCd.friendSearched(new RunnableArgs<Boolean>() {
                    @Override
                    public void run() {
                        if(this.getFirstArg()){
                            friendModel.setSearchResult(SearchResult.Normal);
                            friendModel.setSearchStatus(SearchStatus.Starting);
                            friendModel.setClosedError(false);
                            friendModel.setTimeoutPhase(0);
                            final FragmentManager fm = getSupportFragmentManager();
                            RequestLocationTaskFrag frag = RequestLocationTaskFrag.newInstance(
                                    myModel.getUserId(), FirebaseInstanceId.getInstance().getToken(),
                                    friendModel.getUserId());
                            fm.beginTransaction().add(frag, friendModel.getUserId()).commit();
                            setRequestLocationTaskFragListener(frag);
                        }
                    }
                });
            }
        });
    }

    //refresh friends list from firebase database
    private void refreshFriendList(){
        FirebaseDB.getAllMyLinks(myModel.getUserId(), new FirebaseListener() {
            @Override
            public void onResult(Object result, Status status) {
                if(status == Status.Success && result != null){

                    List<Pair<String, Object>> list = (List<Pair<String, Object>>) result;
                    boolean foundNew = false;
                    for(Pair<String, Object> pair : list){
                        if(!myModel.checkFriendExist(pair.first)){
                            String userId = (String) pair.first;
                            String name = (String) pair.second;

                            FriendModel friendModel = new FriendModel();
                            friendModel.setName(name);
                            friendModel.setUserId(userId);
                            friendModel.save(ActivityMain.this);
                            myModel.addFriendModel(friendModel);
                            foundNew = true;
                        }
                    }

                    if(foundNew){;
                        myModel.sortFriendModels();
                        myModel.commitFriendUserIds();
                        ActivityMain.this.updateFriendsListAdapter();
                    }

                }
            }
        });

    }

    private void checkKnownIssuePhones(){
        boolean notify = Strings.isEmpty(PreferenceUtils.get(this, PreferenceType.DontRemindMeAgainPhoneIssue));
        if(notify){
            final String model = Build.BRAND;
            PhoneBrand phoneBrand = PhoneBrand.convertStringToPhoneBrand(model);
            if(phoneBrand != PhoneBrand.UnknownPhoneBrand){
                String msg = AndroidUtils.getPhoneBrandKnownIssue(this, phoneBrand);
                if(!Strings.isEmpty(msg)){
                    final OverlayBuilder builder = OverlayBuilder.build(this);
                    builder.setContent(msg)
                        .setOverlayType(OverlayType.OkOnly)
                        .setCheckboxTitle(getString(R.string.don_remind_me_again))
                        .setOnDismissRunnable(new Runnable() {
                            @Override
                            public void run() {
                                if(builder.isChecked()){
                                    PreferenceUtils.put(ActivityMain.this,
                                            PreferenceType.DontRemindMeAgainPhoneIssue, "1");
                                }
                            }
                        });
                    Threadings.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            builder.show();
                        }
                    });

                }
            }
        }
    }

    public void setListeners(){
//        listFriends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                createRequestLocationTaskFrag(myModel.getFriendModels().get(position));
//            }
//        });

        registerBroadcastReceiver(BroadcastEvent.RefreshFriend, new RunnableArgs<Intent>() {
            @Override
            public void run() {
                Intent intent = this.getFirstArg();
                final String friendId = intent.getStringExtra("userId");
                if(getMyModel().checkFriendExist(friendId)) {

                    final FriendModel friendModel = getMyModel().getFriendModelById(friendId);
                    SearchStatus originalSearchStatus = friendModel.getSearchStatus();
                    friendModel.load(ActivityMain.this);

                    FragmentManager fm = getSupportFragmentManager();
                    RequestLocationTaskFrag taskFragment = (RequestLocationTaskFrag)
                            fm.findFragmentByTag(friendId);

                    // If the Fragment is non-null, then it is currently searching,
                    // check is waiting for user respond,
                    // if exist and friend status is end, mean it is already finish searching, can safely
                    // remove task fragment already
                    // then update the address design
                    if (taskFragment != null && friendModel.getSearchStatus() == SearchStatus.End) {
                        taskFragment.terminate();
                        if(!isPaused()){
                            fm.beginTransaction().remove(taskFragment).commit();
                        }
                        friendModel.setRecentlyFinishSearch(true);
                        friendModel.setTimeoutPhase(0);
                    }
                    else{
                        friendModel.setRecentlyFinishSearch(false);
                    }

                    //this alive msg is redundant in auto search
                    if(originalSearchStatus == SearchStatus.End &&
                            friendModel.getSearchStatus() == SearchStatus.WaitingUserLocation){
                        return;
                    }

                    ActivityMain.this.updateFriendsListAdapter(friendId);
                }
                else{
                    getMyModel().loadFriend(friendId);
                    onAddFriend(getMyModel().getFriendModelById(friendId));
                }
            }
        });

        registerBroadcastReceiver(BroadcastEvent.RefreshNewlyAddedFriend, new RunnableArgs<Intent>() {
            @Override
            public void run() {
                myModel.loadAllFriendModels();
                myModel.sortFriendModels();
                updateFriendsListAdapter();
            }
        });

        registerBroadcastReceiver(BroadcastEvent.SearchAgainAnyway, new RunnableArgs<Intent>() {
            @Override
            public void run() {
                Intent intent = this.getFirstArg();
                final String friendId = intent.getStringExtra("friendId");
                FriendModel friendModel = getMyModel().getFriendModelById(friendId);
                if(friendModel != null){
                    searchNow(friendModel);
                }
            }
        });

        registerBroadcastReceiver(BroadcastEvent.SearchSuccess, new RunnableArgs<Intent>() {
            @Override
            public void run() {
                Analytics.logEvent(AnalyticEvent.Search_Result, "SearchSuccess");
            }
        });

    }

    @Override
    public void onSearchRequest(FriendModel friendModel) {
        createRequestLocationTaskFrag(friendModel);
    }

    public void setRequestLocationTaskFragListener(final RequestLocationTaskFrag requestLocationTaskFrag){
        requestLocationTaskFrag.setRequestLocationTaskFragListener(new RequestLocationTaskFragListener() {
            @Override
            public void onTimeoutPhaseChanged(String userId, int newPhase) {
                FriendModel friendModel = myModel.getFriendModelById(userId);
                if(friendModel != null){
                    if(newPhase != friendModel.getTimeoutPhase()){
                        friendModel.setTimeoutPhase(newPhase);
                    }
                    ActivityMain.this.updateFriendsListAdapter(userId);
                }
            }

            @Override
            public void onUpdateStatus(String userId, SearchStatus newStatus) {
                FriendModel friendModel = myModel.getFriendModelById(userId);
                if(friendModel != null){
                    if(newStatus != friendModel.getSearchStatus()){
                        friendModel.setSearchStatus(newStatus);
                        friendModel.save(ActivityMain.this);
                    }
                    ActivityMain.this.updateFriendsListAdapter(userId);
                }

            }

            @Override
            public void onUpdateResult(final String userId, final LocationModel locationModel,
                                       final SearchStatus finalSearchStatus, final SearchResult result) {

                final FriendModel friendModel = myModel.getFriendModelById(userId);
                if(friendModel != null){
                    friendModel.setSearchStatus(finalSearchStatus);
                    friendModel.setSearchResult(result);
                    friendModel.setRecentlyFinishSearch(true);
                    friendModel.setTimeoutPhase(0);
                    friendModel.setSearchStatus(SearchStatus.End);

                    //location model could be null if unable to search user
                    if(locationModel != null){
                        friendModel.setLastLocationModel(locationModel);
                    }

                    friendModel.getLastLocationModel().geodecodeCoordinatesIfNeeded(ActivityMain.this, new Runnable() {
                        @Override
                        public void run() {
                            friendModel.save(ActivityMain.this);
                            ActivityMain.this.updateFriendsListAdapter(userId);
                        }
                    });
                }



                if(!isPaused()){
                    Fragment fragment = getSupportFragmentManager().findFragmentByTag(userId);
                    if(fragment != null)
                        getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                }

                Analytics.logEvent(AnalyticEvent.Search_Result, result.name());
            }
        });
    }

    @Override
    public void updateFriendsListAdapter(final String friendId) {
        Threadings.postRunnable(new Runnable() {
            @Override
            public void run() {
                int row = -1;
                for (int i = 0; i < getMyModel().getFriendModels().size(); i++){
                    if(getMyModel().getFriendModels().get(i).getUserId().equals(friendId)){
                        row = i;
                        break;
                    }
                }

                if(row >= 0){
                    friendsAdapter.rowChanged(row);
                    Logs.show("row changed: " + row);
                }
            }
        });
    }

    //notify friend models changed
    @Override
    public void updateFriendsListAdapter() {
        Threadings.postRunnable(new Runnable() {
            @Override
            public void run() {
                friendsAdapter.notifyDataSetChanged();
            }
        });
    }

    //add friend process is delegated in ActivityAddFriend
    @Override
    public void onAddFriend(FriendModel friendModel) {
        updateFriendsListAdapter();
    }

    @Override
    public void onDeleteFriend(FriendModel friendModel) {
        FirebaseDB.deleteLink(getMyModel().getUserId(), friendModel.getUserId(), null);
        //remove blocking user since already deleted
        FirebaseDB.changeBlockUser(myModel.getUserId(), friendModel.getUserId(), false, null);

        getMyModel().deleteFriend(friendModel);
        friendModel.delete(this);
        getMyModel().commitFriendUserIds();
        updateFriendsListAdapter();
    }

    @Override
    public void onEditFriend(FriendModel friendModel, String newName) {
        FriendModel changingFriendModel = getMyModel().getFriendModelById(friendModel.getUserId());
        if(changingFriendModel != null){
            changingFriendModel.setName(newName);
            changingFriendModel.save(this);
            getMyModel().sortFriendModels();
            getMyModel().commitFriendUserIds();
            updateFriendsListAdapter();
            FirebaseDB.editLinkName(myModel.getUserId(), changingFriendModel.getUserId(), newName, null);
        }
    }

    @Override
    public void pickImageForFriend(String friendId) {
        this.pickingProfileImageForFriendId = friendId;
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, pickImageCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        switch(requestCode){
            case pickImageCode:
                if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
                    try{
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA };
                        Cursor cursor = getContentResolver().query(selectedImage,
                                filePathColumn, null, null, null);
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        String picturePath = cursor.getString(columnIndex);
                        cursor.close();

                        //crop using image path
                        performCrop(picturePath);

                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                }
                break;
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    Uri resultUri = result.getUri();
                    AndroidUtils.resizeAndShrinkProfileImage(new File(resultUri.getPath()));
                    AndroidUtils.moveProfileImageToPrivateDir(this,
                            resultUri,
                            pickingProfileImageForFriendId);

                    //set to null to let it refresh image
                    getMyModel().getFriendModelById(pickingProfileImageForFriendId).setHasProfileImage(null);
                    updateFriendsListAdapter(pickingProfileImageForFriendId);

                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                }

        }
    }

    private void performCrop(String picUri) {
        try {

            Uri targetUri = Uri.fromFile(new File(picUri));

            CropImage.activity(targetUri)
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .setAutoZoomEnabled(false)
                    .setFixAspectRatio(true)
                    .setAspectRatio(280, 280)
                    .start(this);
        }
        catch (ActivityNotFoundException anfe) {

        }
    }

}
