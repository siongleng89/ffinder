package com.ffinder.android.controls;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import com.ffinder.android.R;
import com.ffinder.android.helpers.FirebaseDB;
import com.ffinder.android.models.FriendModel;
import com.ffinder.android.models.MyModel;
import com.ffinder.android.utils.AndroidUtils;

/**
 * Created by SiongLeng on 1/9/2016.
 */
public class EditNameDialog {

    private Activity activity;
    private FriendModel friendModel;
    private MyModel myModel;
    private EditText editTxtNewName;
    private TextInputLayout newNameWrapper;
    private AlertDialog dialog;

    public EditNameDialog(Activity activity, FriendModel friendModel, MyModel myModel) {
        this.activity = activity;
        this.friendModel = friendModel;
        this.myModel = myModel;
    }

    public void show(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        View viewInflated = LayoutInflater.from(activity).inflate(R.layout.dialog_edit_name,
                (ViewGroup) activity.getWindow().getDecorView().findViewById(android.R.id.content), false);

        newNameWrapper = (TextInputLayout) viewInflated.findViewById(R.id.newNameWrapper);
        editTxtNewName = (EditText) viewInflated.findViewById(R.id.editTxtNewName);
        editTxtNewName.setText(friendModel.getName());
        builder.setView(viewInflated);

        // Set up the buttons
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                validate();
            }
        });
    }

    public void validate(){
        newNameWrapper.setErrorEnabled(false);

        if(AndroidUtils.validateEditText(editTxtNewName, newNameWrapper, activity.getString(R.string.edit_name_empty_error_msg))){
            boolean changed = !friendModel.getName().equals(editTxtNewName.getText().toString());
            if(changed){
                friendModel.setName(editTxtNewName.getText().toString());
                friendModel.save(activity);
                FirebaseDB.editLinkName(myModel.getUserId(), friendModel.getUserId(), editTxtNewName.getText().toString(), null);
            }

            dialog.dismiss();
        }
    }



}
