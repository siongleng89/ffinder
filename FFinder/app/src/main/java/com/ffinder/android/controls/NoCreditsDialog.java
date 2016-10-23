package com.ffinder.android.controls;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import com.ffinder.android.ActivityPromo;
import com.ffinder.android.ActivityVip;
import com.ffinder.android.R;
import com.ffinder.android.absint.controls.INoCreditsListener;
import com.ffinder.android.enums.OverlayType;
import com.ffinder.android.helpers.OverlayBuilder;
import com.ffinder.android.models.MyModel;

/**
 * Created by SiongLeng on 16/9/2016.
 */
public class NoCreditsDialog {

    private Activity activity;
    private MyModel myModel;
    private RelativeLayout layoutWatchAds, layoutPromoCode, layoutSubscribe, layoutQuit;
    private AlertDialog dialog;
    private INoCreditsListener noCreditsListener;

    public NoCreditsDialog(Activity activity, MyModel myModel, INoCreditsListener noCreditsListener) {
        this.activity = activity;
        this.noCreditsListener = noCreditsListener;
        this.myModel = myModel;
    }

    public void show(){

        View viewInflated = LayoutInflater.from(activity).inflate(R.layout.dialog_no_credits, null);

        layoutWatchAds = (RelativeLayout) viewInflated.findViewById(R.id.layoutWatchAds);
        layoutPromoCode = (RelativeLayout) viewInflated.findViewById(R.id.layoutPromoCode);
        layoutSubscribe = (RelativeLayout) viewInflated.findViewById(R.id.layoutSubscribe);
        layoutQuit = (RelativeLayout) viewInflated.findViewById(R.id.layoutQuit);

        dialog = OverlayBuilder.build(activity)
                        .setOverlayType(OverlayType.CustomView)
                        .setCustomView(viewInflated)
                        .show();

        setListeners();
    }

    public void setListeners(){
        layoutWatchAds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                noCreditsListener.requestWatchAds();
            }
        });
        layoutPromoCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent intent = new Intent(activity, ActivityPromo.class);
                intent.putExtra("userId", myModel.getUserId());
                activity.startActivity(intent);
            }
        });
        layoutSubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent intent = new Intent(activity, ActivityVip.class);
                intent.putExtra("userId", myModel.getUserId());
                activity.startActivity(intent);
            }
        });
        layoutQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }


}
