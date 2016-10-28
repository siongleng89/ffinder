package com.ffinder.android;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.ffinder.android.absint.activities.MyActivityAbstract;
import com.ffinder.android.enums.ActionBarActionType;
import com.ffinder.android.enums.AnalyticEvent;
import com.ffinder.android.enums.OverlayType;
import com.ffinder.android.helpers.*;

public class ActivityShareKey extends MyActivityAbstract {

    private TextView txtYourKey, txtExpiredDateTime;
    private Button btnShareKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_key);

        enableCustomActionBar();
        setActionBarTitle(R.string.share_key_activity_title);
        addActionToActionBar(ActionBarActionType.Back, false, true);
        addActionToActionBar(ActionBarActionType.Reload, false, false);
        addActionToActionBar(ActionBarActionType.Share, false, false);

        txtYourKey = (TextView) findViewById(R.id.txtYourKey);
        txtExpiredDateTime = (TextView) findViewById(R.id.txtExpiredDateTime);
        btnShareKey = (Button) findViewById(R.id.btnShareKey);

        resetKey();
        setListeners();
    }

    @Override
    public void onActionButtonClicked(ActionBarActionType actionBarActionType) {
        super.onActionButtonClicked(actionBarActionType);

        if(actionBarActionType == ActionBarActionType.Reload){
            regenKeyPressed();
        }
        else if(actionBarActionType == ActionBarActionType.Share){
            shareKey();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Analytics.logEvent(AnalyticEvent.Close_Share_Key_Dialog);
    }


    public void resetKey(){
        if(getMyModel().checkUserKeyValid()){
            String key = getMyModel().getUserKey();
            txtYourKey.setText(key);

            txtExpiredDateTime.setText(String.format(getString(R.string.expired_at_title),
                    String.valueOf(DateTimeUtils.
                    convertUnixMiliSecsToUserPrefDateTimeString(this,
                    getMyModel().getUserKeyExpiredUnixTime() * 1000))));
        }
        else{
            final AlertDialog progressDialog = OverlayBuilder.build(this)
                    .setOverlayType(OverlayType.Loading)
                    .setContent(getString(R.string.regenerating_key_msg))
                    .setOnDismissRunnable(new Runnable() {
                        @Override
                        public void run() {
                            getMyModel().cancelRegenerateUserKey();
                        }
                    }).show();

            getMyModel().regenerateUserKey(0, new RunnableArgs<String>() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                    resetKey();
                }
            });
        }
    }

    public void regenKeyPressed(){
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.regen_key_confirm_msg))
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        getMyModel().setUserKeyGeneratedUnixTime(0);
                        resetKey();
                    }})
                .setNegativeButton(R.string.no, null).show();
    }

    private void shareKey(){
        Analytics.logEvent(AnalyticEvent.Share_Key_Button_Clicked);

        String key = txtYourKey.getText().toString();

        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = String.format(getString(R.string.share_msg), key, key);
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_title)));
    }

    public void setListeners(){
        btnShareKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareKey();
            }
        });


        txtYourKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
                    android.text.ClipboardManager clipboard = (android.text.ClipboardManager)
                                                            getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setText(txtYourKey.getText().toString());
                } else {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
                                                    getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("",
                                                            txtYourKey.getText().toString());
                    clipboard.setPrimaryClip(clip);
                }
                Toast.makeText(ActivityShareKey.this,
                        R.string.copied_to_clipboard_toast_msg, Toast.LENGTH_SHORT).show();
            }
        });

    }








}
