package com.ffinder.android;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import com.ffinder.android.absint.activities.MyActivityAbstract;
import com.ffinder.android.absint.helpers.RestfulListener;
import com.ffinder.android.enums.ActionBarActionType;
import com.ffinder.android.enums.AnalyticEvent;
import com.ffinder.android.enums.OverlayType;
import com.ffinder.android.enums.Status;
import com.ffinder.android.extensions.TextFieldWrapper;
import com.ffinder.android.helpers.*;

public class ActivityPromo extends MyActivityAbstract {

    private String myUserId;
    private TextFieldWrapper promoCodeWrapper;
    private Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promo);

        enableCustomActionBar();
        setActionBarTitle(R.string.promo_code_activity_title);
        addActionToActionBar(ActionBarActionType.Back, false, true);
        addActionToActionBar(ActionBarActionType.OK, false, false);

        Bundle bundle = this.getIntent().getExtras();
        myUserId = bundle.getString("userId");

        promoCodeWrapper = (TextFieldWrapper) findViewById(R.id.newNameWrapper);
        btnSubmit = (Button) findViewById(R.id.btnShareKey);
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

        if (promoCodeWrapper.validateNotEmpty(getString(R.string.empty_promo_code_error_msg))){

            final AlertDialog progressDialog = OverlayBuilder.build(this)
                    .setOverlayType(OverlayType.Loading)
                    .setContent(getString(R.string.checking_promo_code_msg))
                    .show();

            RestfulService.usePromoCode(myUserId, promoCodeWrapper.getText(), new RestfulListener<String>() {
                @Override
                public void onResult(final String result, final Status status) {
                    Threadings.postRunnable(new Runnable(){
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            if(status == Status.Success){
                                Integer resultInt = Integer.valueOf(result);
                                if(resultInt > 0){

                                    OverlayBuilder.build(ActivityPromo.this)
                                            .setOverlayType(OverlayType.OkOnly)
                                            .setContent(String.format(getString(R.string.promo_code_success_msg), result))
                                            .setOnDismissRunnable(new Runnable() {
                                                @Override
                                                public void run() {
                                                    finish();
                                                }
                                            }).show();


                                    Analytics.logEvent(AnalyticEvent.Insert_PromoCode_Success);
                                    return;
                                }
                            }

                            promoCodeWrapper.setError(getString(R.string.promo_code_failed_error_msg));
                            Analytics.logEvent(AnalyticEvent.Insert_PromoCode_Failed);
                        }
                    });
                }
            });

        }
    }

    private void setListeners(){
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndSubmit();
            }
        });
    }


}
