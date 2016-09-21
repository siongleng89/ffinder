package com.ffinder.android;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.ffinder.android.absint.activities.MyActivityAbstract;
import com.ffinder.android.absint.databases.FirebaseListener;
import com.ffinder.android.enums.Status;
import com.ffinder.android.helpers.FirebaseDB;
import com.ffinder.android.models.MyModel;
import com.ffinder.android.tasks.AdsIdTask;
import com.ffinder.android.utils.*;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by SiongLeng on 30/8/2016.
 */
public class ActivitySetup extends MyActivityAbstract {

    private boolean initialized;
    private ActivitySetup _this;
    private TextView txtStatus;
    private String currentStatus;
    private boolean failed;
    private AnimationDrawable frameAnimation;
    private ImageView imgViewLoading;
    private Button btnRetry;
    private MyModel myModel;

    public ActivitySetup() {
        _this = this;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setup);

        myModel = new MyModel(this);

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
                setCurrentStatus(getString(R.string.initializing_user));

                tryRecoverUserByGoogleAdsId(new RunnableArgs<String>() {
                    @Override
                    public void run() {
                        if(!Strings.isEmpty(this.getFirstArg())){     //successfully recovered
                            String recoverUserId = this.getFirstArg();
                            myModel.setUserId(recoverUserId);
                            myModel.save();
                            myModel.loginFirebase(0, new RunnableArgs<Boolean>() {
                                @Override
                                public void run() {
                                    if(this.getFirstArg()){
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
                        else{
                            myModel.delete();
                            final String userId = FirebaseDB.getNewUserId();
                            myModel.setUserId(userId);
                            myModel.save();

                            myModel.loginFirebase(0, new RunnableArgs<Boolean>() {
                                @Override
                                public void run() {
                                    if(this.getFirstArg()){
                                        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
                                        FirebaseDB.saveNewUser(userId, refreshedToken, new FirebaseListener() {
                                            @Override
                                            public void onResult(Object result, Status status) {
                                                if(status == Status.Success){
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
                                    else{
                                        failed(FailedCode.DBFailed);
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    private void tryRecoverUserByGoogleAdsId(final RunnableArgs<String> onResult){
        AdsIdTask adsIdTask = new AdsIdTask(this, null);
        try {
            String adsId = adsIdTask.execute().get(3, TimeUnit.SECONDS);
            FirebaseDB.getUserIdByIdentifier(adsId, new FirebaseListener<String>(String.class) {
                @Override
                public void onResult(String recoverUserId, Status status) {
                    if(status == Status.Success && !Strings.isEmpty(recoverUserId)){
                        onResult.run(recoverUserId);
                    }
                    else{
                        onResult.run(null);
                    }
                }
            });


        } catch (InterruptedException e) {
            e.printStackTrace();
            onResult.run(null);
        } catch (ExecutionException e) {
            e.printStackTrace();
            onResult.run(null);
        } catch (TimeoutException e) {
            e.printStackTrace();
            onResult.run(null);
        }

    }

    private void failed(FailedCode failedCode){
        String msg = "";
        switch (failedCode){
            case NoConnection:
                msg = getString(R.string.no_connection_msg);
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
        NoConnection, DBFailed
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
