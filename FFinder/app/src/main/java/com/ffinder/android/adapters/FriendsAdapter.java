package com.ffinder.android.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.ffinder.android.ActivityMap;
import com.ffinder.android.R;
import com.ffinder.android.absint.activities.IFriendsAdapterHolder;
import com.ffinder.android.absint.activities.IProfileImagePickerListener;
import com.ffinder.android.absint.adapters.IFriendItemListener;
import com.ffinder.android.enums.OverlayType;
import com.ffinder.android.enums.SearchStatus;
import com.ffinder.android.extensions.ButtonWhite;
import com.ffinder.android.extensions.ProfileImageView;
import com.ffinder.android.extensions.TextFieldWrapper;
import com.ffinder.android.helpers.*;
import com.ffinder.android.models.FriendModel;
import com.nineoldandroids.view.ViewHelper;

import java.util.HashMap;
import java.util.List;

/**
 * Created by SiongLeng on 30/8/2016.
 */
public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {

    private Activity context;
    private List<FriendModel> friendModels;
    private IFriendItemListener friendItemListener;
    private IProfileImagePickerListener profileImagePickerListener;
    private IFriendsAdapterHolder friendsAdapterHolder;
    private HashMap<String, Boolean> mapIsResultShowing;


    public FriendsAdapter(Activity context, @NonNull List<FriendModel> objects,
                          IFriendItemListener friendItemListener,
                          IProfileImagePickerListener profileImagePickerListener,
                          IFriendsAdapterHolder friendsAdapterHolder) {

        this.context = context;
        this.friendModels = objects;
        this.friendItemListener = friendItemListener;
        this.profileImagePickerListener = profileImagePickerListener;
        this.friendsAdapterHolder = friendsAdapterHolder;
        this.mapIsResultShowing = new HashMap();
    }


    @Override
    public FriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.lvitem_friend, parent, false);

        return new FriendViewHolder(this.context, view, mapIsResultShowing, profileImagePickerListener,
                    friendsAdapterHolder);
    }

    @Override
    public void onBindViewHolder(FriendViewHolder holder, int position) {
        FriendModel friendModel = this.friendModels.get(position);
        holder.bindFriendModel(friendModel);
    }


    @Override
    public int getItemCount() {
        return friendModels.size();
    }

    public void reset(){
        mapIsResultShowing.clear();
    }


    public class FriendViewHolder extends RecyclerView.ViewHolder {
        private TextView txtTextFriend,
                txtLocation, txtLastUpdated, txtMessage, txtStatus;
        private ButtonWhite btnMap, btnToggleBlock, btnDelete;
        private ImageButton btnSearch;
        private RelativeLayout layoutResult, layoutStatus, layoutName;
        private ProfileImageView imageViewProfile;
        private FriendModel friendModel;
        private HashMap<String, Boolean> mapIsResultShowing;
        private IProfileImagePickerListener profileImagePickerListener;
        private IFriendsAdapterHolder friendsAdapterHolder;

        public FriendViewHolder(Context context, View itemView, HashMap<String, Boolean> mapIsResultShowing,
                                    IProfileImagePickerListener profileImagePickerListener,
                                    IFriendsAdapterHolder friendsAdapterHolder) {
            super(itemView);

            this.friendsAdapterHolder = friendsAdapterHolder;
            this.profileImagePickerListener = profileImagePickerListener;
            this.mapIsResultShowing = mapIsResultShowing;
            txtTextFriend = (TextView) itemView.findViewById(R.id.txtFriend);
            txtLocation = (TextView) itemView.findViewById(R.id.txtLocation);
            txtLastUpdated = (TextView) itemView.findViewById(R.id.txtLastUpdated);
            txtStatus = (TextView) itemView.findViewById(R.id.txtStatus);
            txtMessage = (TextView) itemView.findViewById(R.id.txtMessage);

            imageViewProfile = (ProfileImageView) itemView.findViewById(R.id.imageViewProfile);

            txtLocation.setText("");
            txtLastUpdated.setText("");
            txtTextFriend.setText("");
            txtMessage.setText("");
            txtStatus.setText("");

            btnMap = (ButtonWhite) itemView.findViewById(R.id.btnMap);
            btnDelete = (ButtonWhite) itemView.findViewById(R.id.btnDelete);
            btnToggleBlock = (ButtonWhite) itemView.findViewById(R.id.btnToggleBlock);
            btnSearch = (ImageButton) itemView.findViewById(R.id.btnSearch);

            layoutResult = (RelativeLayout) itemView.findViewById(R.id.layoutResult);
            layoutStatus = (RelativeLayout) itemView.findViewById(R.id.layoutStatus);
            layoutName = (RelativeLayout) itemView.findViewById(R.id.layoutName);

            imageViewProfile.setProfileImagePickerListener(profileImagePickerListener);

            Logs.show("new view created");

            setListeners();
        }

        public void bindFriendModel(FriendModel friendModel){
            this.friendModel = friendModel;
            updateDesign(friendModel);
        }

        private void updateDesign(FriendModel friendModel){

            imageViewProfile.setShortFormName(Strings.safeSubstring(friendModel.getName().toUpperCase(), 0, 2));
            imageViewProfile.setProfileImageIfAvailable(friendModel);
            txtTextFriend.setText(friendModel.getName());

            //not in search state
            if (friendModel.getSearchStatus() == SearchStatus.End){
                //never search this user before
                if (Strings.isEmpty(friendModel.getLastLocationModel().getAddress())){
                    txtLocation.setText(R.string.never_locate_user_msg);
                    txtLastUpdated.setText("");
                }
                //searched this user before
                else{
                    txtLocation.setText(friendModel.getLastLocationModel().getAddress());
                    txtLastUpdated.setText(DateTimeUtils.convertUnixMiliSecsToDateTimeString(context,
                                             friendModel.getLastLocationModel().getTimestampLastUpdatedLong()));
                }

                //check got error
                if (friendModel.getSearchResult().isError()){
                    AnimateBuilder.fadeIn(context, txtMessage);
                    txtMessage.setText(friendModel.getSearchResult().getMessage(context));
                }
                else{
                    AnimateBuilder.fadeOut(context, txtMessage);
                }

                if(mapIsResultShowing.containsKey(friendModel.getUserId())){
                    return;
                }

                mapIsResultShowing.put(friendModel.getUserId(), true);

                AnimateBuilder.fadeIn(context, layoutResult);
                AnimateBuilder.fadeOut(context, layoutStatus);

            }
            //in search state currently
            else{

                txtStatus.setText(friendModel.getSearchStatus().getMessage(context));

                if(!mapIsResultShowing.containsKey(friendModel.getUserId())){
                    return;
                }

                mapIsResultShowing.remove(friendModel.getUserId());

                AnimateBuilder.fadeIn(context, layoutStatus);
                AnimateBuilder.fadeOut(context, layoutResult);

            }


        }


        private void setListeners(){
            btnMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ActivityMap.class);
                    intent.putExtra("username", friendModel.getName());
                    intent.putExtra("latitude", friendModel.getLastLocationModel().getLatitude());
                    intent.putExtra("longitude", friendModel.getLastLocationModel().getLongitude());
                    intent.putExtra("address", friendModel.getLastLocationModel().getAddress());
                    intent.putExtra("datetime", DateTimeUtils.convertUnixMiliSecsToDateTimeString(context,
                            friendModel.getLastLocationModel().getTimestampLastUpdatedLong()));

                    context.startActivity(intent);
                }
            });

            btnSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    friendItemListener.onSearchRequest(friendModel);
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
                                    friendsAdapterHolder.onDeleteFriend(friendModel);
                                }
                            }).show();
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

    }


}
