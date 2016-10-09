package com.ffinder.android.adapters;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.ffinder.android.ActivityMap;
import com.ffinder.android.R;
import com.ffinder.android.absint.adapters.IFriendItemListener;
import com.ffinder.android.absint.databases.FirebaseListener;
import com.ffinder.android.enums.FCMMessageType;
import com.ffinder.android.enums.SearchResult;
import com.ffinder.android.enums.Status;
import com.ffinder.android.helpers.FirebaseDB;
import com.ffinder.android.helpers.NotificationSender;
import com.ffinder.android.models.FriendModel;
import com.ffinder.android.models.MyModel;
import com.ffinder.android.utils.DateTimeUtils;
import com.ffinder.android.utils.Strings;
import com.ffinder.android.utils.Threadings;

import java.util.List;

/**
 * Created by SiongLeng on 30/8/2016.
 */
public class FriendsAdapter extends ArrayAdapter<FriendModel> {

    private Activity context;
    private List<FriendModel> friendModels;
    private MyModel myModel;
    private IFriendItemListener friendItemListener;

    public FriendsAdapter(Activity context, @LayoutRes int resource, @NonNull List<FriendModel> objects,
                          MyModel myModel, IFriendItemListener friendItemListener) {
        super(context, resource, objects);
        this.context = context;
        this.friendModels = objects;
        this.myModel = myModel;
        this.friendItemListener = friendItemListener;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder mViewHolder = null;

        if (convertView == null) {
            mViewHolder = new ViewHolder();
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.lvitem_friend, parent, false);

            mViewHolder.txtTextFriend = (TextView) convertView.findViewById(R.id.txtFriend);
            mViewHolder.txtLocation = (TextView) convertView.findViewById(R.id.txtLocation);

            mViewHolder.txtLastUpdated = (TextView) convertView.findViewById(R.id.txtLastUpdated);

            mViewHolder.txtMessage = (TextView) convertView.findViewById(R.id.txtMessage);
            mViewHolder.btnMap = (Button) convertView.findViewById(R.id.btnMap);
            mViewHolder.btnSearch = (Button) convertView.findViewById(R.id.btnSearch);

            mViewHolder.imgViewLoading = (ImageView) convertView.findViewById(R.id.imgViewLoading);
            mViewHolder.imgViewLoading.setBackgroundResource(R.drawable.loading_animation);
            mViewHolder.loadingAnimation =(AnimationDrawable) mViewHolder.imgViewLoading.getBackground();


            convertView.setTag(mViewHolder);

            setListeners(convertView, position);

        }
        updateDesign(convertView, friendModels.get(position));

        return convertView;
    }

    private void updateDesign(View convertView, FriendModel friendModel){
        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.txtTextFriend.setText(friendModel.getName());
        String msg = null, address = null, lastUpdated = null, error = null;

        lastUpdated = String.format((context.getString(R.string.last_updated)),
                DateTimeUtils.convertUnixMiliSecsToDateTimeString(context,
                        friendModel.getLastLocationModel().getTimestampLastUpdatedLong()));

        switch (friendModel.getSearchResult()){
            case Normal:
                msg = friendModel.getSearchStatus().getMessage(context);
                address = friendModel.getLastLocationModel().getAddress();

                updateDesign(viewHolder, msg, address, lastUpdated, friendModel.getSearchResult(),
                        !Strings.isEmpty(friendModel.getLastLocationModel().getLatitude()), friendModel);
                break;
            default:
                address = friendModel.getLastLocationModel().getAddress();
                updateDesign(viewHolder, msg, address, lastUpdated, friendModel.getSearchResult(),
                        !Strings.isEmpty(friendModel.getLastLocationModel().getLatitude()), friendModel);
                break;
        }

    }

    private void updateDesign(final ViewHolder viewHolder, String statusMsg,
                              String address, String lastUpdated, SearchResult error,
                              boolean hasCoordinates, final FriendModel friendModel){

        if(!Strings.isEmpty(statusMsg)){
            //searching
            viewHolder.txtLocation.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
            viewHolder.txtLocation.setText(statusMsg);

            viewHolder.imgViewLoading.setVisibility(View.VISIBLE);
            viewHolder.loadingAnimation.run();
            viewHolder.btnSearch.setVisibility(View.INVISIBLE);
            viewHolder.btnMap.setVisibility(View.INVISIBLE);
            viewHolder.txtLastUpdated.setVisibility(View.INVISIBLE);
        }
        else{
            //search end
            viewHolder.txtLocation.setTextColor(ContextCompat.getColor(context, R.color.colorCaption));

            if(Strings.isEmpty(address) && !hasCoordinates){
                viewHolder.txtLocation.setText(R.string.never_locate_user_msg);
            }
            else{
                viewHolder.txtLocation.setText(address);
            }

            viewHolder.txtLastUpdated.setText(lastUpdated);

            Threadings.delay(300, context, new Runnable() {
                @Override
                public void run() {
                    viewHolder.imgViewLoading.setVisibility(View.GONE);
                    viewHolder.loadingAnimation.stop();
                    viewHolder.btnSearch.setVisibility(View.VISIBLE);
                    viewHolder.btnMap.setVisibility(View.VISIBLE);
                    viewHolder.txtLastUpdated.setVisibility(View.VISIBLE);

                    if(friendModel.isRecentlyFinishSearch()){
//                        Integer colorFrom = ContextCompat.getColor(context, R.color.colorPrimary);
//                        Integer colorTo = ContextCompat.getColor(context, R.color.colorCaption);
//                        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
//                        colorAnimation.setDuration(3000);
//                        colorAnimation.setRepeatCount(1);
//                        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//
//                            @Override
//                            public void onAnimationUpdate(ValueAnimator animator) {
//                                viewHolder.txtLocation.setTextColor((Integer)animator.getAnimatedValue());
//                                viewHolder.txtLastUpdated.setTextColor((Integer)animator.getAnimatedValue());
//                            }
//
//                        });
//                        colorAnimation.start();
                        friendModel.setRecentlyFinishSearch(false);
                    }

                }
            });

        }

        if(error == null || error == SearchResult.Normal){
            viewHolder.txtMessage.setVisibility(View.GONE);
        }
        else{
            viewHolder.txtMessage.setText(error.getMessage(context));
            viewHolder.txtMessage.setVisibility(View.VISIBLE);
        }

        if(hasCoordinates){
            viewHolder.btnMap.setEnabled(true);
        }
        else{
            viewHolder.btnMap.setEnabled(false);
        }

    }

    private void setListeners(final View convertView, final int position){
        final ViewHolder viewHolder = (ViewHolder) convertView.getTag();

        viewHolder.btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FriendModel friendModel = friendModels.get(position);
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

        viewHolder.btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendItemListener.onSearchRequest(friendModels.get(position));
            }
        });
    }


    static class ViewHolder {
        private TextView txtTextFriend, txtLocation, txtLastUpdated, txtMessage;
        private Button btnMap, btnSearch;
        private ImageView imgViewLoading;
        private AnimationDrawable loadingAnimation;
    }

}
