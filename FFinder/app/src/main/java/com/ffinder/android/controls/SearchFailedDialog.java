package com.ffinder.android.controls;

import android.content.Intent;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.ffinder.android.ActivityKnownIssues;
import com.ffinder.android.R;
import com.ffinder.android.absint.activities.MyActivityAbstract;
import com.ffinder.android.absint.controls.ISearchFailedListener;
import com.ffinder.android.enums.AnalyticEvent;
import com.ffinder.android.enums.SearchResult;
import com.ffinder.android.helpers.Analytics;

/**
 * Created by SiongLeng on 20/9/2016.
 */
public class SearchFailedDialog {

    private MyActivityAbstract activity;
    private AlertDialog dialog;
    private RelativeLayout layoutMessage;
    private Button btnSearchAgain, btnWait;
    private ISearchFailedListener searchFailedListener;
    private SearchResult searchResult;

    public SearchFailedDialog(MyActivityAbstract activity, SearchResult searchResult, ISearchFailedListener searchFailedListener) {
        this.activity = activity;
        this.searchResult = searchResult;
        this.searchFailedListener = searchFailedListener;
    }

    public void show(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        View viewInflated = LayoutInflater.from(activity).inflate(R.layout.dialog_search_failed,
                (ViewGroup) activity.getWindow().getDecorView().findViewById(android.R.id.content), false);

        layoutMessage = (RelativeLayout) viewInflated.findViewById(R.id.layoutMessage);
        btnSearchAgain = (Button) viewInflated.findViewById(R.id.btnSearchAgain);
        btnWait = (Button) viewInflated.findViewById(R.id.btnWait);

        View searchFailedView = LayoutInflater.from(activity).inflate(R.layout.layout_search_failed,
                (ViewGroup) activity.getWindow().getDecorView().findViewById(android.R.id.content), false);

        TextView txtPartOne = (TextView) searchFailedView.findViewById(R.id.txtPartOne);
        TextView txtPartTwo = (TextView) searchFailedView.findViewById(R.id.txtPartTwo);
        TextView txtPartThree = (TextView) searchFailedView.findViewById(R.id.txtPartThree);
        TextView txtPartFour = (TextView) searchFailedView.findViewById(R.id.txtPartFour);

        txtPartOne.setText(activity.getString(R.string.failed_search_msg_text1));

        if(searchResult == SearchResult.ErrorTimeoutUnknownReason){
            txtPartTwo.setText(activity.getString(R.string.error_timeout_unknown_reason_possible_reasons1));
            txtPartThree.setText(activity.getString(R.string.error_timeout_unknown_reason_possible_reasons2));
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) txtPartThree
                    .getLayoutParams();
            mlp.setMargins(0, -10, 0, 0);
            txtPartThree.setPaintFlags(txtPartThree.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
            txtPartThree.setTextColor(ContextCompat.getColor(activity, R.color.colorAccent));

            txtPartFour.setText(activity.getString(R.string.failed_search_msg_text2));
            setListenerForKnownIssue(txtPartThree);
        }

        layoutMessage.addView(searchFailedView);

        builder.setView(viewInflated);
        dialog = builder.create();
        dialog.show();

        setListeners();
    }


    public void setListeners(){
        btnSearchAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchFailedListener.onSearchAnywayChoose();
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
    }

    private void setListenerForKnownIssue(TextView textView){
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, ActivityKnownIssues.class);
                activity.startActivity(intent);
            }
        });
    }

}
