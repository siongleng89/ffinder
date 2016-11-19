package com.ffinder.android;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.*;
import com.ffinder.android.absint.activities.MyActivityAbstract;
import com.ffinder.android.absint.controls.INoCreditsListener;
import com.ffinder.android.absint.databases.FirebaseListener;
import com.ffinder.android.absint.helpers.IAdsMediationListener;
import com.ffinder.android.absint.helpers.RestfulListener;
import com.ffinder.android.controls.NoCreditsDialog;
import com.ffinder.android.enums.*;
import com.ffinder.android.helpers.*;
import com.ffinder.android.models.MyModel;

/**
 * Created by sionglengho on 10/11/16.
 */
public class LayoutNextAdsCd {

    private MyActivityAbstract activity;
    private TextView txtNextAdsCount, txtSearchLeft;
    private RelativeLayout layoutCount, layoutControl;
    private ImageView imgViewTick;
    private Integer currentNextAdsCount;
    private boolean completeProcessing;
    private int addingCount;


    public LayoutNextAdsCd(MyActivityAbstract activity){
        this.activity = activity;

        AdsMediation.init(activity);
    }

    public View getView(){
        View nextAdsFragmentView = activity.getLayoutInflater().inflate(
                R.layout.fragment_next_ads_cd, null, false);

        nextAdsFragmentView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));

        txtNextAdsCount = (TextView) nextAdsFragmentView.findViewById(R.id.txtNextAdsCount);
        txtSearchLeft = (TextView) nextAdsFragmentView.findViewById(R.id.txtSearchLeft);
        imgViewTick = (ImageView) nextAdsFragmentView.findViewById(R.id.imgViewTick);

        layoutCount = (RelativeLayout) nextAdsFragmentView.findViewById(R.id.layoutCount);
        layoutControl = (RelativeLayout) nextAdsFragmentView.findViewById(R.id.layoutControl);

        txtSearchLeft.setVisibility(View.INVISIBLE);
        imgViewTick.setVisibility(View.GONE);
        txtNextAdsCount.setVisibility(View.GONE);

        setListeners();
        quickBoot();
        return nextAdsFragmentView;
    }

    public void onResume() {
        initiate();
    }

    private void quickBoot(){
        if(fastCheckIsVip()){
            txtSearchLeft.setText(R.string.vip_title);
            txtSearchLeft.setVisibility(View.VISIBLE);
            imgViewTick.setVisibility(View.VISIBLE);
            txtNextAdsCount.setVisibility(View.GONE);
        }
        else{
            String storedCredits = PreferenceUtils.get(getMyActivity(), PreferenceType.NextAdsCount);
            if(Strings.isNumeric(storedCredits)){
                changeNextAdsCount(Integer.valueOf(storedCredits), false);
                txtSearchLeft.setText(R.string.search_left);
                txtSearchLeft.setVisibility(View.VISIBLE);
                txtNextAdsCount.setVisibility(View.VISIBLE);
                imgViewTick.setVisibility(View.GONE);
            }
        }
    }


    private void initiate(){
        checkIsVip(new RunnableArgs<Boolean>() {
            @Override
            public void run() {
                if(this.getFirstArg()){
                    Threadings.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            completeProcessing = true;
                            txtSearchLeft.setText(R.string.vip_title);
                            AnimateBuilder.fadeIn(getMyActivity(), txtSearchLeft);
                            AnimateBuilder.fadeIn(getMyActivity(), imgViewTick);
                            txtNextAdsCount.setVisibility(View.GONE);
                        }
                    });
                }
                else{
                    AdsMediation.preload();
                    FirebaseDB.getNextAdsCount(activity.getMyModel().getUserId(), new FirebaseListener<Integer>(Integer.class) {
                        @Override
                        public void onResult(Integer result, Status status) {
                            completeProcessing = true;
                            boolean needSaveToDb = false;
                            if(addingCount > 0){
                                result -= addingCount;
                                addingCount = 0;
                                needSaveToDb = true;
                            }
                            changeNextAdsCount(result, false);
                            Threadings.postRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    txtSearchLeft.setText(R.string.search_left);
                                    AnimateBuilder.fadeIn(getMyActivity(), txtSearchLeft);
                                    AnimateBuilder.fadeIn(getMyActivity(), txtNextAdsCount);
                                    imgViewTick.setVisibility(View.GONE);
                                }
                            });


                            if(needSaveToDb){
                                FirebaseDB.setNextAdsCount(activity.getMyModel().getUserId(), getCurrentNextAdsCount(), new FirebaseListener() {
                                    @Override
                                    public void onResult(Object result, Status status) {
                                    }
                                });
                            }

                        }
                    });
                }
            }
        });
    }

    private void checkIsVip(RunnableArgs<Boolean> onResult){
        ((MyApplication) getMyActivity().getApplication()).getVipAndProductsHelpers().isInVipPeriod(onResult);
    }

    private boolean fastCheckIsVip(){
        String isVip = PreferenceUtils.get(activity, PreferenceType.ISVip);
        return (!Strings.isEmpty(isVip) && isVip.equals("1"));
    }

    public void friendSearched(final RunnableArgs<Boolean> canRun){
        if(!completeProcessing){
            addingCount++;

            boolean noCredits = false;
            String storedCredits = PreferenceUtils.get(getMyActivity(), PreferenceType.NextAdsCount);
            if(Strings.isNumeric(storedCredits)){

                changeNextAdsCount(Math.max(0, Integer.valueOf(storedCredits) - 1), false);

                if(Integer.valueOf(storedCredits) <= 0){
                    noCredits = true;
                }
            }

            canRun.run(!noCredits);
            if(noCredits){
                new NoCreditsDialog(getMyActivity(), activity.getMyModel(),
                        getCurrentNextAdsCount(), new INoCreditsListener() {
                    @Override
                    public void requestWatchAds() {
                        showAds();
                    }
                }).show();
            }
            return;
        }

        checkIsVip(new RunnableArgs<Boolean>() {
            @Override
            public void run() {
                if(!this.getFirstArg()){
                    if(getCurrentNextAdsCount() <= 0){
                        canRun.run(false);
                        new NoCreditsDialog(getMyActivity(), activity.getMyModel(),
                                getCurrentNextAdsCount(), new INoCreditsListener() {
                            @Override
                            public void requestWatchAds() {
                                showAds();
                            }
                        }).show();
                        Analytics.logEvent(AnalyticEvent.Search_Failed_No_Credit);
                    }
                    else{
                        canRun.run(true);
                        changeNextAdsCount(getCurrentNextAdsCount() - 1, true);
                        FirebaseDB.setNextAdsCount(activity.getMyModel().getUserId(),
                                getCurrentNextAdsCount(), new FirebaseListener() {
                            @Override
                            public void onResult(Object result, Status status) {
                                if(status == Status.Failed){
                                    initiate();
                                }
                            }
                        });
                        Analytics.logEvent(AnalyticEvent.Search_Deduct_Credit);
                    }
                }
                else{
                    canRun.run(true);
                    Analytics.logEvent(AnalyticEvent.Search_Using_VIP);
                }
            }
        });
    }

    private void refreshNextAdsCount(final int newAdsCount, final boolean animate){
        Threadings.postRunnable(new Runnable() {
            @Override
            public void run() {
                Logs.show("refresh next ads count: " + newAdsCount);

                String finalString = "";

                finalString = String.valueOf(Math.max(newAdsCount, 0));

                if (animate){
                    final String finalString1 = finalString;
                    AnimateBuilder.build(getMyActivity(), txtNextAdsCount)
                            .setDurationMs(200).setValue(0).setAnimateType(AnimateType.alpha)
                            .setFinishCallback(new Runnable() {
                                @Override
                                public void run() {
                                    txtNextAdsCount.setText(finalString1);
                                    AnimateBuilder.fadeIn(getMyActivity(), txtNextAdsCount);

                                }
                            }).start();
                }
                else{
                    txtNextAdsCount.setText(finalString);
                }


            }
        });
    }

    private void showAds(){
        if(DeviceInfo.isNetworkAvailable(activity)){

            AdsMediation.showAds(new IAdsMediationListener() {
                @Override
                public void onResult(boolean success) {
                    RestfulService.adsWatched(activity.getMyModel().getUserId(), new RestfulListener<String>() {
                        @Override
                        public void onResult(String result, Status status) {
                            if(status == Status.Success && !Strings.isEmpty(result) && Strings.isNumeric(result)){
                                changeNextAdsCount(Integer.valueOf(result), true);
                            }
                        }
                    });
                    if(success){
                        Analytics.logEvent(AnalyticEvent.Watch_Ads);
                    }
                    else{
                        Toast.makeText(activity,
                                R.string.no_ads_available_toast_msg, Toast.LENGTH_LONG).show();
                        Analytics.logEvent(AnalyticEvent.No_Ads_Available);
                    }
                }
            });
        }
        else{
            OverlayBuilder.build(activity).setOverlayType(OverlayType.OkOnly)
                    .setContent(activity.getString(R.string.no_connection_msg))
                    .show();
        }
    }

    private void changeNextAdsCount(int newCount, boolean animate){
        setCurrentNextAdsCount(newCount);
        refreshNextAdsCount(newCount, animate);
    }

    public Integer getCurrentNextAdsCount() {
        if(currentNextAdsCount == null){
            String value = PreferenceUtils.get(getMyActivity(), PreferenceType.NextAdsCount);
            if(Strings.isNumeric(value)){
                currentNextAdsCount = Integer.valueOf(value);
            }
            else{
                initiate();
            }
        }
        return currentNextAdsCount;
    }

    public void setCurrentNextAdsCount(Integer value){
        PreferenceUtils.put(getMyActivity(), PreferenceType.NextAdsCount, value.toString());
        currentNextAdsCount = value;
    }

    private void setListeners(){
        layoutControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fastCheckIsVip()){
                    Intent intent = new Intent(getMyActivity(), ActivityVip.class);
                    intent.putExtra("userId", activity.getMyModel().getUserId());
                    getMyActivity().startActivity(intent);
                }
                else{
                    new NoCreditsDialog(getMyActivity(), activity.getMyModel(),
                            getCurrentNextAdsCount(), new INoCreditsListener() {
                        @Override
                        public void requestWatchAds() {
                            showAds();
                        }
                    }).show();
                }
            }
        });
    }


    private AppCompatActivity getMyActivity(){
        return activity;
    }


}
