package com.ffinder.android.controls;

import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.ffinder.android.R;
import com.ffinder.android.enums.OverlayType;
import com.ffinder.android.enums.TutorialType;
import com.ffinder.android.helpers.AndroidUtils;
import com.ffinder.android.helpers.OverlayBuilder;

import java.util.ArrayList;

/**
 * Created by sionglengho on 28/10/16.
 */
public class TutorialDialog {

    private Activity activity;
    private ImageView imgViewPrev, imgViewNext, imgViewScreenshot, imgViewClose;
    private TextView txtTitle, txtSubtitle;
    private AlertDialog dialog;
    private ArrayList<TutorialStepModel> tutorialStepModels;
    private int currentIndex;
    private TutorialType tutorialType;

    public TutorialDialog(Activity activity, TutorialType tutorialType) {
        this.activity = activity;
        this.tutorialType = tutorialType;
        tutorialStepModels = new ArrayList();
        populateModels();
    }

    public void show(){

        View viewInflated = LayoutInflater.from(activity).inflate(R.layout.dialog_tutorial, null);

        imgViewPrev = (ImageView) viewInflated.findViewById(R.id.imgViewPrev);
        imgViewNext = (ImageView) viewInflated.findViewById(R.id.imgViewNext);
        imgViewScreenshot = (ImageView) viewInflated.findViewById(R.id.imgViewScreenshot);
        imgViewClose = (ImageView) viewInflated.findViewById(R.id.imgViewClose);
        txtTitle = (TextView) viewInflated.findViewById(R.id.txtSubscriptionTitle);
        txtSubtitle = (TextView) viewInflated.findViewById(R.id.txtSubtitle);

        goToIndex(0);

        dialog = OverlayBuilder.build(activity)
                .setOverlayType(OverlayType.CustomView)
                .setCustomView(viewInflated)
                .show();

        setListeners();
    }


    private void goToIndex(int index){
        boolean disablePrev = false, disableNext = false;

        if(index <= 0){
            index = 0;
            disablePrev = true;
        }

        if(index >= tutorialStepModels.size() - 1){
            index = tutorialStepModels.size() - 1;
            disableNext = true;
        }

        currentIndex = index;
        TutorialStepModel tutorialStepModel = tutorialStepModels.get(index);

        imgViewScreenshot.setImageDrawable(ContextCompat.getDrawable(activity,
                                        tutorialStepModel.getDrawableId()));
        txtSubtitle.setText(tutorialStepModel.getSubtitle());
        txtTitle.setText(tutorialStepModel.getTitle());

        if(disablePrev){
            imgViewPrev.setVisibility(View.INVISIBLE);
        }
        else{
            imgViewPrev.setVisibility(View.VISIBLE);
        }

        if(disableNext){
            imgViewNext.setVisibility(View.INVISIBLE);
        }
        else{
            imgViewNext.setVisibility(View.VISIBLE);
        }
    }

    private void populateModels(){
        ArrayList<String> ids = new ArrayList();


        if(this.tutorialType == TutorialType.AddManuallyPasscode){
            ids.add("add_manually_a1");
            ids.add("add_manually_a2");
            ids.add("add_manually_a3");
            ids.add("add_manually_a4");
        }
        else if(this.tutorialType == TutorialType.SharePasscode){
            ids.add("share_passcode_s1");
            ids.add("share_passcode_s2");
            ids.add("share_passcode_s3");
            ids.add("share_passcode_s4");
        }

        for(String id : ids){
            tutorialStepModels.add(new TutorialStepModel("",
                    activity.getString(AndroidUtils.getStringIdentifier(activity, id)),
                    AndroidUtils.getDrawableIdentifier(activity, id)));
        }

    }

    public void setListeners(){
        imgViewClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        imgViewPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToIndex(--currentIndex);
            }
        });

        imgViewNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToIndex(++currentIndex);
            }
        });

        setImageViewTouchListeners(imgViewPrev);
        setImageViewTouchListeners(imgViewNext);

    }

    private void setImageViewTouchListeners(final ImageView imageView){
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                    imageView.setColorFilter(ContextCompat.getColor(activity, R.color.colorPrimaryDark));
                }
                else if (event.getAction() == android.view.MotionEvent.ACTION_UP ||
                        event.getAction() == MotionEvent.ACTION_CANCEL) {
                    imageView.setColorFilter(null);
                }
                return false;
            }
        });
    }


    private class TutorialStepModel{
        private String title, subtitle;
        private int drawableId;

        public TutorialStepModel(String title, String subtitle, int drawableId) {
            this.title = title;
            this.subtitle = subtitle;
            this.drawableId = drawableId;
        }

        public String getTitle() {
            return title;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public int getDrawableId() {
            return drawableId;
        }
    }

}
