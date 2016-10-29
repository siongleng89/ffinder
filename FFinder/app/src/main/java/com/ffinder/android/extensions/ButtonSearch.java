package com.ffinder.android.extensions;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.ffinder.android.R;
import com.ffinder.android.enums.AnimateType;
import com.ffinder.android.enums.SearchAnimationState;
import com.ffinder.android.helpers.AnimateBuilder;
import com.ffinder.android.helpers.SearchAnimationPool;
import com.ffinder.android.helpers.Threadings;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.view.ViewHelper;

import java.util.ArrayList;

/**
 * Created by sionglengho on 26/10/16.
 */
public class ButtonSearch extends RelativeLayout {

    private Context context;
    private ImageView imgViewFlower;
    private ViewGroup viewGroup;
    private LinearLayout layoutMainButton;
    private TextView txtLastUpdated, txtStatus;
    private int originalSrcColor, onTapSrcColor, originalBackgroundColor, onTapBackgroundColor;
    private SearchAnimationState currentState;
    private AnimatorSet statusFadingAnimatorSet;

    public ButtonSearch(Context context) {
        super(context);
        init(context);
    }

    public ButtonSearch(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ButtonSearch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){
        this.context = context;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.viewGroup = (ViewGroup) inflater.inflate(R.layout.btn_search, this, true);

        imgViewFlower = (ImageView) viewGroup.findViewById(R.id.imgViewFlower);
        layoutMainButton = (LinearLayout) viewGroup.findViewById(R.id.layoutMainButton);
        txtLastUpdated = (TextView) viewGroup.findViewById(R.id.txtLastUpdated);
        txtStatus = (TextView) viewGroup.findViewById(R.id.txtStatus);

        originalSrcColor = ContextCompat.getColor(context, R.color.colorPrimaryDark);
        onTapSrcColor = ContextCompat.getColor(context, R.color.colorContrast);
        originalBackgroundColor = ContextCompat.getColor(context, R.color.colorContrast);
        onTapBackgroundColor = ContextCompat.getColor(context, R.color.colorPrimaryDark);


        changeSearchState(SearchAnimationState.Sleeping);

        this.setClickable(true);
        imgViewFlower.setColorFilter(originalSrcColor, PorterDuff.Mode.SRC_ATOP);
        layoutMainButton.getBackground().setColorFilter(originalBackgroundColor, PorterDuff.Mode.SRC_ATOP);
    }

