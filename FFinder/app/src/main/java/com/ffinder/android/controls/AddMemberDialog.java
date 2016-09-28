package com.ffinder.android.controls;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.ffinder.android.R;
import com.ffinder.android.absint.databases.FirebaseListener;
import com.ffinder.android.enums.AnalyticEvent;
import com.ffinder.android.enums.FCMMessageType;
import com.ffinder.android.enums.Status;
import com.ffinder.android.helpers.Analytics;
import com.ffinder.android.helpers.FirebaseDB;
import com.ffinder.android.helpers.NotificationSender;
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
    private RelativeLayout layoutTutorials;
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
        layoutTutorials = (RelativeLayout) viewInflated.findViewById(R.id.layoutTutorials);

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

        setListeners();

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
                                    successAddUser(keyModel.getUserId(), keyModel.getUserName(), myName);
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
                layoutTutorials.setVisibility(View.VISIBLE);
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

    private void successAddUser(String addingUserId, String name, String myName){
        FriendModel newFriendModel = new FriendModel();
        newFriendModel.setUserId(addingUserId);
        newFriendModel.setName(Strings.pickNonEmpty(editTxtMemberName.getText().toString(), name, "No_Name"));

        myModel.addFriendModel(newFriendModel, true);
        myModel.sortFriendModels();
        newFriendModel.save(activity);

        pd.dismiss();
        dialog.dismiss();

        NotificationSender.send(myModel.getUserId(), addingUserId, FCMMessageType.FriendsAdded, NotificationSender.TTL_LONG,
                new Pair<String, String>("username", myName));

        Analytics.logEvent(AnalyticEvent.Add_Friend_Success);
    }


    private void setListeners(){
        editTxtKey.addTextChangedListener(new TextWatcher() {
            boolean isFormatting;
            boolean deletingHyphen;
            int hyphenStart;
            boolean deletingBackward;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (isFormatting)
                    return;

                // Make sure user is deleting one char, without a selection
                final int selStart = Selection.getSelectionStart(s);
                final int selEnd = Selection.getSelectionEnd(s);
                if (s.length() > 1 // Can delete another character
                        && count == 1 // Deleting only one character
                        && after == 0 // Deleting
                        && s.charAt(start) == '-' // a hyphen
                        && selStart == selEnd) { // no selection
                    deletingHyphen = true;
                    hyphenStart = start;
                    // Check if the user is deleting forward or backward
                    if (selStart == start + 1) {
                        deletingBackward = true;
                    } else {
                        deletingBackward = false;
                    }
                } else {
                    deletingHyphen = false;
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (isFormatting)
                    return;

                isFormatting = true;

                // If deleting hyphen, also delete character before or after it
                if (deletingHyphen && hyphenStart > 0) {
                    if (deletingBackward) {
                        if (hyphenStart - 1 < editable.length()) {
                            editable.delete(hyphenStart - 1, hyphenStart);
                        }
                    } else if (hyphenStart < editable.length()) {
                        editable.delete(hyphenStart, hyphenStart + 1);
                    }
                }
                if (editable.length() == 4 || editable.length() == 9) {
                    editable.append('-');
                }

                isFormatting = false;
            }
        });
    }

    private enum AddMemberError{
        KeyNotExistOrExpired, UserAlreadyAdded, UnknownError
    }

}
