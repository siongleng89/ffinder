package com.ffinder.android;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
 * Created by SiongLeng on 30/8/2016.
 */
public class FragmentNextAdsCd extends Fragment {

    private TextView txtNextAdsCount, txtSearchLeft;
    private RelativeLayout layoutCount, layoutControl;
    private ImageView imgViewTick;
    private MyModel myModel;
    private AdsFrag adsFrag;
    private Integer currentNextAdsCount;
    private boolean completeProcessing;
    private int addingCount;
    private boolean afterSavedInstanceState;

    public static FragmentNextAdsCd newInstance() {
        return new FragmentNextAdsCd();
    }


    public FragmentNextAdsCd() {
    }


    public void setMyModel(MyModel newModel){
        this.myModel = newModel;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View nextAdsFragmentView = inflater.inflate(R.layout.fragment_next_ads_cd, container, false);

        txtNextAdsCount = (TextView) nextAdsFragmentView.findViewById(R.id.txtNextAdsCount);
        txtSearchLeft = (TextView) nextAdsFragmentView.findViewById(R.id.txtSearchLeft);
        imgViewTick = (ImageView) nextAdsFragmentView.findViewById(R.id.imgViewTick);

        layoutCount = (RelativeLayout) nextAdsFragmentView.findViewById(R.id.layoutCount);
        layoutControl = (RelativeLayout) nextAdsFragmentView.findViewById(R.id.layoutControl);

        txtSearchLeft.setVisibility(View.INVISIBLE);
        imgViewTick.setVisibility(View.GONE);
        txtNextAdsCount.setVisibility(View.GONE);

        setListeners();
        return nextAdsFragmentView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onPause() {
        super.onPause();
        afterSavedInstanceState = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        afterSavedInstanceState = false;
        initiate();
    }

    private void initiate(){
        checkIsVip(new RunnableArgs<Boolean>() {
            @Override
            public void run() {
                if(this.getFirstArg()){
                    Threadings.postRunnable(getActivity(), new Runnable() {
                        @Override
                        public void run() {
                            completeProcessing = true;
                            txtSearchLeft.setText(R.string.vip_member);
                            AnimateBuilder.fadeIn(getActivity(), txtSearchLeft);
                            AnimateBuilder.fadeIn(getActivity(), imgViewTick);
                        }
                    });

                }
                else{
                    recreateAdsFrag();
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
                            txtSearchLeft.setText(R.string.search_left);
                            AnimateBuilder.fadeIn(getActivity(), txtSearchLeft);
                            AnimateBuilder.fadeIn(getActivity(), txtNextAdsCount);

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
        ((MyApplication) getActivity().getApplication()).getVipAndProductsHelpers().isInVipPeriod(onResult);
    }

    public void friendSearched(final RunnableArgs<Boolean> canRun){
        if(!completeProcessing){
            addingCount++;
            canRun.run(true);
            return;
        }

        checkIsVip(new RunnableArgs<Boolean>() {
            @Override
            public void run() {
                if(!this.getFirstArg()){
                    if(getCurrentNextAdsCount() <= 0){
                        canRun.run(false);
                        new NoCreditsDialog(getActivity(), myModel, new INoCreditsListener() {
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

        FragmentManager fm = getActivity().getSupportFragmentManager();
        adsFrag = (AdsFrag) fm.findFragmentByTag("adsFrag");
        //not in search
        if (adsFrag == null) {
            adsFrag = AdsFrag.newInstance();
            fm.beginTransaction().add(adsFrag, "adsFrag").commit();
        }
    }

    private void refreshNextAdsCount(final int newAdsCount, final boolean animate){
        Threadings.postRunnable(getActivity(), new Runnable() {
            @Override
            public void run() {
                if(getActivity() == null) return;

                Logs.show("refresh next ads count: " + newAdsCount);

                String finalString = "";

                if(!completeProcessing){
                    finalString = "?";
                }
                else{
                    finalString = String.valueOf(Math.max(newAdsCount, 0));
                }

                if (animate){
                    final String finalString1 = finalString;
                    AnimateBuilder.build(getActivity(), txtNextAdsCount)
                            .setDurationMs(200).setValue(0).setAnimateType(AnimateType.alpha)
                            .setFinishCallback(new Runnable() {
                                @Override
                                public void run() {
                                    txtNextAdsCount.setText(finalString1);
                                    AnimateBuilder.fadeIn(getActivity(), txtNextAdsCount);

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


        final AlertDialog dialog = OverlayBuilder.build(getActivity())
                .setOverlayType(OverlayType.Loading)
                .setContent(getString(R.string.loading))
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

    private void changeNextAdsCount(int newCount, boolean animate){
        setCurrentNextAdsCount(newCount);
        refreshNextAdsCount(newCount, animate);
    }

    public Integer getCurrentNextAdsCount() {
        if(currentNextAdsCount == null){
            String value = PreferenceUtils.get(getContext(), PreferenceType.NextAdsCount);
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
        PreferenceUtils.put(getContext(), PreferenceType.NextAdsCount, value.toString());
        currentNextAdsCount = value;
    }

    private void setListeners(){
        layoutControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ActivityVip.class);
                intent.putExtra("userId", myModel.getUserId());
                startActivity(intent);
            }
        });
    }

}
