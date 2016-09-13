package com.ffinder.android;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.ffinder.android.absint.databases.FirebaseListener;
import com.ffinder.android.enums.Status;
import com.ffinder.android.helpers.FirebaseDB;
import com.ffinder.android.models.MyModel;
import com.ffinder.android.utils.*;
import com.google.firebase.iid.FirebaseInstanceId;

/**
 * Created by SiongLeng on 30/8/2016.
 */
public class ActivitySetup extends AppCompatActivity {

    private boolean initialized;
    private ActivitySetup _this;
    private TextView txtStatus;
    private String currentStatus;
    private boolean failed;
    private AnimationDrawable frameAnimation;
    private ImageView imgViewLoading;
    private Button btnRetry;
    private String userId;
    private MyModel myModel;

    public ActivitySetup() {
        _this = this;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setup);

        Bundle b = getIntent().getExtras();
        userId = b.getString("userId");

        myModel = new MyModel(this);
        myModel.delete();

        txtStatus = (TextView) findViewById(R.id.txtStatus);
        setCurrentStatus(currentStatus);

        btnRetry = (Button) findViewById(R.id.btnRetry);

        imgViewLoading = (ImageView)findViewById(R.id.imgViewLoading);
        imgViewLoading.setBackgroundResource(R.drawable.loading_animation);

        frameAnimation = (AnimationDrawable) imgViewLoading.getBackground();

        setFailed(failed);

        setListeners();
        init();
    }

    private void init(){
        if(!initialized){
            initialized = true;
            startProcess();
        }
    }

    private void startProcess(){
        setFailed(false);
        setCurrentStatus(getString(R.string.retrieving_token));
        if(!DeviceInfo.isNetworkAvailable(_this)){
            failed(FailedCode.NoConnection);
            return;
        }

        Threadings.runInBackground(new Runnable() {
            @Override
            public void run() {
                String token = null;
                int i = 0;
                while (Strings.isEmpty(token)){
                    token = FirebaseInstanceId.getInstance().getToken();
                    if(!Strings.isEmpty(token) || i > 10) break;
                    Threadings.sleep(1000);
                    i++;
                }

                if(Strings.isEmpty(token)){
                    failed(FailedCode.NoToken);
                }
                else{
                    setCurrentStatus(getString(R.string.initializing_user));

                    myModel.setUserId(userId);
                    final String finalToken = token;
                    myModel.loginFirebase(0, new RunnableArgs<Boolean>() {
                        @Override
                        public void run() {
                            if(this.getFirstArg()){
                                FirebaseDB.saveNewUser(userId, finalToken, new FirebaseListener<String>() {
                                    @Override
                                    public void onResult(String userId, Status status) {
                                        if(status == Status.Success){
                                            myModel.setUserId(userId);
                                            myModel.save();
                                            setCurrentStatus(getString(R.string.initialization_done));
                                            Intent k = new Intent(_this, ActivityMain.class);
                                            startActivity(k);
                                        }
                                        else{
                                            failed(FailedCode.DBFailed);
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    private void failed(FailedCode failedCode){
        String msg = "";
        switch (failedCode){
            case NoConnection:
                msg = getString(R.string.no_connection_msg);
                break;
            case NoToken:
                msg = getString(R.string.no_token_msg);
                break;
            case DBFailed:
                msg = getString(R.string.db_failed_msg);
                break;
        }

        setCurrentStatus(msg);
        setFailed(true);

    }

    public void setListeners(){
        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startProcess();
            }
        });
    }

    private enum FailedCode{
        NoConnection, NoToken, DBFailed
    }

    public void setCurrentStatus(final String currentStatus) {
        this.currentStatus = currentStatus;
        if(txtStatus != null && currentStatus != null){
            System.out.println(this.currentStatus);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txtStatus.setText(currentStatus);
                }
            });
        }
    }

    public void setFailed(final boolean failed) {
        this.failed = failed;

        if(imgViewLoading != null){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(!failed){
                        frameAnimation.run();
                    }
                    else{
                        frameAnimation.stop();
                    }

                    btnRetry.setVisibility(failed ? View.VISIBLE : View.GONE);
                }
            });
        }



    }

}
