package com.ffinder.android.absint.activities;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;
import com.ffinder.android.MyApplication;
import com.ffinder.android.R;
import com.ffinder.android.enums.ActionBarActionType;
import com.ffinder.android.enums.BroadcastEvent;
import com.ffinder.android.helpers.Analytics;
import com.ffinder.android.helpers.AnimateBuilder;
import com.ffinder.android.helpers.BroadcasterHelper;
import com.ffinder.android.models.MyModel;
import com.ffinder.android.helpers.AndroidUtils;
import com.ffinder.android.helpers.RunnableArgs;

import java.util.HashMap;


/**
 * Created by SiongLeng on 16/9/2016.
 */
public class MyActivityAbstract extends AppCompatActivity {

    protected boolean paused;
    private LinearLayout menuOverflowLayout;
    private HashMap<BroadcastEvent, BroadcastReceiver> broadcastReceiverHashMap;
    private MyModel myModel;


    public MyActivityAbstract() {
        broadcastReceiverHashMap = new HashMap();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Analytics.logToScreen(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        paused = false;
    }

    public boolean isPaused() {
        return paused;
    }



    @Override
    protected void onStart() {
        super.onStart();

        for(BroadcastEvent broadcastEvent : broadcastReceiverHashMap.keySet()){
            BroadcasterHelper.register(this, broadcastEvent, broadcastReceiverHashMap.get(broadcastEvent));
        }

    }

    @Override
    protected void onStop() {
        super.onStop();

        for(BroadcastReceiver broadcastReceiver : broadcastReceiverHashMap.values()){
            BroadcasterHelper.unregister(this, broadcastReceiver);
        }
    }

    protected void enableCustomActionBar(){
        final Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setOverflowIcon(null);

        setSupportActionBar(myToolbar);

        View view = getLayoutInflater().inflate(R.layout.top_action_bar, null);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(view);


        Toolbar parent =(Toolbar) view.getParent();
        parent.setPadding(0,0,0,0);//for tab otherwise give space in tab
        parent.setContentInsetsAbsolute(0,0);
    }

    protected void setActionBarTitle(int titleId){
        TextView txtTitle = (TextView) getSupportActionBar().getCustomView().findViewById(R.id.txtTitle);
        txtTitle.setText(getString(titleId));
    }

    protected void addActionToActionBar(ActionBarActionType actionBarActionType, boolean bigIcon,
                                            boolean isLeftSide){

        int drawable = -1;
        switch (actionBarActionType){

            case AppIcon:
                drawable = R.drawable.app_icon;
                break;

            case ShareKey:
                drawable = R.drawable.add_icon;
                break;

            case Overflow:
                drawable = R.drawable.overflow_icon;
                break;

            case Back:
                drawable = R.drawable.back_icon;
                break;

            case OK:
                drawable = R.drawable.tick_icon;
                break;

            case Share:
                drawable = R.drawable.share_icon;
                break;

            case Reload:
                drawable = R.drawable.reload_icon;
                break;

        }

        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, tv, true))
        {
            float actionBarHeightPx = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());

            int iconMarginInDp = bigIcon ? 3 : 7;
            int btnPaddingInDp = bigIcon ? 5 : 10;
            int btnPaddingInPixel = AndroidUtils.dpToPx(this, btnPaddingInDp);
            float btnSize = actionBarHeightPx - AndroidUtils.dpToPx(this, iconMarginInDp) * 2;

            RelativeLayout view = (RelativeLayout) getSupportActionBar().getCustomView();

            LinearLayout leftLinearLayout = (LinearLayout) view.findViewById(R.id.leftLinearLayout);
            LinearLayout rightLinearLayout = (LinearLayout) view.findViewById(R.id.rightLinearLayout);


            ImageButton btn = new ImageButton(this);
            AndroidUtils.setButtonBackground(this, btn, R.drawable.nav_btn);

            Drawable iconImage = ContextCompat.getDrawable(this, drawable);
            btn.setImageDrawable(iconImage);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((int) btnSize, (int) btnSize);
            lp.setMargins(isLeftSide ? 0 : AndroidUtils.dpToPx(this, 10), 0, 0, 0);

