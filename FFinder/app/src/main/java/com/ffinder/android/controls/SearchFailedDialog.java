package com.ffinder.android.controls;

import android.content.Intent;
import android.graphics.Paint;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.ffinder.android.ActivityKnownIssues;
import com.ffinder.android.R;
import com.ffinder.android.absint.activities.MyActivityAbstract;
import com.ffinder.android.enums.AnalyticEvent;
import com.ffinder.android.enums.BroadcastEvent;
import com.ffinder.android.enums.OverlayType;
import com.ffinder.android.helpers.Analytics;
import com.ffinder.android.helpers.BroadcasterHelper;
import com.ffinder.android.helpers.OverlayBuilder;
import com.ffinder.android.models.FriendModel;

/**
 * Created by SiongLeng on 20/9/2016.
 */
public class SearchFailedDialog {

    private MyActivityAbstract activity;
    private AlertDialog dialog;
    private Button btnSearchAgain, btnWait;
    private RelativeLayout layoutContent, layoutClickToSeeReasons;
    private LinearLayout layoutReasons;
    private TextView textViewGoToKnownIssue;
    private FriendModel friendModel;

    public SearchFailedDialog(MyActivityAbstract activity, FriendModel friendModel) {
        this.activity = activity;
        this.friendModel = friendModel;
    }

    public void show(){
        View viewInflated = LayoutInflater.from(activity).inflate(R.layout.dialog_search_failed,
                                 null);

        btnSearchAgain = (Button) viewInflated.findViewById(R.id.btnSearchAgain);
        btnWait = (Button) viewInflated.findViewById(R.id.btnWait);
        textViewGoToKnownIssue = (TextView) viewInflated.findViewById(R.id.textViewGoToKnownIssue);

        layoutContent = (RelativeLayout) viewInflated.findViewById(R.id.layoutSubscribe);
        layoutClickToSeeReasons = (RelativeLayout) viewInflated.findViewById(R.id.layoutClickToSeeReasons);
        layoutReasons = (LinearLayout) viewInflated.findViewById(R.id.layoutReasons);

        textViewGoToKnownIssue.setPaintFlags(textViewGoToKnownIssue.getPaintFlags() |
                                                Paint.UNDERLINE_TEXT_FLAG);

        dialog = OverlayBuilder.build(activity)
                .setOverlayType(OverlayType.CustomView)
                .setCustomView(viewInflated)
                .show();

        setListeners();
    }


    public void setListeners(){
        btnSearchAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BroadcasterHelper.broadcast(dialog.getContext(),
                                BroadcastEvent.SearchAgainAnyway,
                        new Pair<String, String>("friendId", friendModel.getUserId())
                        );
                dialog.dismiss();
                Analytics.logEvent(AnalyticEvent.Search_Failed_And_Decide_To_Search_Anyway);
            }
        });

        btnWait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Analytics.logEvent(AnalyticEvent.Search_Failed_And_Decide_To_Wait_Notification);
            }
        });

        layoutContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(layoutReasons.getVisibility() != View.VISIBLE){
                    layoutClickToSeeReasons.setVisibility(View.GONE);
                    layoutReasons.setVisibility(View.VISIBLE);
                }
            }
        });

        textViewGoToKnownIssue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, ActivityKnownIssues.class);
                activity.startActivity(intent);
            }
        });
    }


}
