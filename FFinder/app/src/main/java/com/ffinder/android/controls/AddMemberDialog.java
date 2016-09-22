package com.ffinder.android.controls;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import com.ffinder.android.R;
import com.ffinder.android.absint.databases.FirebaseListener;
import com.ffinder.android.enums.AnalyticEvent;
import com.ffinder.android.enums.Status;
import com.ffinder.android.helpers.Analytics;
import com.ffinder.android.helpers.FirebaseDB;
import com.ffinder.android.models.FriendModel;
import com.ffinder.android.models.KeyModel;
import com.ffinder.android.models.MyModel;
import com.ffinder.android.statics.Vars;
import com.ffinder.android.utils.AndroidUtils;
import com.ffinder.android.utils.Strings;

/**
 * Created by SiongLeng on 1/9/2016.
 */
public class AddMemberDialog {

    private boolean processCanceled;
    private Activity activity;
    private TextView txtError;
    private EditText editTxtYourName, editTxtKey, editTxtMemberName;
    private TextInputLayout userKeyWrapper, memberNameWrapper, yourNameWrapper;
    private AlertDialog dialog;
    private ProgressDialog pd;
    private MyModel myModel;
    private boolean autoAdd;

    public AddMemberDialog(Activity activity, MyModel myModel) {
        this.activity = activity;
        this.myModel = myModel;
    }

    public void show(){
        String pendingAddUserKey = Vars.pendingAddUserKey;
        if(!Strings.isEmpty(pendingAddUserKey)){
            autoAdd = true;
        }
        Vars.clearPendingAddUser();

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        View viewInflated = LayoutInflater.from(activity).inflate(R.layout.dialog_add_friend,
                (ViewGroup) activity.getWindow().getDecorView().findViewById(android.R.id.content), false);

        txtError = (TextView) viewInflated.findViewById(R.id.txtError);

        userKeyWrapper = (TextInputLayout) viewInflated.findViewById(R.id.keyWrapper);
        memberNameWrapper = (TextInputLayout) viewInflated.findViewById(R.id.memberNameWrapper);
        yourNameWrapper = (TextInputLayout) viewInflated.findViewById(R.id.yourNameWrapper);

        editTxtKey = (EditText) viewInflated.findViewById(R.id.editTxtKey);
        editTxtKey.setText(pendingAddUserKey != null ? pendingAddUserKey : "");
        editTxtMemberName = (EditText) viewInflated.findViewById(R.id.editTxtMemberName);
        editTxtYourName = (EditText) viewInflated.findViewById(R.id.editTxtYourName);
        editTxtYourName.setText(AndroidUtils.getUsername(activity));

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

        if(autoAdd){
            checkCanAdd();
        }

        Analytics.logEvent(AnalyticEvent.Open_Add_Friend_Dialog);
    }

    private void validate(){
        processCanceled = false;
        txtError.setVisibility(View.GONE);

        if(AndroidUtils.validateEditText(editTxtKey, userKeyWrapper, activity.getString(R.string.no_user_key_msg))
                && AndroidUtils.validateEditText(editTxtMemberName, memberNameWrapper, activity.getString(R.string.no_member_name_msg))
                && AndroidUtils.validateEditText(editTxtYourName, yourNameWrapper, activity.getString(R.string.no_your_name_msg))){
            checkCanAdd();
        }

    }

    private void checkCanAdd(){
        pd = AndroidUtils.loading(activity.getString(R.string.adding_friend_msg), activity, new Runnable() {
            @Override
            public void run() {
                processCanceled = true;
            }
        });

        final String targetKey = editTxtKey.getText().toString();
        final String myName = editTxtYourName.getText().toString();

        FirebaseDB.checkKeyExist(myModel.getUserId(), targetKey, new FirebaseListener<KeyModel>(KeyModel.class) {
            @Override
            public void onResult(final KeyModel keyModel, Status status) {
                if(processCanceled) return;

                if(status == Status.Success && keyModel != null){
                    if(Strings.isEmpty(editTxtMemberName.getText().toString())){
                        editTxtMemberName.setText(keyModel.getUserName());
                    }

                    if(!myModel.checkFriendExist(keyModel.getUserId())){
                        FirebaseDB.addNewLink(myModel.getUserId(), keyModel.getUserId(), myName,
                                        editTxtMemberName.getText().toString(), new FirebaseListener() {
                            @Override
                            public void onResult(Object result, Status status) {
                                if(status == Status.Success){
                                    successAddUser(keyModel.getUserId(), keyModel.getUserName());
                                }
                                else{
                                    errorOccurred(AddMemberError.UnknownError);
                                }
                            }
                        });
                    }
                    else{
                        errorOccurred(AddMemberError.UserAlreadyAdded, myModel.getFriendModelById(keyModel.getUserId()).getName());
                    }
                }
                else{
                    errorOccurred(AddMemberError.KeyNotExistOrExpired, targetKey);
                }
            }
        });
    }

    private void errorOccurred(AddMemberError addMemberError, String... extra){
        String errorMsg = "";
        String msg = "";
        switch (addMemberError){
            case UserAlreadyAdded:
                msg = String.format(activity.getString(R.string.user_already_added_error_msg), extra[0]);
                errorMsg = msg;
                break;
            case KeyNotExistOrExpired:
                msg = activity.getString(R.string.key_expired_or_not_exist_msg);
                errorMsg = msg + "--" + extra[0];
                break;
            case UnknownError:
                msg = activity.getString(R.string.unknown_error_msg);
                errorMsg = msg;
                break;
        }

        txtError.setText(msg);
        txtError.setVisibility(View.VISIBLE);
        pd.dismiss();

        Analytics.logEvent(AnalyticEvent.Add_Friend_Failed, errorMsg);
    }

    private void successAddUser(String addingUserId, String name){
        FriendModel newFriendModel = new FriendModel();
        newFriendModel.setUserId(addingUserId);
        newFriendModel.setName(Strings.pickNonEmpty(editTxtMemberName.getText().toString(), name, "No_Name"));

        myModel.addFriendModel(newFriendModel, true);
        myModel.sortFriendModels();
        newFriendModel.save(activity);

        pd.dismiss();
        dialog.dismiss();

        Analytics.logEvent(AnalyticEvent.Add_Friend_Success);
    }



    private enum AddMemberError{
        KeyNotExistOrExpired, UserAlreadyAdded, UnknownError
    }

}
