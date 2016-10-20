package com.ffinder.android.helpers;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import com.ffinder.android.enums.AnimateType;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

import static com.ffinder.android.enums.AnimateType.*;

/**
 * Created by sionglengho on 19/10/16.
 */
public class AnimateBuilder {

    private AnimateBuilder animateBuilder;
    private Context context;
    private View view;
    private float value;
    private AnimateType animateType;
    private int durationMs;
    private boolean repeat;
    private Runnable finishCallback;

    public static AnimateBuilder build(Context context, View view){
        AnimateBuilder animateBuilder = new AnimateBuilder(context, view);
        return animateBuilder;
    }

    private AnimateBuilder(Context context, View view) {
        this.context = context;
        this.view = view;
    }

    public AnimateBuilder setAnimateType(AnimateType animateType) {
        this.animateType = animateType;
        return this;
    }

    public AnimateBuilder setValue(float value) {
        this.value = value;
        return this;
    }

    public AnimateBuilder setDurationMs(int durationMs) {
        this.durationMs = durationMs;
        return this;
    }

    public AnimateBuilder setRepeat(boolean repeat) {
        this.repeat = repeat;
        return this;
    }

    public AnimateBuilder setFinishCallback(Runnable finishCallback) {
        this.finishCallback = finishCallback;
        return this;
    }

    public static void fadeIn(Context context, View view){
        AnimateBuilder.build(context, view).setAnimateType(AnimateType.alpha)
                .setValue(1).setDurationMs(300).start();
    }

    public static void fadeOut(Context context, View view){
        AnimateBuilder.build(context, view).setAnimateType(AnimateType.alpha)
                .setValue(0).setDurationMs(300).start();
    }

    public void start(){

        view.clearAnimation();
        ViewPropertyAnimator.animate(view).setListener(null).cancel();

        // have to use object animator if wanna use repeat, but will not be able to use finish listener
        if(repeat){
            ObjectAnimator anim = null;
            switch (animateType){
                case alpha:
                    anim =  ObjectAnimator.ofFloat(view, "alpha", value);
                    break;
                case moveByX:
                    anim =  ObjectAnimator.ofFloat(view, "translationX", dpToPixel(value));
                    break;
                case moveByY:
                    anim =  ObjectAnimator.ofFloat(view, "translationY", dpToPixel(value));
                    break;
                case rotate:
                    anim =  ObjectAnimator.ofFloat(view, "rotation", value);
                    break;

            }

            anim.setDuration(durationMs);
            anim.setRepeatCount(ObjectAnimator.INFINITE);
            anim.setRepeatMode(ObjectAnimator.REVERSE);
            anim.start();


        }
        else{
            ViewPropertyAnimator viewPropertyAnimator = null;

            switch (animateType){
                case alpha:
                    if(value == 1){
                        ViewHelper.setAlpha(view, 0);
                        view.setVisibility(View.VISIBLE);
                    }

                    viewPropertyAnimator =
                            ViewPropertyAnimator.animate(view).setDuration(durationMs).alpha(value);
                    break;
                case moveByX:
                    viewPropertyAnimator = ViewPropertyAnimator.animate(view)
                            .setDuration(durationMs)
                            .translationXBy(dpToPixel(value));
                    break;
                case moveByY:
                    viewPropertyAnimator = ViewPropertyAnimator.animate(view)
                            .setDuration(durationMs)
                            .translationYBy(dpToPixel(value));
                    break;
                case rotate:

                    break;

            }

            if (finishCallback != null && viewPropertyAnimator != null){
                viewPropertyAnimator.setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        finishCallback.run();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });

            }
        }
    }





    private float dpToPixel(float dp){
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics()
        );
    }

}
