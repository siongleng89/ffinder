package com.ffinder.android.controls;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.ffinder.android.ActivityPromo;
import com.ffinder.android.ActivityVip;
import com.ffinder.android.R;
import com.ffinder.android.absint.controls.INoCreditsListener;
import com.ffinder.android.models.MyModel;

/**
 * Created by SiongLeng on 16/9/2016.
 */
public class NoCreditsDialog {

    private Activity activity;
    private MyModel myModel;
    private Button btnWatchAds, btnPromo, btnSubscript, btnQuit;
    private AlertDialog dialog;
    private INoCreditsListener noCreditsListener;

    public NoCreditsDialog(Activity activity, MyModel myModel, INoCreditsListener noCreditsListener) {
        this.activity = activity;
        this.noCreditsListener = noCreditsListener;
        this.myModel = myModel;
    }

    public void show(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        View viewInflated = LayoutInflater.from(activity).inflate(R.layout.dialog_no_credits,
                (ViewGroup) activity.getWindow().getDecorView().findViewById(android.R.id.content), false);

        btnWatchAds = (Button) viewInflated.findViewById(R.id.btnWatchAds);
        btnPromo = (Button) viewInflated.findViewById(R.id.btnPromo);
        btnSubscript = (Button) viewInflated.findViewById(R.id.btnSubscript);
        btnQuit = (Button) viewInflated.findViewById(R.id.btnQuit);

        builder.setView(viewInflated);


        dialog = builder.create();
        dialog.show();


        setListeners();
    }

    public void setListeners(){
        btnWatchAds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                noCreditsListener.requestWatchAds();
            }
        });
        btnPromo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent intent = new Intent(activity, ActivityPromo.class);
                intent.putExtra("userId", myModel.getUserId());
                activity.startActivity(intent);
            }
        });
        btnSubscript.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent intent = new Intent(activity, ActivityVip.class);
                intent.putExtra("userId", myModel.getUserId());
                activity.startActivity(intent);
            }
        });
        btnQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }


}
