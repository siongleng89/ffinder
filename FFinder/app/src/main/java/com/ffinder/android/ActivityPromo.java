package com.ffinder.android;

import android.app.ProgressDialog;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.ffinder.android.absint.activities.MyActivityAbstract;
import com.ffinder.android.absint.helpers.RestfulListener;
import com.ffinder.android.enums.AnalyticEvent;
import com.ffinder.android.enums.Status;
import com.ffinder.android.helpers.Analytics;
import com.ffinder.android.helpers.FirebaseDB;
import com.ffinder.android.helpers.RestfulService;
import com.ffinder.android.utils.AndroidUtils;
import com.ffinder.android.utils.Threadings;
import com.google.firebase.database.FirebaseDatabase;

public class ActivityPromo extends MyActivityAbstract {

    private String myUserId;
    private TextInputLayout promoCodeWrapper;
    private EditText editTextPromoCode;
    private Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promo);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(R.string.promo_code_activity_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle bundle = this.getIntent().getExtras();
        myUserId = bundle.getString("userId");

        promoCodeWrapper = (TextInputLayout) findViewById(R.id.promoCodeWrapper);
        editTextPromoCode = (EditText) findViewById(R.id.editTextPromoCode);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        setListeners();


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void validateAndSubmit(){
        if(AndroidUtils.validateEditText(editTextPromoCode, promoCodeWrapper, getString(R.string.empty_promo_code_error_msg))){
            final ProgressDialog progressDialog = AndroidUtils.loading(getString(R.string.checking_promo_code_msg), this, new Runnable() {
                @Override
                public void run() {
                }
            });

            RestfulService.usePromoCode(myUserId, editTextPromoCode.getText().toString(), new RestfulListener<String>() {
                @Override
                public void onResult(final String result, final Status status) {
                    Threadings.postRunnable(ActivityPromo.this, new Runnable(){
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            if(status == Status.Success){
                                Integer resultInt = Integer.valueOf(result);
                                if(resultInt > 0){
                                    AndroidUtils.showDialog(ActivityPromo.this, "", String.format(getString(R.string.promo_code_success_msg), result), new Runnable() {
                                        @Override
                                        public void run() {
                                            finish();
                                        }
                                    });
                                    Analytics.logEvent(AnalyticEvent.Insert_PromoCode_Success);
                                    return;
                                }
                            }

                            promoCodeWrapper.setErrorEnabled(true);
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
