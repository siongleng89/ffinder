package com.ffinder.android;

import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.ffinder.android.absint.activities.MyActivityAbstract;
import com.ffinder.android.absint.databases.FirebaseListener;
import com.ffinder.android.enums.*;
import com.ffinder.android.extensions.TextFieldWrapper;
import com.ffinder.android.helpers.*;
import com.ffinder.android.models.FriendModel;
import com.ffinder.android.models.KeyModel;
import com.ffinder.android.statics.Vars;

public class ActivityAddFriend extends MyActivityAbstract {

    private boolean processCanceled;

    private TextView txtError;
    private TextFieldWrapper userKeyWrapper, memberNameWrapper, yourNameWrapper;
    private AlertDialog loadingDialog;
    private Button btnAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        enableCustomActionBar();
        setActionBarTitle(R.string.add);
        addActionToActionBar(ActionBarActionType.Back, false, true);
        addActionToActionBar(ActionBarActionType.OK, false, false);

        txtError = (TextView) findViewById(R.id.txtError);

        userKeyWrapper = (TextFieldWrapper) findViewById(R.id.newNameWrapper);
        memberNameWrapper = (TextFieldWrapper) findViewById(R.id.memberNameWrapper);
        yourNameWrapper = (TextFieldWrapper) findViewById(R.id.yourNameWrapper);
        btnAdd = (Button) findViewById(R.id.btnAdd);

        userKeyWrapper.setNumericOnly();

        yourNameWrapper.setText(AndroidUtils.getUsername(ActivityAddFriend.this));


        //check got pending auto add user key (eg. click redirect from whatsapp)
        String pendingAddUserKey = Vars.pendingAddUserKey;
        if(!Strings.isEmpty(pendingAddUserKey)){
            userKeyWrapper.setText(pendingAddUserKey);
            validateAndSubmit();
        }
        Vars.clearPendingAddUser();

        setListeners();
    }

    @Override
    public void onActionButtonClicked(ActionBarActionType actionBarActionType) {
        super.onActionButtonClicked(actionBarActionType);

        if (actionBarActionType == ActionBarActionType.OK){
            validateAndSubmit();
        }
    }

    private void validateAndSubmit(){
        AnimateBuilder.fadeOutAndSetGone(this, txtError);
        processCanceled = false;

        //use & not &&, to force evaluate all conditions
        if (yourNameWrapper.validateNotEmpty(getString(R.string.no_your_name_msg)) &
                userKeyWrapper.validateNotEmpty(getString(R.string.no_user_key_msg))){

            loadingDialog = OverlayBuilder.build(this)
                                .setOverlayType(OverlayType.Loading)
                                .setContent(getString(R.string.adding_friend_msg))
                                .setOnDismissRunnable(new Runnable() {
                                    @Override
                                    public void run() {
                                        processCanceled = true;
                                    }
                                }).show();

            FirebaseDB.checkKeyExist(getMyModel().getUserId(), userKeyWrapper.getText(),
                    new FirebaseListener<KeyModel>(KeyModel.class) {
                @Override
                public void onResult(final KeyModel keyModel, Status status) {
                    if(processCanceled) return;

                    String targetKey = userKeyWrapper.getText();
                    final String myName = yourNameWrapper.getText();
                    String targetName = memberNameWrapper.getText();

                    if(status == Status.Success && keyModel != null){
                        if(Strings.isEmpty(targetName)){
                            targetName = keyModel.getUserName();
                            memberNameWrapper.setText(targetName);
                        }

                        if(!getMyModel().checkFriendExist(keyModel.getUserId())){
                            final String finalTargetName = targetName;
                            FirebaseDB.addNewLink(getMyModel().getUserId(), keyModel.getUserId(), myName,
                                    targetName, new FirebaseListener() {
                                        @Override
                                        public void onResult(Object result, Status status) {
                                            if(status == Status.Success){
                                                successAddUser(keyModel.getUserId(),
                                                        finalTargetName, myName);
                                            }
                                            else{
                                                errorOccurred(AddMemberError.UnknownError);
                                            }
                                        }
                                    });
                        }
                        else{
                            errorOccurred(AddMemberError.UserAlreadyAdded,
                                    getMyModel().getFriendModelById(keyModel.getUserId()).getName());
                        }
                    }
                    else{
                        errorOccurred(AddMemberError.KeyNotExistOrExpired, targetKey);
                    }
                }
            });
        }

    }

    private void errorOccurred(AddMemberError addMemberError, String... extra){
        String errorMsg = "";
        String msg = "";
        switch (addMemberError){
            case UserAlreadyAdded:
                msg = String.format(getString(R.string.user_already_added_error_msg), extra[0]);
                errorMsg = msg;
                break;
            case KeyNotExistOrExpired:
                msg = getString(R.string.key_expired_or_not_exist_msg);
                errorMsg = msg + "--" + extra[0];
                break;
            case UnknownError:
                msg = getString(R.string.unknown_error_msg);
                errorMsg = msg;
                break;
        }

        txtError.setText(msg);
        AnimateBuilder.fadeIn(this, txtError);
        loadingDialog.dismiss();

        Analytics.logEvent(AnalyticEvent.Add_Friend_Failed, errorMsg);
    }

    private void successAddUser(String addingUserId, String targetName, String myName){
        FriendModel newFriendModel = new FriendModel();
        newFriendModel.setUserId(addingUserId);
        newFriendModel.setName(Strings.pickNonEmpty(targetName, ""));

        getMyModel().addFriendModel(newFriendModel);
        getMyModel().sortFriendModels();
        newFriendModel.save(this);
        getMyModel().commitFriendUserIds();

        BroadcasterHelper.broadcast(this, BroadcastEvent.RefreshFriend,
                new Pair<String, String>("userId", addingUserId));
        loadingDialog.dismiss();

        //notificate user added friend
        NotificationSender.sendWithUserId(getMyModel().getUserId(), addingUserId,
                FCMMessageType.FriendsAdded, NotificationSender.TTL_LONG, null,
                new Pair<String, String>("username", myName));


        finish();
        Analytics.logEvent(AnalyticEvent.Add_Friend_Success);
    }


    private void setListeners(){
        userKeyWrapper.getEditTxtField().addTextChangedListener(new TextWatcher() {
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

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndSubmit();
            }
        });

    }

    private enum AddMemberError{
        KeyNotExistOrExpired, UserAlreadyAdded, UnknownError
    }



}