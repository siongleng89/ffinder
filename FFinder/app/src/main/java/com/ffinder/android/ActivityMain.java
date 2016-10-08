package com.ffinder.android;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.*;
import com.ffinder.android.absint.activities.IFriendsAdapterHolder;
import com.ffinder.android.absint.activities.MyActivityAbstract;
import com.ffinder.android.absint.adapters.IFriendItemListener;
import com.ffinder.android.absint.controls.ISearchFailedListener;
import com.ffinder.android.absint.databases.FirebaseListener;
import com.ffinder.android.absint.tasks.RequestLocationTaskFragListener;
import com.ffinder.android.adapters.FriendsAdapter;
import com.ffinder.android.controls.*;
import com.ffinder.android.enums.*;
import com.ffinder.android.helpers.Analytics;
import com.ffinder.android.helpers.FirebaseDB;
import com.ffinder.android.models.FriendModel;
import com.ffinder.android.models.LocationModel;
import com.ffinder.android.models.MyModel;
import com.ffinder.android.statics.Vars;
import com.ffinder.android.tasks.AdsIdTask;
import com.ffinder.android.tasks.RequestLocationTaskFrag;
import com.ffinder.android.utils.*;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.List;


public class ActivityMain extends MyActivityAbstract implements IFriendItemListener, IFriendsAdapterHolder {

    private FragmentNextAdsCd fragmentNextAdsCd;
    private MyModel myModel;
    private Button btnShareKey;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView listFriends;
    private RelativeLayout layoutEmptyFriend;
    private FriendsAdapter friendsAdapter;

    public ActivityMain() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        myModel = new MyModel(this);
        myModel.loginFirebase(0, null);

        btnShareKey = (Button) findViewById(R.id.btnShareKey);

