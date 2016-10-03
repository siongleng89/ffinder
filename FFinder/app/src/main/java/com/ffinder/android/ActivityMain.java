package com.ffinder.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.*;
import com.ffinder.android.absint.activities.MyActivityAbstract;
import com.ffinder.android.absint.adapters.IFriendItemListener;
import com.ffinder.android.absint.controls.ISearchFailedListener;
import com.ffinder.android.absint.databases.FirebaseListener;
import com.ffinder.android.absint.models.MyModelChangedListener;
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


public class ActivityMain extends MyActivityAbstract implements IFriendItemListener {

    private ActivityMain _this;
    private FragmentNextAdsCd fragmentNextAdsCd;
    private MyModel myModel;

    private Button btnShareKey;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView listFriends;
    private RelativeLayout layoutEmptyFriend;
    private FriendsAdapter friendsAdapter;
    private BroadcastReceiver refreshFriendReceiver, refreshFriendListReceiver;
    private boolean afterSavedInstanceState;

    public ActivityMain() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logs.show("ActivityMain onCreate start");

        super.onCreate(savedInstanceState);
        _this = this;
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

                checkHasPendingToAddUser();
                recreateRequestLocationTaskFrags();

                AdsIdTask adsIdTask = new AdsIdTask(ActivityMain.this, myModel.getUserId());
                adsIdTask.execute();

                String refreshedToken = FirebaseInstanceId.getInstance().getToken();
                if(!Strings.isEmpty(refreshedToken)) FirebaseDB.updateMyToken(myModel.getUserId(), refreshedToken);

                checkKnownIssuePhones();
                FirebaseMessaging.getInstance().subscribeToTopic("allDevices");