            btn.setLayoutParams(lp);
            btn.setScaleType(ImageView.ScaleType.FIT_CENTER);
            btn.setPadding(btnPaddingInPixel, btnPaddingInPixel, btnPaddingInPixel, btnPaddingInPixel);

            if(isLeftSide){
                leftLinearLayout.addView(btn);
            }
            else{
                rightLinearLayout.addView(btn);
            }
            setActionButtonListener(btn, actionBarActionType);
        }
    }

    protected void addActionToOverflow(String title){
        if (menuOverflowLayout == null){
            Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
            RelativeLayout relativeLayout = (RelativeLayout) toolbar.getParent();

            menuOverflowLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.menu_overflow, null);

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
            lp.addRule(RelativeLayout.BELOW, R.id.my_toolbar);
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            lp.setMargins(0, 0, AndroidUtils.dpToPx(this, 3), 0);

            menuOverflowLayout.setLayoutParams(lp);
            menuOverflowLayout.setVisibility(View.GONE);

            relativeLayout.addView(menuOverflowLayout);

            relativeLayout.setFocusableInTouchMode(true);
            relativeLayout.setFocusable(true);
            relativeLayout.setClickable(true);
        }

        int padding = AndroidUtils.dpToPx(this, 10);

        TextView textView = new TextView(this);
        textView.setTextColor(Color.WHITE);
        textView.setText(title);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        textView.setPadding(padding, padding, padding, padding);
        textView.setTypeface(null, Typeface.BOLD);

        LinearLayoutCompat.LayoutParams param = new LinearLayoutCompat.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);


        menuOverflowLayout.addView(textView);

        setActionOverflowButtonListener(textView, title);
    }


    private void setActionButtonListener(final ImageButton imageButton, final ActionBarActionType actionBarActionType){
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onActionButtonClicked(actionBarActionType);

                if(actionBarActionType == ActionBarActionType.Overflow && menuOverflowLayout != null){
                    AnimateBuilder.fadeIn(MyActivityAbstract.this, menuOverflowLayout);
                }
                else if(actionBarActionType == ActionBarActionType.Back){
                    finish();
                }

            }
        });

        imageButton.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                    AndroidUtils.setButtonBackground(MyActivityAbstract.this,
                            imageButton, R.drawable.nav_btn_ontap);
                }
                else if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                    AndroidUtils.setButtonBackground(MyActivityAbstract.this,
                            imageButton, R.drawable.nav_btn);
                }
                return false;
            }
        });

    }

    private void setActionOverflowButtonListener(final TextView textView,
                                                 final String title){
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOverflowActionClicked(title);
            }
        });
    }


    public void onActionButtonClicked(ActionBarActionType actionBarActionType){

    }

    public void onOverflowActionClicked(String title){

    }


    protected void registerBroadcastReceiver(BroadcastEvent broadcastEvent, RunnableArgs<Intent> onResult){
        if(broadcastReceiverHashMap.containsKey(broadcastEvent)){   //prevent duplicate registrations
            return;
        }
        else{
            BroadcastReceiver broadcastReceiver = BroadcasterHelper.register(this, broadcastEvent, onResult);
            broadcastReceiverHashMap.put(broadcastEvent, broadcastReceiver);
        }
    }

    protected void removeBroadcastReceiver(BroadcastEvent broadcastEvent){
        if(broadcastReceiverHashMap.containsKey(broadcastEvent)){
            BroadcasterHelper.unregister(this, broadcastReceiverHashMap.get(broadcastEvent));
            broadcastReceiverHashMap.remove(broadcastEvent);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
                if(menuOverflowLayout != null) menuOverflowLayout.setVisibility(View.GONE);
                break;
        }
        return super.dispatchTouchEvent(ev);
    }


    protected MyModel getMyModel(){
        if (myModel == null){
            myModel = ((MyApplication) getApplication()).getMyModel();
        }
        return myModel;
    }









}
