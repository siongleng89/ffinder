package com.ffinder.android.extensions;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.ffinder.android.R;
import com.ffinder.android.helpers.AnimateBuilder;
import com.ffinder.android.helpers.AndroidUtils;
import com.ffinder.android.helpers.Threadings;

/**
 * Created by sionglengho on 22/10/16.
 */
public class ButtonWhite extends RelativeLayout {

    private Context context;
    private int innerPaddingTopPixel, innerPaddingBottomPixel, innerPaddingLeftPixel,
            innerPaddingRightPixel;
    private Drawable imageSrcDrawable;
    private int backgroundNormalColor, backgroundOnTapColor, srcNormalColor, srcOnTapColor,
                    backgroundDisabledColor, srcDisabledColor;
    private ImageView imgBtn;
    private RelativeLayout layoutBtn;
    private boolean selected;
    private boolean disabled;

    public ButtonWhite(Context context) {
        super(context);
        init(context);
    }

    public ButtonWhite(Context context, AttributeSet attrs) {
        super(context, attrs);
        extractAttrs(attrs);
        init(context);
    }

    public ButtonWhite(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        extractAttrs(attrs);
        init(context);
    }

    private void extractAttrs(AttributeSet attrs){
        TypedArray a=getContext().obtainStyledAttributes(
                attrs,
                R.styleable.ButtonWhite);

        imageSrcDrawable = a.getDrawable (R.styleable.ButtonWhite_imageSrc);

        int innerPaddingPixel = a.getDimensionPixelSize(R.styleable.ButtonWhite_innerPadding, 0);
        if (innerPaddingPixel == 0){
            innerPaddingTopPixel = a.getDimensionPixelSize(R.styleable.ButtonWhite_innerPaddingTop, 0);
            innerPaddingBottomPixel = a.getDimensionPixelSize(R.styleable.ButtonWhite_innerPaddingBottom, 0);
            innerPaddingLeftPixel = a.getDimensionPixelSize(R.styleable.ButtonWhite_innerPaddingLeft, 0);
            innerPaddingRightPixel = a.getDimensionPixelSize(R.styleable.ButtonWhite_innerPaddingRight, 0);
        }
        else{
            innerPaddingTopPixel = innerPaddingPixel;
            innerPaddingBottomPixel = innerPaddingPixel;
            innerPaddingLeftPixel = innerPaddingPixel;
            innerPaddingRightPixel = innerPaddingPixel;
        }

        //Don't forget this
        a.recycle();
    }

    private void init(Context context){
        this.context = context;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.btn_white, this, true);

        layoutBtn = (RelativeLayout) this.findViewById(R.id.layoutBtn);
        imgBtn = (ImageView) layoutBtn.findViewById(R.id.imgBtn);

        backgroundNormalColor = Color.WHITE;
        backgroundOnTapColor = ContextCompat.getColor(context, R.color.colorPrimaryDark);
        backgroundDisabledColor = Color.parseColor("#CCCCCC");

        srcNormalColor = ContextCompat.getColor(context, R.color.colorPrimaryDark);
        srcOnTapColor = ContextCompat.getColor(context, R.color.colorContrast);
        srcDisabledColor = ContextCompat.getColor(context, R.color.colorContrast);

        layoutBtn.setPadding(innerPaddingLeftPixel, innerPaddingTopPixel,
                innerPaddingRightPixel, innerPaddingBottomPixel);
        imgBtn.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imgBtn.setImageDrawable(imageSrcDrawable);

        imgBtn.setColorFilter(srcNormalColor,
                PorterDuff.Mode.SRC_ATOP);
        AndroidUtils.setButtonBackground(context, layoutBtn, R.drawable.btn_bg);

        this.setClickable(true);
        this.setFocusable(true);

        setListener();

    }

    public void setImageSrcDrawable(int drawableId){
        imgBtn.setImageDrawable(ContextCompat.getDrawable(context, drawableId));
    }

    public void setListener(){
        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {

                    if(selected || disabled) return false;

                    AnimateBuilder.animateBackgroundTintColor(backgroundNormalColor,
                            backgroundOnTapColor, layoutBtn);
                    AnimateBuilder.animateSrcTintColor(srcNormalColor, srcOnTapColor,
                            imgBtn);

                }
                else if (event.getAction() == android.view.MotionEvent.ACTION_UP ||
                        event.getAction() == MotionEvent.ACTION_CANCEL) {

                    if(selected || disabled) return false;

                    //delay for better animation with selected
                    Threadings.delay(150, new Runnable() {
                        @Override
                        public void run() {
                            if(selected || disabled) return;

                            AnimateBuilder.animateBackgroundTintColor(backgroundOnTapColor,
                                    backgroundNormalColor, layoutBtn);
                            AnimateBuilder.animateSrcTintColor(srcOnTapColor, srcNormalColor,
                                    imgBtn);
                        }
                    });

                }
                return false;
            }
        });

    }

    public void setSelected(boolean selected, boolean animate) {
        this.selected = selected;

        AnimateBuilder.stopAllAnimation(layoutBtn);
        AnimateBuilder.stopAllAnimation(imgBtn);

        if(animate){
            AnimateBuilder.stopAllAnimation(layoutBtn);
            AnimateBuilder.stopAllAnimation(imgBtn);
            if(selected){
                AnimateBuilder.animateBackgroundTintColor(backgroundNormalColor,
                        backgroundOnTapColor, layoutBtn);
                AnimateBuilder.animateSrcTintColor(srcNormalColor, srcOnTapColor,
                        imgBtn);
            }
            else{
                AnimateBuilder.animateBackgroundTintColor(backgroundOnTapColor,
                        backgroundNormalColor, layoutBtn);
                AnimateBuilder.animateSrcTintColor(srcOnTapColor, srcNormalColor,
                        imgBtn);
            }
        }
        else{
            if(selected){
                layoutBtn.getBackground().setColorFilter((int) backgroundOnTapColor,
                        PorterDuff.Mode.SRC_ATOP);
                imgBtn.setColorFilter(srcOnTapColor,
                        PorterDuff.Mode.SRC_ATOP);
            }
            else{
                layoutBtn.getBackground().setColorFilter((int) backgroundNormalColor,
                        PorterDuff.Mode.SRC_ATOP);
                imgBtn.setColorFilter(srcNormalColor,
                        PorterDuff.Mode.SRC_ATOP);
            }
        }

    }

    public void setDisabled(boolean isDisabled){
        this.disabled = isDisabled;


        if(this.disabled){
            this.setClickable(false);
            layoutBtn.getBackground().setColorFilter((int) backgroundDisabledColor,
                    PorterDuff.Mode.SRC_ATOP);
            imgBtn.setColorFilter(srcDisabledColor,
                    PorterDuff.Mode.SRC_ATOP);
        }
        else{
            this.setClickable(true);
            layoutBtn.getBackground().setColorFilter((int) backgroundNormalColor,
                    PorterDuff.Mode.SRC_ATOP);
            imgBtn.setColorFilter(srcNormalColor,
                    PorterDuff.Mode.SRC_ATOP);
        }

    }


}