                Threadings.postRunnable(ActivityMain.this, new Runnable() {
                    @Override
                    public void run() {
                        Bundle extras = getIntent().getExtras();
                        if(extras !=null && extras.containsKey("firstRun")) {
                            if(myModel.getNonSelfFriendModelsCount() == 0){
                                FriendModel myOwnModel = myModel.getFriendModelById(myModel.getUserId());
                                searchNow(myOwnModel);
                            }
                        }
                    }
                });
            }
        });

        onNewIntent(getIntent());

        Logs.show("ActivityMain onCreate end");
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
                AddMemberDialog addMemberDialog = new AddMemberDialog(this, myModel);
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
        if(extras !=null && extras.containsKey("shareKey")) {
            UserKeyDialog userKeyDialog = new UserKeyDialog(this, myModel);
            userKeyDialog.show();
        }

    }

    private void checkHasPendingToAddUser(){
        Threadings.postRunnable(ActivityMain.this, new Runnable() {
            @Override
            public void run() {
                if(!Strings.isEmpty(Vars.pendingAddUserKey)){
                    AddMemberDialog addMemberDialog = new AddMemberDialog(ActivityMain.this, myModel);
                    addMemberDialog.show();
                }
            }
        });

    }

    private void recreateRequestLocationTaskFrags(){
        FragmentManager fm = getSupportFragmentManager();

        for(FriendModel friendModel : myModel.getFriendModels()){
            RequestLocationTaskFrag taskFragment = (RequestLocationTaskFrag) fm.findFragmentByTag(friendModel.getUserId());

            // If the Fragment is non-null, then it is currently being
            // retained across a configuration change.
            if (taskFragment != null) {
                friendModel.setSearchResult(SearchResult.Normal);
                friendModel.setSearchStatus(taskFragment.getCurrentStatus());
                setRequestLocationTaskFragListener(taskFragment);
            }
        }
    }

    private void createRequestLocationTaskFrag(final FriendModel friendModel){
        final FragmentManager fm = getSupportFragmentManager();
        final RequestLocationTaskFrag[] taskFragment = {(RequestLocationTaskFrag) fm.findFragmentByTag(friendModel.getUserId())};
        //not in search
        if (taskFragment[0] == null || taskFragment[0].getCurrentResult() != null) {
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
        fragmentNextAdsCd.friendSearched(new RunnableArgs<Boolean>() {
            @Override
            public void run() {
                if(this.getFirstArg()){
                    friendModel.setSearchResult(SearchResult.Normal);
                    friendModel.setSearchStatus(SearchStatus.Starting);
                    final FragmentManager fm = getSupportFragmentManager();
                    RequestLocationTaskFrag frag =  RequestLocationTaskFrag.newInstance(myModel.getUserId(), friendModel.getUserId());
                    fm.beginTransaction().add(frag, friendModel.getUserId()).commit();
                    setRequestLocationTaskFragListener(frag);
                }
            }
        });
    }


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
                            friendModel.save(_this);
                            myModel.addFriendModel(friendModel, false);
                            foundNew = true;
                        }
                    }

                    if(foundNew){;
                        myModel.sortFriendModels();
                        myModel.commitFriendUserIds();
                        myModel.notifyFriendModelsChanged();
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

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 20 * 1000);
    }

    private void editName(FriendModel friendModel){
        EditNameDialog editNameDialog = new EditNameDialog(this, friendModel, myModel);
        editNameDialog.show();
    }

    private void deleteUser(FriendModel friendModel){
        ConfirmDeleteDialog confirmDeleteDialog = new ConfirmDeleteDialog(this, friendModel, myModel);
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
                        KnownIssueDialog knownIssueDialog = new KnownIssueDialog(ActivityMain.this, PhoneBrand.valueOf(model));
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

        myModel.addMyModelChangedListener(new MyModelChangedListener() {
            @Override
            public void onChanged(MyModel newMyModel, String changedProperty) {
                if(changedProperty.equals("friendModels")){
                    runOnUiThread(new Runnable() {
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
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFriendList();
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction("REFRESH_FRIEND");

        refreshFriendReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String friendId = intent.getStringExtra("userId");
                if(myModel.checkFriendExist(friendId)) {
                    final FriendModel friendModel = myModel.getFriendModelById(friendId);
                    friendModel.load(_this);
                }
            }
        };
        registerReceiver(refreshFriendReceiver, filter);

        IntentFilter filter2 = new IntentFilter();
        filter2.addAction("REFRESH_FRIENDLIST");

        refreshFriendListReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                refreshFriendList();
            }
        };
        registerReceiver(refreshFriendListReceiver, filter2);

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
            }

            @Override
            public void onUpdateResult(String userId, LocationModel locationModel, SearchStatus finalSearchStatus, SearchResult result) {
                FriendModel friendModel = myModel.getFriendModelById(userId);
                if(locationModel != null){
                    friendModel.setLastLocationModel(locationModel);
                }
                friendModel.setRecentlyFinishSearch(true);
                friendModel.setSearchStatus(finalSearchStatus);
                friendModel.setSearchResult(result);
                friendModel.save(_this);

                if(!isAfterSavedInstanceState()){
                    Fragment fragment = getSupportFragmentManager().findFragmentByTag(userId);
                    if(fragment != null)
                        getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                }

                Analytics.logEvent(AnalyticEvent.Search_Result, result.name());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        afterSavedInstanceState = false;

        Logs.show("ActivityMain onResume start");

        Threadings.runInBackground(new Runnable() {
            @Override
            public void run() {
                for(FriendModel friendModel : myModel.getFriendModels()){
                    Fragment fragment = getSupportFragmentManager().findFragmentByTag(friendModel.getUserId());
                    if(fragment != null){
                        if(friendModel.getSearchStatus() == SearchStatus.End){
                            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                        }
                    }
                }

                Threadings.postRunnable(ActivityMain.this, new Runnable() {
                    @Override
                    public void run() {
                        if(friendsAdapter != null) friendsAdapter.notifyDataSetChanged();
                    }
                });

                refreshFriendList();
            }
        });


        PreferenceUtils.delete(this, PreferenceType.AutoNotifiedReceivedIds);

        Logs.show("ActivityMain onResume end");

    }

    @Override
    protected void onPause() {
        super.onPause();
        afterSavedInstanceState = true;
    }

    @Override
    protected void onDestroy() {
        if (refreshFriendReceiver != null) {
            unregisterReceiver(refreshFriendReceiver);
            refreshFriendReceiver = null;
        }
        if (refreshFriendListReceiver != null) {
            unregisterReceiver(refreshFriendListReceiver);
            refreshFriendListReceiver = null;
        }

        super.onDestroy();
    }

    public boolean isAfterSavedInstanceState() {
        return afterSavedInstanceState;
    }


}
