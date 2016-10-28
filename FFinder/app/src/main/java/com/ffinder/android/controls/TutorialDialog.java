package com.ffinder.android.controls;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.ffinder.android.R;
import com.ffinder.android.enums.OverlayType;
import com.ffinder.android.helpers.AnimateBuilder;
import com.ffinder.android.helpers.OverlayBuilder;
import com.ffinder.android.helpers.Threadings;

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

    public TutorialDialog(Activity activity) {
        this.activity = activity;
        tutorialStepModels = new ArrayList();
        populateModels();
    }

    public void show(){

        View viewInflated = LayoutInflater.from(activity).inflate(R.layout.dialog_tutorial, null);

        imgViewPrev = (ImageView) viewInflated.findViewById(R.id.imgViewPrev);
        imgViewNext = (ImageView) viewInflated.findViewById(R.id.imgViewNext);
        imgViewScreenshot = (ImageView) viewInflated.findViewById(R.id.imgViewScreenshot);
        imgViewClose = (ImageView) viewInflated.findViewById(R.id.imgViewClose);
        txtTitle = (TextView) viewInflated.findViewById(R.id.txtTitle);
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
        tutorialStepModels.add(new TutorialStepModel("Test", "test again", R.drawable.add_friend1));
        tutorialStepModels.add(new TutorialStepModel("Test2", "test again2", R.drawable.add_friend1_1));
        tutorialStepModels.add(new TutorialStepModel("Test3", "test again3", R.drawable.add_friend1_2));
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
