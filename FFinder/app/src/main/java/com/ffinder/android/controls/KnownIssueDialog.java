package com.ffinder.android.controls;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.ffinder.android.R;
import com.ffinder.android.absint.activities.MyActivityAbstract;
import com.ffinder.android.enums.PhoneBrand;
import com.ffinder.android.enums.PreferenceType;
import com.ffinder.android.helpers.PreferenceUtils;

/**
 * Created by SiongLeng on 19/9/2016.
 */
public class KnownIssueDialog {

    private MyActivityAbstract activity;
    private AlertDialog dialog;
    private RelativeLayout layoutFragment;
    private CheckBox checkboxDontRemindMeAgain;
    private Button btnOk;
    private PhoneBrand phoneBrand;

    public KnownIssueDialog(MyActivityAbstract activity, PhoneBrand phoneBrand) {
        this.activity = activity;
        this.phoneBrand = phoneBrand;
    }

    public void show(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        View viewInflated = LayoutInflater.from(activity).inflate(R.layout.dialog_known_issue,
                (ViewGroup) activity.getWindow().getDecorView().findViewById(android.R.id.content), false);

        layoutFragment = (RelativeLayout) viewInflated.findViewById(R.id.layoutFragment);
        checkboxDontRemindMeAgain = (CheckBox) viewInflated.findViewById(R.id.checkBoxDontRemind);
        btnOk = (Button) viewInflated.findViewById(R.id.btnOk);


        View contentView = LayoutInflater.from(activity).inflate(R.layout.layout_known_issue, null);
        if(phoneBrand == PhoneBrand.Xiaomi){
            ((TextView) contentView.findViewById(R.id.txtMessage)).setText(activity.getString(R.string.issue_fix_xiaomi));
        }
        else if(phoneBrand == PhoneBrand.Huawei){
            ((TextView) contentView.findViewById(R.id.txtMessage)).setText(activity.getString(R.string.issue_fix_huawei));
        }
        else if(phoneBrand == PhoneBrand.Sony){
            ((TextView) contentView.findViewById(R.id.txtMessage)).setText(activity.getString(R.string.issue_fix_sony));
        }
        layoutFragment.addView(contentView);

        builder.setView(viewInflated);
        dialog = builder.create();
        dialog.show();

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(checkboxDontRemindMeAgain.isChecked()){
                    PreferenceUtils.put(activity, PreferenceType.DontRemindMeAgainPhoneIssue, "1");
                }
            }
        });

        setListeners();
    }


    public void setListeners(){
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }


}