    public void changeSearchState(SearchAnimationState newState, final String... extras){
        if(newState == SearchAnimationState.Starting
                && currentState == SearchAnimationState.SearchingHappy){
            return;
        }

        if(newState == currentState) return;

        currentState = newState;

        switch (newState){
            case Sleeping:
                animateInLastUpdated();
                setStatusText(context.getString(R.string.search));
                imgViewFlower.setImageDrawable(
                             SearchAnimationPool.getDrawables(context, "flower_sleeping_0", 1).get(0)
                        );
                break;
            case Satisfied:
                animateInLastUpdated();
                setStatusText(context.getString(R.string.search));
                imgViewFlower.setImageDrawable(
                        SearchAnimationPool.getDrawables(context, "flower_satisfied_0", 1).get(0)
                );
                break;
            case DoNothing:
                setStatusText(context.getString(R.string.search));
                imgViewFlower.setImageDrawable(
                             SearchAnimationPool.getDrawables(context, "flower_starting_0", 1).get(0)
                )       ;
                break;
            case Starting:
                animateOutLastUpdated();
                setStatusText(context.getString(R.string.searching));
                final CustomAnimationDrawable startingAnimation = getAnimationDrawable("flower_starting_0",
                        12, 30, false, new Runnable() {
                            @Override
                            public void run() {
                                changeSearchState(SearchAnimationState.SearchingHappy);
                            }
                        });
                imgViewFlower.setImageDrawable(startingAnimation);

                Threadings.delay(200, new Runnable() {
                    @Override
                    public void run() {
                        startingAnimation.start();
                    }
                });

                break;
            case SearchingHappy:
                final CustomAnimationDrawable searchHappyAnimation = getAnimationDrawable(
                        "flower_happy_swing_0",
                        14, 50, true, null);
                imgViewFlower.setImageDrawable(searchHappyAnimation);
                searchHappyAnimation.start();

                break;
            case SearchingTrouble:
                final CustomAnimationDrawable searchTroublingAnimation = getAnimationDrawable(
                        "flower_troubling_0",
                        14, 50, true, null);
                imgViewFlower.setImageDrawable(searchTroublingAnimation);
                searchTroublingAnimation.start();

                break;
            case SearchingDizzy:
                final CustomAnimationDrawable searchDizzyAnimation = getAnimationDrawable(
                        "flower_confusing_0",
                        14, 50, true, null);
                imgViewFlower.setImageDrawable(searchDizzyAnimation);
                searchDizzyAnimation.start();

                break;

            case SearchSuccess:
                final CustomAnimationDrawable searchSuccessAnimation = getAnimationDrawable(
                        "flower_search_success_0",
                        14, 90, true, new Runnable() {
                            @Override
                            public void run() {
                                Threadings.delay(1000, new Runnable() {
                                    @Override
                                    public void run() {
                                        changeSearchState(SearchAnimationState.Ending, "searchSucceed");
                                    }
                                });
                            }
                        });
                imgViewFlower.setImageDrawable(searchSuccessAnimation);
                searchSuccessAnimation.start();



                break;

            case Ending:
                final CustomAnimationDrawable searchEndingAnimation = getAnimationDrawable(
                        "flower_ending_0",
                        12, 30, false, new Runnable() {
                            @Override
                            public void run() {

                                if(extras != null && extras.length > 0){
                                    if(extras[0].equals("autoSearching")){
                                        changeSearchState(SearchAnimationState.AutoSearching);
                                        return;
                                    }
                                    else if(extras[0].equals("searchSucceed")){
                                        changeSearchState(SearchAnimationState.Satisfied);
                                        return;
                                    }
                                }

                                setStatusText(context.getString(R.string.search));
                                changeSearchState(SearchAnimationState.Sleeping);
                            }
                        });
                imgViewFlower.setImageDrawable(searchEndingAnimation);
                searchEndingAnimation.start();
                break;

            case AutoSearching:
                animateInLastUpdated();
                setStatusText(context.getString(R.string.auto_searching));
                final CustomAnimationDrawable autoSearchAnimation = getAnimationDrawable(
                        "flower_auto_searching_0",
                        15, 70, 4000);
                imgViewFlower.setImageDrawable(autoSearchAnimation);
                autoSearchAnimation.start();
                break;

        }


    }

    public void animateOutLastUpdated(){
        if(ViewHelper.getX(txtLastUpdated) == 100) return;

        AnimateBuilder.build(context, txtLastUpdated)
                .setAnimateType(AnimateType.moveByX)
                .setDurationMs(200)
                .setValue(100)
                .start();

    }

    public void animateInLastUpdated(){
        if(ViewHelper.getX(txtLastUpdated) == 0) return;

        AnimateBuilder.build(context, txtLastUpdated)
                .setAnimateType(AnimateType.moveByX)
                .setDurationMs(200)
                .setValue(-100)
                .start();
    }

    public void setLastUpdated(String input){
        txtLastUpdated.setText(input);
    }

    public void setStatusText(final String text){
        txtStatus.setText(text);
    }

    private CustomAnimationDrawable getAnimationDrawable(String name, int totalFrames,
                                      int frameDuration, boolean repeat,
                                      final Runnable onFinish){
        final CustomAnimationDrawable animation = new CustomAnimationDrawable(){
            @Override
            public void onAnimationFinish(CustomAnimationDrawable animation) {
            if (onFinish != null){
                onFinish.run();
            }
            }
        };

        animation.setOneShot(!repeat);

        ArrayList<Drawable> drawables = SearchAnimationPool.getDrawables(context, name, totalFrames);
        for(Drawable drawable : drawables){
            animation.addFrame(drawable, frameDuration);
        }

        return animation;
    }


    private CustomAnimationDrawable getAnimationDrawable(String name, int totalFrames,
                                                         int frameDuration, final int repeatDelay){

        CustomAnimationDrawable animation = new CustomAnimationDrawable(){
            @Override
            public void onAnimationFinish(final CustomAnimationDrawable theAnimation) {
                Threadings.delay(repeatDelay, new Runnable() {
                    @Override
                    public void run() {
                        theAnimation.reset();
                    }
                });
            }
        };

        animation.setOneShot(true);

        ArrayList<Drawable> drawables = SearchAnimationPool.getDrawables(context, name, totalFrames);
        for(Drawable drawable : drawables){
            animation.addFrame(drawable, frameDuration);
        }

        return animation;
    }




}
