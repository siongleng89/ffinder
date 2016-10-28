package com.ffinder.android.helpers;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.ffinder.android.enums.AnimateType;
import com.nineoldandroids.animation.*;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

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

    public static void fadeIn(Context context, View view, final Runnable... runnables){
        if(view.getVisibility() == View.VISIBLE && ViewHelper.getAlpha(view) == 1){
            if(runnables != null){
                for(Runnable runnable : runnables){
                    runnable.run();
                }
            }
            return;
        }

        AnimateBuilder.build(context, view).setAnimateType(AnimateType.alpha)
                .setValue(1).setDurationMs(300).setFinishCallback(new Runnable() {
            @Override
            public void run() {
                if(runnables != null){
                    for(Runnable runnable : runnables){
                        runnable.run();
                    }
                }
            }
        }).start();
    }

    public static void fadeOut(Context context, View view, final Runnable... runnables){
        if(ViewHelper.getAlpha(view) == 0){
            if(runnables != null){
                for(Runnable runnable : runnables){
                    runnable.run();
                }
            }
            return;
        }

        AnimateBuilder.build(context, view).setAnimateType(AnimateType.alpha)
                .setValue(0).setDurationMs(300).setFinishCallback(new Runnable() {
            @Override
            public void run() {
                if(runnables != null){
                    for(Runnable runnable : runnables){
                        runnable.run();
                    }
                }
            }
        }).start();
    }

    public static void fadeOutAndSetGone(Context context, final View view, final Runnable... runnables){
        if(view.getVisibility() == View.GONE){
            if(runnables != null){
                for(Runnable runnable : runnables){
                    runnable.run();
                }
            }
            return;
        }

        AnimateBuilder.build(context, view).setAnimateType(AnimateType.alpha)
                .setValue(0).setDurationMs(300).setFinishCallback(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(View.GONE);

                if(runnables != null){
                    for(Runnable runnable : runnables){
                        runnable.run();
                    }
                }
            }
        }).start();
    }


    public static void animateSrcTintColor(final int fromColor, final int toColor, final ImageView view){
        if (fromColor == toColor) return;

        Threadings.postRunnable(new Runnable() {
            @Override
            public void run() {
                ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), fromColor, toColor);
                colorAnimation.setDuration(200); // milliseconds
                colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {

                        view.setColorFilter((int) animator.getAnimatedValue(),
                                PorterDuff.Mode.SRC_ATOP);
                    }

                });
                colorAnimation.start();
                view.setTag(colorAnimation);
            }
        });


    }

    public static void animateBackgroundTintColor(final int fromColor, final int toColor, final View view){
        if (fromColor == toColor) return;

        Threadings.postRunnable(new Runnable() {
            @Override
            public void run() {
                ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), fromColor, toColor);
                colorAnimation.setDuration(200); // milliseconds
                colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        view.getBackground().setColorFilter((int) animator.getAnimatedValue(),
                                PorterDuff.Mode.SRC_ATOP);
                    }

                });
                colorAnimation.start();
                view.setTag(colorAnimation);
            }
        });


    }

    public static void animateTextColor(final int fromColor, final int toColor, final TextView view){
        if (fromColor == toColor) return;

        Threadings.postRunnable(new Runnable() {
            @Override
            public void run() {
                ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), fromColor, toColor);
                colorAnimation.setDuration(200); // milliseconds
                colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        view.setTextColor((int) animator.getAnimatedValue());
                    }

                });
                colorAnimation.start();
                view.setTag(colorAnimation);
            }
        });


    }

    public static void stopAllAnimation(final View view){
        Threadings.postRunnable(new Runnable() {
            @Override
            public void run() {
                view.clearAnimation();
                if(view.getAnimation() != null){
                    view.getAnimation().cancel();
                }

                ViewPropertyAnimator.animate(view).cancel();

                Object animatorObj = view.getTag();
                if(animatorObj != null && animatorObj instanceof ValueAnimator){
                    ((ValueAnimator) animatorObj).cancel();
                }
            }
        });
    }

    public void start(){
        Threadings.postRunnable(new Runnable() {
            @Override
            public void run() {
                view.clearAnimation();
                ViewPropertyAnimator.animate(view).cancel();

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
                                Logs.show("animation start");
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                finishCallback.run();
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {
                                Logs.show("animation cancel");
                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        });

                    }


                    viewPropertyAnimator.start();

                }
            }
        });

    }





    private float dpToPixel(float dp){
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics()
        );
    }

}
