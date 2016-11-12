package com.ffinder.android;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.ffinder.android.absint.controls.INoCreditsListener;
import com.ffinder.android.absint.databases.FirebaseListener;
import com.ffinder.android.absint.helpers.RestfulListener;
import com.ffinder.android.controls.NoCreditsDialog;
import com.ffinder.android.enums.*;
import com.ffinder.android.helpers.*;
import com.ffinder.android.models.MyModel;
import com.ffinder.android.tasks.AdsFrag;

/**
 * Created by sionglengho on 10/11/16.
 */
public class LayoutNextAdsCd {

    private AppCompatActivity activity;
    private TextView txtNextAdsCount, txtSearchLeft;
    private RelativeLayout layoutCount, layoutControl;
    private ImageView imgViewTick;
    private MyModel myModel;
    private AdsFrag adsFrag;
    private Integer currentNextAdsCount;
    private boolean completeProcessing;
    private int addingCount;
    private boolean afterSavedInstanceState;


    public LayoutNextAdsCd(AppCompatActivity activity, MyModel myModel){
        this.activity = activity;
        this.myModel = myModel;
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


    public void onPause() {
        afterSavedInstanceState = true;
    }

    public void onResume() {
        afterSavedInstanceState = false;
        recreateAdsFrag();
        initiate();
    }

    private void quickBoot(){
        fastCheckIsVip(new RunnableArgs<Boolean>() {
            @Override
            public void run() {
                if(this.getFirstArg()){
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
                    }
                }
            }
        });
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
                    adsFrag.preload();
                    FirebaseDB.getNextAdsCount(myModel.getUserId(), new FirebaseListener<Integer>(Integer.class) {
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
                                }
                            });


                            if(needSaveToDb){
                                FirebaseDB.setNextAdsCount(myModel.getUserId(), getCurrentNextAdsCount(), new FirebaseListener() {
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

    private void fastCheckIsVip(RunnableArgs<Boolean> onResult){
        String isVip = PreferenceUtils.get(activity, PreferenceType.ISVip);
        if(!Strings.isEmpty(isVip) && isVip.equals("1")){
            onResult.run(true);
        }
        else{
            onResult.run(false);
        }
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
                new NoCreditsDialog(getMyActivity(), myModel,
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
                        new NoCreditsDialog(getMyActivity(), myModel,
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
                        FirebaseDB.setNextAdsCount(myModel.getUserId(), getCurrentNextAdsCount(), new FirebaseListener() {
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

    private void recreateAdsFrag(){
        if(afterSavedInstanceState) return;

        FragmentManager fm = getMyActivity().getSupportFragmentManager();
        adsFrag = (AdsFrag) fm.findFragmentByTag("adsFrag");
        //not created
        if (adsFrag == null) {
            adsFrag = AdsFrag.newInstance(activity);
            fm.beginTransaction().add(adsFrag, "adsFrag").commit();
        }
        else{
            adsFrag.setActivity(activity);
        }
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

            final Runnable toRun = new Runnable() {
                @Override
                public void run() {
                    final AlertDialog dialog = OverlayBuilder.build(getMyActivity())
                            .setOverlayType(OverlayType.Loading)
                            .setContent(getMyActivity().getString(R.string.loading))
                            .show();

                    adsFrag.showAds(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            RestfulService.adsWatched(myModel.getUserId(), new RestfulListener<String>() {
                                @Override
                                public void onResult(String result, Status status) {
                                    if(status == Status.Success && !Strings.isEmpty(result) && Strings.isNumeric(result)){
                                        changeNextAdsCount(Integer.valueOf(result), true);
                                    }
                                }
                            });
                        }
                    });
                    Analytics.logEvent(AnalyticEvent.Watch_Ads);
                }
            };


            if(DeviceInfo.isUsingDataNetwork(activity)){
                String dontRemind = PreferenceUtils.get(activity,
                                    PreferenceType.DontRemindWatchAdsWarning);
                if(Strings.isEmpty(dontRemind) || !dontRemind.equals("1")){
                    final OverlayBuilder builder = OverlayBuilder.build(activity);
                    builder.setOverlayType(OverlayType.OkCancel)
                            .setContent(activity.getString(R.string.confirm_watch_ads_on_3g))
                            .setCheckboxTitle(activity.getString(R.string.don_remind_me_again))
                            .setRunnables(new Runnable() {
                                @Override
                                public void run() {
                                    toRun.run();
                                }
                            })
                            .setOnDismissRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    if(builder.isChecked()){
                                        PreferenceUtils.put(activity,
                                                PreferenceType.DontRemindWatchAdsWarning, "1");
                                    }
                                }
                            });
                    builder.show();

                }
                else{
                    toRun.run();
                }
            }
            else{
                toRun.run();
            }

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
                fastCheckIsVip(new RunnableArgs<Boolean>() {
                    @Override
                    public void run() {
                        boolean isVip = this.getFirstArg();
                        if(isVip){
                            Intent intent = new Intent(getMyActivity(), ActivityVip.class);
                            intent.putExtra("userId", myModel.getUserId());
                            getMyActivity().startActivity(intent);
                        }
                        else{
                            new NoCreditsDialog(getMyActivity(), myModel,
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
        });
    }


    private AppCompatActivity getMyActivity(){
        return activity;
    }


}