        fragmentNextAdsCd = (FragmentNextAdsCd) getSupportFragmentManager().findFragmentById(R.id.nextAdsCdFragment);
        fragmentNextAdsCd.setMyModel(myModel);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorPrimary));
        listFriends = (ListView) findViewById(R.id.listFriends);
        friendsAdapter = new FriendsAdapter(this, R.layout.lvitem_friend, myModel.getFriendModels(), myModel, this);
        listFriends.setAdapter(friendsAdapter);
        registerForContextMenu(listFriends);

        layoutEmptyFriend = (RelativeLayout) findViewById(R.id.layoutEmptyFriendFragment);

        setListeners();

        Threadings.runInBackground(new Runnable() {
            @Override
            public void run() {
                myModel.loadAllFriendModels();
                myModel.sortFriendModels();
                ActivityMain.this.updateFriendsListAdapter();
                refreshFriendList();

                //check if pending to add user, if yes, pop add user dialog automatically
                checkHasPendingToAddUser();

                //reload(recreate/remove) task fragments for friend searching (mostly for rotating device)
                reloadFriendsDesignByRequestLocationTaskFrags();

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

                //auto run search myself the first time user run apps
                Bundle extras = getIntent().getExtras();
                if(extras !=null && extras.containsKey("firstRun")) {
                    if(myModel.getNonSelfFriendModelsCount() == 0){
                        FriendModel myOwnModel = myModel.getFriendModelById(myModel.getUserId());
                        searchNow(myOwnModel);
                    }
                }
            }
        });

        onNewIntent(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        reloadFriendsDesignByRequestLocationTaskFrags();
        PreferenceUtils.delete(this, PreferenceType.AutoNotifiedReceivedIds);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.default_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_share:
                UserKeyDialog userKeyDialog = new UserKeyDialog(ActivityMain.this, myModel);
                userKeyDialog.show();
                break;
            case R.id.action_add:
                AddMemberDialog addMemberDialog = new AddMemberDialog(this, this, myModel);
                addMemberDialog.show();
                break;
            case R.id.action_settings:
                Intent intent = new Intent(this, ActivitySettings.class);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("");
        menu.add(getString(R.string.edit_name_context_menu));
        menu.add(getString(R.string.delete_user_context_menu));

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int listPosition = info.position;

        if(item.getTitle().equals(getString(R.string.edit_name_context_menu))){
            editName(myModel.getFriendModels().get(listPosition));
        }
        else if(item.getTitle().equals(getString(R.string.delete_user_context_menu))){
            deleteUser(myModel.getFriendModels().get(listPosition));
        }else{
            return false;
        }
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle extras = intent.getExtras();
        //open apps by clicking share key reminder push notification
        if(extras !=null && extras.containsKey("shareKey")) {
            UserKeyDialog userKeyDialog = new UserKeyDialog(this, myModel);
            userKeyDialog.show();
        }

    }

    //auto add user if there is pending add user key
    private void checkHasPendingToAddUser(){
        Threadings.postRunnable(ActivityMain.this, new Runnable() {
            @Override
            public void run() {
                if(!Strings.isEmpty(Vars.pendingAddUserKey)){
                    AddMemberDialog addMemberDialog = new AddMemberDialog(
                            ActivityMain.this, ActivityMain.this, myModel);
                    addMemberDialog.show();
                }
            }
        });
    }

    //reload friends adapter design by checking retained fragments
    private void reloadFriendsDesignByRequestLocationTaskFrags(){
        Threadings.postRunnable(this, new Runnable() {
            @Override
            public void run() {
                FragmentManager fm = getSupportFragmentManager();

                for(FriendModel friendModel : myModel.getFriendModels()){
                    RequestLocationTaskFrag taskFragment = (RequestLocationTaskFrag)
                                                            fm.findFragmentByTag(friendModel.getUserId());

                    // If the Fragment is non-null, then it is currently being
                    // retained across a configuration change.
                    if (taskFragment != null) {
                        if(friendModel.getSearchStatus() == SearchStatus.End){
                            getSupportFragmentManager().beginTransaction().remove(taskFragment).commit();
                        }
                        else{
                            friendModel.setSearchResult(SearchResult.Normal);
                            friendModel.setSearchStatus(taskFragment.getCurrentStatus());
                            setRequestLocationTaskFragListener(taskFragment);
                        }
                    }
                }
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
                if(friendModel.getSearchResult() == SearchResult.ErrorTimeoutUnknownReason
                        || friendModel.getSearchResult() == SearchResult.ErrorTimeoutLocationDisabled){

                    new SearchFailedDialog(ActivityMain.this,
                            friendModel.getSearchResult(), new ISearchFailedListener() {
                        @Override
                        public void onSearchAnywayChoose() {
                            searchNow(friendModel);
                        }
                    }).show();
                    return;
                }
            }

            searchNow(friendModel);
        }
    }

    private void searchNow(final FriendModel friendModel){
        Threadings.postRunnable(this, new Runnable() {
            @Override
            public void run() {
                fragmentNextAdsCd.friendSearched(new RunnableArgs<Boolean>() {
                    @Override
                    public void run() {
                        if(this.getFirstArg()){
                            friendModel.setSearchResult(SearchResult.Normal);
                            friendModel.setSearchStatus(SearchStatus.Starting);
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
                Threadings.postRunnable(ActivityMain.this, new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });

        Threadings.delay(20 * 1000, this, new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void editName(FriendModel friendModel){
        EditNameDialog editNameDialog = new EditNameDialog(this, this, friendModel, myModel);
        editNameDialog.show();
    }

    private void deleteUser(FriendModel friendModel){
        ConfirmDeleteDialog confirmDeleteDialog = new ConfirmDeleteDialog(this, this, friendModel, myModel);
        confirmDeleteDialog.show();
    }

    private void checkKnownIssuePhones(){
        boolean notify = Strings.isEmpty(PreferenceUtils.get(this, PreferenceType.DontRemindMeAgainPhoneIssue));
        if(notify){
            final String model = Build.BRAND;
            if(model.equalsIgnoreCase(PhoneBrand.Huawei.name())
                    || model.equalsIgnoreCase(PhoneBrand.Xiaomi.name())
                    || model.equalsIgnoreCase(PhoneBrand.Sony.name())){
                Threadings.postRunnable(ActivityMain.this, new Runnable() {
                    @Override
                    public void run() {
                        KnownIssueDialog knownIssueDialog = new KnownIssueDialog(ActivityMain.this,
                                                        PhoneBrand.valueOf(model));
                        knownIssueDialog.show();
                    }
                });
            }
        }
    }

    public void setListeners(){
        listFriends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                createRequestLocationTaskFrag(myModel.getFriendModels().get(position));
            }
        });

        btnShareKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserKeyDialog userKeyDialog = new UserKeyDialog(ActivityMain.this, myModel);
                userKeyDialog.show();
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFriendList();
            }
        });

        registerBroadcastReceiver(BroadcastEvent.RefreshFriend, new RunnableArgs<Intent>() {
            @Override
            public void run() {
                Logs.show("User location msg received");
                Intent intent = this.getFirstArg();
                final String friendId = intent.getStringExtra("userId");
                if(myModel.checkFriendExist(friendId)) {
                    final FriendModel friendModel = myModel.getFriendModelById(friendId);
                    friendModel.load(ActivityMain.this);

                    Threadings.postRunnable(ActivityMain.this, new Runnable() {
                        @Override
                        public void run() {
                            FragmentManager fm = getSupportFragmentManager();
                            RequestLocationTaskFrag taskFragment = (RequestLocationTaskFrag)
                                    fm.findFragmentByTag(friendId);

                            // If the Fragment is non-null, then it is currently searching,
                            // check is waiting for user respond,
                            // if yes, change to waiting for user location
                            // else, discard
                            if (taskFragment != null) {
                                taskFragment.notifyProgress(SearchStatus.End);
                                taskFragment.notifyResult(friendModel.getLastLocationModel(), SearchResult.Normal);
                            }
                        }
                    });
                }
            }
        });

        registerBroadcastReceiver(BroadcastEvent.RefreshWholeFriendList, new RunnableArgs<Intent>() {
            @Override
            public void run() {
                refreshFriendList();
            }
        });

        registerBroadcastReceiver(BroadcastEvent.UserIsAlive, new RunnableArgs<Intent>() {
            @Override
            public void run() {
                Intent intent = this.getFirstArg();
                final String friendId = intent.getStringExtra("userId");
                Logs.show("User is Alive msg received");

                Threadings.postRunnable(ActivityMain.this, new Runnable() {
                    @Override
                    public void run() {
                        FragmentManager fm = getSupportFragmentManager();
                        RequestLocationTaskFrag taskFragment = (RequestLocationTaskFrag)
                                fm.findFragmentByTag(friendId);

                        // If the Fragment is non-null, then it is currently searching,
                        // check is waiting for user respond,
                        // if yes, change to waiting for user location
                        // else, discard
                        if (taskFragment != null) {
                            if(taskFragment.getCurrentStatus() == SearchStatus.WaitingUserRespond){
                                taskFragment.notifyProgress(SearchStatus.WaitingUserLocation);
                            }
                        }
                    }
                });

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
            public void onUpdateStatus(String userId, SearchStatus newStatus) {
                FriendModel friendModel = myModel.getFriendModelById(userId);
                friendModel.setSearchStatus(newStatus);
                ActivityMain.this.updateFriendsListAdapter();
            }

            @Override
            public void onUpdateResult(String userId, final LocationModel locationModel,
                                       final SearchStatus finalSearchStatus, final SearchResult result) {

                final FriendModel friendModel = myModel.getFriendModelById(userId);

                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        friendModel.setRecentlyFinishSearch(true);
                        friendModel.setSearchStatus(finalSearchStatus);
                        friendModel.setSearchResult(result);
                        friendModel.save(ActivityMain.this);
                        ActivityMain.this.updateFriendsListAdapter();
                    }
                };

                if(locationModel != null){
                    //need to geodecode coordinates to address before save if address is currently empty
                    if(Strings.isEmpty(locationModel.getAddress())
                            && !Strings.isEmpty(locationModel.getLatitude())){
                        AndroidUtils.geoDecode(ActivityMain.this, locationModel.getLatitude(),
                                locationModel.getLongitude(), new RunnableArgs<String>() {
                                    @Override
                                    public void run() {
                                        locationModel.setAddress(this.getFirstArg());
                                        friendModel.setLastLocationModel(locationModel);
                                        runnable.run();
                                    }
                                });
                    }
                    else{
                        runnable.run();
                    }
                }
                else{
                    runnable.run();
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


    //notify friend models changed
    @Override
    public void updateFriendsListAdapter() {
        Threadings.postRunnable(ActivityMain.this, new Runnable() {
            @Override
            public void run() {
                friendsAdapter.notifyDataSetChanged();

                if(myModel.getNonSelfFriendModelsCount() == 0){
                    layoutEmptyFriend.setVisibility(View.VISIBLE);
                }
                else{
                    layoutEmptyFriend.setVisibility(View.GONE);
                }

            }
        });
    }
}
