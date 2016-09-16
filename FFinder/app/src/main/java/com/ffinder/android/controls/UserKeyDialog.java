package com.ffinder.android.controls;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.ffinder.android.R;
import com.ffinder.android.enums.AnalyticEvent;
import com.ffinder.android.helpers.Analytics;
import com.ffinder.android.models.MyModel;
import com.ffinder.android.utils.AndroidUtils;
import com.ffinder.android.utils.DateTimeUtils;
import com.ffinder.android.utils.RunnableArgs;

/**
 * Created by SiongLeng on 6/9/2016.
 */
public class UserKeyDialog {

    private Activity activity;
    private MyModel myModel;
    private TextView txtYourKey, txtExpiredDateTime;
    private Button btnShareKey;
    private ImageButton btnRegenKey;
    private AlertDialog dialog;

    public UserKeyDialog(Activity activity, MyModel myModel) {
        this.activity = activity;
        this.myModel = myModel;
    }

    public void show(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        View viewInflated = LayoutInflater.from(activity).inflate(R.layout.dialog_user_key,
                (ViewGroup) activity.getWindow().getDecorView().findViewById(android.R.id.content), false);

        txtYourKey = (TextView) viewInflated.findViewById(R.id.txtYourKey);
        txtExpiredDateTime = (TextView) viewInflated.findViewById(R.id.txtExpiredDateTime);
        btnShareKey = (Button) viewInflated.findViewById(R.id.btnShareKey);
        btnRegenKey = (ImageButton) viewInflated.findViewById(R.id.btnRegenKey);

        builder.setView(viewInflated);

        // Set up the buttons
        builder.setPositiveButton(activity.getString(R.string.close), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
            }
        });

        resetKey();
        setListeners();
    }

    public void resetKey(){
        if(myModel.checkUserKeyValid()){
            String key = myModel.getUserKey();
            txtYourKey.setText(key);
            txtExpiredDateTime.setText(String.valueOf(DateTimeUtils.convertUnixMiliSecsToDateTimeString(activity,
                                    myModel.getUserKeyExpiredUnixTime() * 1000) +
                                String.format(activity.getString(R.string.minutes_left), getKeyRemainingMinutes())));
        }
        else{
            final ProgressDialog progressDialog = AndroidUtils.loading(activity.getString(R.string.regenerating_key_msg), activity, new Runnable() {
                @Override
                public void run() {
                    myModel.cancelRegenerateUserKey();
                }
            });
            myModel.regenerateUserKey(0, new RunnableArgs<String>() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                    resetKey();
                }
            });
        }
    }

    private long getKeyRemainingMinutes(){
        return (myModel.getUserKeyExpiredUnixTime() - (System.currentTimeMillis() / 1000L)) / 60;
    }

    public void setListeners(){
        btnShareKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Analytics.logEvent(AnalyticEvent.Share_Key);

                String key = txtYourKey.getText().toString();

                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = String.format(activity.getString(R.string.share_msg), key, key);
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, activity.getString(R.string.share_subject));
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                activity.startActivity(Intent.createChooser(sharingIntent, activity.getString(R.string.share_title)));
            }
        });

        btnRegenKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               new AlertDialog.Builder(activity)
                        .setMessage(activity.getString(R.string.regen_key_confirm_msg))
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                myModel.setUserKeyGeneratedUnixTime(0);
                                resetKey();
                            }})
                        .setNegativeButton(R.string.no, null).show();
            }
        });

        txtYourKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
                    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setText(txtYourKey.getText().toString());
                } else {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("",txtYourKey.getText().toString());
                    clipboard.setPrimaryClip(clip);
                }
                Toast.makeText(activity, R.string.copied_to_clipboard_toast_msg, Toast.LENGTH_SHORT).show();
            }
        });

    }
}
