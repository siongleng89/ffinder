package com.ffinder.android.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.ffinder.android.ActivityMap;
import com.ffinder.android.ActivityShareKey;
import com.ffinder.android.FragmentSearchButtonsHolder;
import com.ffinder.android.R;
import com.ffinder.android.absint.activities.IFriendsAdapterHolder;
import com.ffinder.android.absint.activities.IProfileImagePickerListener;
import com.ffinder.android.absint.activities.MyActivityAbstract;
import com.ffinder.android.absint.adapters.IFriendItemListener;
import com.ffinder.android.enums.*;
import com.ffinder.android.extensions.ButtonSearch;
import com.ffinder.android.extensions.ButtonWhite;
import com.ffinder.android.extensions.ProfileImageView;
import com.ffinder.android.extensions.TextFieldWrapper;
import com.ffinder.android.helpers.*;
import com.ffinder.android.models.FriendModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SiongLeng on 30/8/2016.
 */
public class FriendsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private MyActivityAbstract context;
    private List<FriendModel> friendModels;
    private IFriendItemListener friendItemListener;
    private IProfileImagePickerListener profileImagePickerListener;
    private IFriendsAdapterHolder friendsAdapterHolder;
    private ArrayList<String> animateOnNextUpdateFriendIds;
    private FragmentSearchButtonsHolder fragmentSearchButtonsHolder;
    private int lastPosition = -1;
    private final int FRIEND_ITEM_ROW = 0, FRIEND_ERROR_ROW = 1, ASK_ADD_FRIEND_ROW = 2;


    public FriendsAdapter(MyActivityAbstract context, @NonNull List<FriendModel> objects,
                          IFriendItemListener friendItemListener,
                          IProfileImagePickerListener profileImagePickerListener,
                          IFriendsAdapterHolder friendsAdapterHolder) {

        this.context = context;
        this.friendModels = objects;
        this.friendItemListener = friendItemListener;
        this.profileImagePickerListener = profileImagePickerListener;
        this.friendsAdapterHolder = friendsAdapterHolder;
        this.animateOnNextUpdateFriendIds = new ArrayList();


        FragmentManager fm = context.getSupportFragmentManager();
        fragmentSearchButtonsHolder = (FragmentSearchButtonsHolder)
                         fm.findFragmentByTag(FragmentSearchButtonsHolder.class.getName());

        if(fragmentSearchButtonsHolder == null){
            fragmentSearchButtonsHolder = new FragmentSearchButtonsHolder();
            fm.beginTransaction().add(fragmentSearchButtonsHolder,
                    FragmentSearchButtonsHolder.class.getName()).commit();
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == FRIEND_ITEM_ROW){
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.lvitem_friend, parent, false);

            return new FriendViewHolder(this.context, view, profileImagePickerListener,
                    friendsAdapterHolder, animateOnNextUpdateFriendIds, fragmentSearchButtonsHolder);
        }
        else if(viewType == FRIEND_ERROR_ROW){
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.lvitem_layout_dummy, parent, false);
            return new FriendErrorViewHolder(this.context, view);
        }
        else if(viewType == ASK_ADD_FRIEND_ROW){
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.lvitem_ask_add_friend, parent, false);

            return new AskAddFriendViewHolder(this.context, view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof FriendViewHolder){
            FriendModel friendModel = this.friendModels.get(position / 2);
            ((FriendViewHolder) holder).bindFriendModel(friendModel);
        }
        else if(holder instanceof FriendErrorViewHolder){
            FriendModel friendModel = this.friendModels.get(position / 2);
            ((FriendErrorViewHolder) holder).updateDesign(friendModel, position);
        }
        else if(holder instanceof AskAddFriendViewHolder){
            ((AskAddFriendViewHolder) holder).updateDesign();
        }

    }

    @Override
    public int getItemViewType(int position) {
        if(position == friendModels.size() * 2 - 2){
            return ASK_ADD_FRIEND_ROW;
        }
        else{
            if(position % 2 == 0){
                return FRIEND_ITEM_ROW;
            }
            else{
                return FRIEND_ERROR_ROW;
            }
        }
    }

    @Override
    public int getItemCount() {
        return friendModels.size() * 2;
    }

    public void rowChanged(int row){
        notifyItemChanged(row * 2);
        notifyItemChanged(row * 2 + 1);
    }


    public class AskAddFriendViewHolder extends RecyclerView.ViewHolder{

        private TextView txtViewTapToAdd;

        public AskAddFriendViewHolder(final Context context, View itemView) {
            super(itemView);

            txtViewTapToAdd = (TextView) itemView.findViewById(R.id.txtViewTapToAdd);

            txtViewTapToAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, ActivityShareKey.class);
                    context.startActivity(intent);
                }
            });
        }

        public void updateDesign(){

        }


    }

    public class FriendErrorViewHolder extends RecyclerView.ViewHolder{

        private View itemView;
        private RelativeLayout layoutMain, layoutClose;
        private TextView txtMessage;
        private Context context;

        public FriendErrorViewHolder(Context context, View itemView) {
            super(itemView);

            this.itemView = itemView;
            this.context = context;
            layoutMain = (RelativeLayout) itemView.findViewById(R.id.layoutMain);

        }

        public void updateDesign(FriendModel friendModel, int position){
            if(friendModel.getSearchResult().isError() && !friendModel.isClosedError()){
                //animate show if necessary
                layoutMain.removeAllViews();

                View view = LayoutInflater.from(context)
                        .inflate(R.layout.lvitem_friend_error, layoutMain, true);

                layoutClose = (RelativeLayout) view.findViewById(R.id.layoutClose);
                txtMessage = (TextView) view.findViewById(R.id.txtMessage);
                txtMessage.setText(friendModel.getSearchResult().getMessage(context));

                setListener(friendModel);
            }
            else{
                layoutMain.removeAllViews();
            }

        }

        public void setListener(final FriendModel friendModel){
            layoutClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    friendModel.setClosedError(true);
                    friendModel.save(context);
                    friendsAdapterHolder.updateFriendsListAdapter(friendModel.getUserId());
                }
            });
        }

    }



    public class FriendViewHolder extends RecyclerView.ViewHolder {
        public View itemView;
        private TextView txtTextFriend,
                txtLocation, txtStatus;
        private ButtonWhite btnMap, btnToggleBlock, btnDelete;
        private RelativeLayout layoutResult, layoutStatus, layoutName, layoutRight;
        private ProfileImageView imageViewProfile;
        private FriendModel friendModel;
        private IProfileImagePickerListener profileImagePickerListener;
        private IFriendsAdapterHolder friendsAdapterHolder;
        private ArrayList<String> animateOnNextUpdateFriendIds;
        private FragmentSearchButtonsHolder fragmentSearchButtonsHolder;

        public FriendViewHolder(Context context, View itemView,
                                    IProfileImagePickerListener profileImagePickerListener,
                                    IFriendsAdapterHolder friendsAdapterHolder,
                                    ArrayList<String> animateOnNextUpdateFriendIds,
                                    FragmentSearchButtonsHolder fragmentSearchButtonsHolder) {
            super(itemView);

            this.itemView = itemView;
            this.fragmentSearchButtonsHolder = fragmentSearchButtonsHolder;
            this.friendsAdapterHolder = friendsAdapterHolder;
            this.animateOnNextUpdateFriendIds = animateOnNextUpdateFriendIds;
            this.profileImagePickerListener = profileImagePickerListener;

            txtTextFriend = (TextView) itemView.findViewById(R.id.txtFriend);
            txtLocation = (TextView) itemView.findViewById(R.id.txtLocation);
            txtStatus = (TextView) itemView.findViewById(R.id.txtStatus);

            imageViewProfile = (ProfileImageView) itemView.findViewById(R.id.imageViewProfile);

            txtLocation.setText("");
            txtTextFriend.setText("");
            txtStatus.setText("");

            btnMap = (ButtonWhite) itemView.findViewById(R.id.btnMap);
            btnDelete = (ButtonWhite) itemView.findViewById(R.id.btnDelete);
            btnToggleBlock = (ButtonWhite) itemView.findViewById(R.id.btnToggleBlock);

            layoutResult = (RelativeLayout) itemView.findViewById(R.id.layoutResult);
            layoutStatus = (RelativeLayout) itemView.findViewById(R.id.layoutStatus);
            layoutName = (RelativeLayout) itemView.findViewById(R.id.layoutName);
            layoutRight = (RelativeLayout) itemView.findViewById(R.id.layoutRight);

            imageViewProfile.setProfileImagePickerListener(profileImagePickerListener);

            Logs.show("new view created");

            setListeners();
        }

        public void bindFriendModel(FriendModel friendModel){
            this.friendModel = friendModel;
            updateDesign(friendModel);
        }


        private void updateDesign(FriendModel friendModel){

            Logs.show("updating " + friendModel.getName() + " status is:" + friendModel.getSearchStatus());

            ButtonSearch buttonSearch = fragmentSearchButtonsHolder.getButton(friendModel.getUserId());
            layoutRight.addView(buttonSearch);

            setButtonSearchListener(buttonSearch, friendModel);

            imageViewProfile.setShortFormName(Strings.safeSubstring(friendModel.getName().toUpperCase(), 0, 2));
            imageViewProfile.setProfileImageIfAvailable(friendModel);
            txtTextFriend.setText(friendModel.getName());

            //update block button toggle design accordingly
            if(friendModel.isBlockSearch()){
                btnToggleBlock.setImageSrcDrawable(R.drawable.disallow_search_icon);
                btnToggleBlock.setSelected(true,
                        animateOnNextUpdateFriendIds.contains(friendModel.getUserId()));
            }
            else{
                btnToggleBlock.setImageSrcDrawable(R.drawable.allow_search_icon);
                btnToggleBlock.setSelected(false,
                        animateOnNextUpdateFriendIds.contains(friendModel.getUserId()));
            }

            animateOnNextUpdateFriendIds.remove(friendModel.getUserId());

            //disable map button if no coordinates
            if(friendModel.getLastLocationModel().hasCoordinates()){
                btnMap.setDisabled(false);
            }
            else{
                btnMap.setDisabled(true);
            }

            //not in search state
            if (friendModel.getSearchStatus() == SearchStatus.End){
                //never search this user before
                if (Strings.isEmpty(friendModel.getLastLocationModel().getAddress())){
                    txtLocation.setText(R.string.never_locate_user_msg);
                    buttonSearch.setLastUpdated(context.getString(R.string.never));
                }
                //searched this user before
                else{
                    txtLocation.setText(friendModel.getLastLocationModel().getAddress());
                    buttonSearch.setLastUpdated(DateTimeUtils.convertUnixMiliSecsToDateTimeString(context,
                                             friendModel.getLastLocationModel().getTimestampLastUpdatedLong()));
                }

                //check got error
                if (friendModel.getSearchResult().isError()){
                    //recently searched failed, resort to auto notf
                    if(friendModel.isRecentlyFinishSearch()){
                        friendModel.setRecentlyFinishSearch(false);
                        if(friendModel.getSearchResult().errorTriggeredAutoNotification()){
                            buttonSearch.changeSearchState(SearchAnimationState.Ending, "autoSearching");
                        }
                        else{
                            buttonSearch.changeSearchState(SearchAnimationState.Ending);
                        }

                    }
                    else{
                        if(friendModel.getSearchResult().errorTriggeredAutoNotification()){
                            buttonSearch.changeSearchState(SearchAnimationState.AutoSearching);
                        }
                        else{
                            buttonSearch.changeSearchState(SearchAnimationState.Ending);
                        }
                    }

                }
                else{

                    //recently searched success
                    if(friendModel.isRecentlyFinishSearch()){
                        friendModel.setRecentlyFinishSearch(false);
                        buttonSearch.changeSearchState(SearchAnimationState.SearchSuccess);
                    }
                    else{
                        //is same day
                        if(DateTimeUtils.checkDaysDifference(
                                friendModel.getLastLocationModel().getTimestampLastUpdatedLong()) == 0){
                            buttonSearch.changeSearchState(SearchAnimationState.Satisfied);
                        }
                    }

                }

                layoutResult.setVisibility(View.VISIBLE);
                layoutStatus.setVisibility(View.GONE);

//                AnimateBuilder.fadeIn(context, layoutResult);
//                AnimateBuilder.fadeOut(context, layoutStatus);
            }
            //in search state currently
            else{

                if(friendModel.getTimeoutPhase() == 0){
                    buttonSearch.changeSearchState(SearchAnimationState.Starting);
                }
                else if(friendModel.getTimeoutPhase() == 1){
                    buttonSearch.changeSearchState(SearchAnimationState.SearchingTrouble);
                }
                else if(friendModel.getTimeoutPhase() == 2){
                    buttonSearch.changeSearchState(SearchAnimationState.SearchingDizzy);
                }


                txtStatus.setText(friendModel.getSearchStatus().getMessage(context));

                layoutResult.setVisibility(View.GONE);
                layoutStatus.setVisibility(View.VISIBLE);

//                AnimateBuilder.fadeIn(context, layoutStatus);
//                AnimateBuilder.fadeOut(context, layoutResult);
            }


        }

        private void setListeners(){
            btnMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ActivityMap.class);
                    intent.putExtra("userId", friendModel.getUserId());
                    intent.putExtra("username", friendModel.getName());
                    intent.putExtra("latitude", friendModel.getLastLocationModel().getLatitude());
                    intent.putExtra("longitude", friendModel.getLastLocationModel().getLongitude());
                    intent.putExtra("address", friendModel.getLastLocationModel().getAddress());
                    intent.putExtra("datetime", DateTimeUtils.convertUnixMiliSecsToDateTimeString(context,
                            friendModel.getLastLocationModel().getTimestampLastUpdatedLong()));

                    context.startActivity(intent);
                }
            });

            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    OverlayBuilder.build(context)
                            .setOverlayType(OverlayType.OkCancel)
                            .setTitle(String.format(context.getString(R.string.delete_user_title),
                                    friendModel.getName()))
                            .setContent(context.getString(R.string.confirm_delete_user_msg))
                            .setRunnables(new Runnable() {
                                @Override
                                public void run() {
                                    fragmentSearchButtonsHolder.remove(friendModel.getUserId());
                                    friendsAdapterHolder.onDeleteFriend(friendModel);
                                }
                            }).show();
                }
            });

            btnToggleBlock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Runnable blockRunnable = new Runnable() {
                        @Override
                        public void run() {
                            friendModel.setBlockSearch(!friendModel.isBlockSearch());
                            friendModel.save(context);

                            animateOnNextUpdateFriendIds.add(friendModel.getUserId());

                            friendsAdapterHolder.updateFriendsListAdapter(friendModel.getUserId());

                            FirebaseDB.changeBlockUser(context.getMyModel().getUserId(),
                                    friendModel.getUserId(), friendModel.isBlockSearch(), null);

                            Analytics.logEvent(AnalyticEvent.Change_Block_User,
                                    "BlockUser: " + String.valueOf(friendModel.isBlockSearch()));

                        }
                    };

                    boolean dontShowAgain = !Strings.isEmpty(PreferenceUtils.get(context,
                                                PreferenceType.DontShowAgainBlockUserWarning));

                    //dont need to show warning overlay
                    // if user is unblocking
                    // or user already tick dont show again before
                    if(friendModel.isBlockSearch() || dontShowAgain){
                        blockRunnable.run();
                    }
                    else{
                        final OverlayBuilder builder = OverlayBuilder.build(context);
                        builder.setTitle(String.format(context.getString(R.string.block_dialog_title),
                                friendModel.getName()))
                                .setContent(String.format(context.getString(R.string.block_dialog_content),
                                        friendModel.getName(), friendModel.getName()))
                                .setOverlayType(OverlayType.OkCancel)
                                .setCheckboxTitle(context.getString(R.string.dont_show_this_again))
                                .setRunnables(blockRunnable)
                                .setOnDismissRunnable(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(builder.isChecked()){
                                            PreferenceUtils.put(context,
                                                    PreferenceType.DontShowAgainBlockUserWarning, "1");
                                        }
                                    }
                                });
                        builder.show();
                    }


                }
            });

            imageViewProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    profileImagePickerListener.pickImageForFriend(friendModel.getUserId());
                }
            });

            layoutName.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                        txtTextFriend.setPaintFlags(txtTextFriend.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
                    }
                    else if (event.getAction() == android.view.MotionEvent.ACTION_UP ||
                            event.getAction() == MotionEvent.ACTION_CANCEL) {
                        txtTextFriend.setPaintFlags(0);
                    }
                    return false;
                }
            });

            layoutName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    View viewInflated = LayoutInflater.from(context).inflate(R.layout.layout_edit_name, null);
                    final TextFieldWrapper newNameWrapper = (TextFieldWrapper)
                                                    viewInflated.findViewById(R.id.newNameWrapper);
                    newNameWrapper.setText(friendModel.getName());

                    OverlayBuilder.build(context)
                            .setOverlayType(OverlayType.OkCancel)
                            .setContentView(viewInflated)
                            .setTitle(context.getString(R.string.edit_name_title))
                            .setRunnables(new Runnable() {
                                @Override
                                public void run() {
                                    if(!Strings.isEmpty(newNameWrapper.getText())
                                            && !newNameWrapper.getText().equals(friendModel.getName())){
                                        friendsAdapterHolder.onEditFriend(friendModel,
                                                newNameWrapper.getText());
                                    }
                                }
                            }).show();
                }
            });

        }

        private void setButtonSearchListener(final ButtonSearch buttonSearch, final FriendModel friendModel){
            buttonSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    friendItemListener.onSearchRequest(friendModel);
                }
            });
        }



    }


}
