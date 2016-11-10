package com.ffinder.android.extensions;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import com.ffinder.android.R;
import com.ffinder.android.helpers.AnimateBuilder;
import com.ffinder.android.helpers.AndroidUtils;
import com.ffinder.android.helpers.Strings;

/**
 * Created by sionglengho on 21/10/16.
 */
public class FFTextButton extends Button {

    private Context context;
    private String colorScheme;
    private int btnNormalColor, btnOnTapColor, textNormalColor, textOnTapColor;

    public FFTextButton(Context context) {
        super(context);
        init(context);
    }

    public FFTextButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        extractAttrs(attrs);
        init(context);
    }

    public FFTextButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        extractAttrs(attrs);
        init(context);
    }

    private void extractAttrs(AttributeSet attrs){
        TypedArray a=getContext().obtainStyledAttributes(
                attrs,
                R.styleable.FFTextButton);

        colorScheme = a.getString(R.styleable.FFTextButton_ffBtnColorScheme);

        if(Strings.isEmpty(colorScheme)){
            colorScheme = "green";
        }

        //Don't forget this
        a.recycle();
    }



    private void init(Context context){
        this.context = context;

        AndroidUtils.setButtonBackground(context, this, R.drawable.btn_bg);

        if (colorScheme.equals("green")){
            textNormalColor = Color.WHITE;
            textOnTapColor = Color.WHITE;

            btnNormalColor = Color.parseColor("#abd489");
            btnOnTapColor = Color.parseColor("#497462");
        }
        else if (colorScheme.equals("grey")){
            textNormalColor = Color.parseColor("#7c7c7c");
            textOnTapColor = Color.WHITE;

            btnNormalColor = Color.parseColor("#f6f6f6");
            btnOnTapColor = Color.parseColor("#7c7c7c");
        }

        this.setTransformationMethod(null);
        FFTextButton.this.setTypeface(null, Typeface.BOLD);
        FFTextButton.this.setTextColor(textNormalColor);
        FFTextButton.this.getBackground().setColorFilter(btnNormalColor,
                                                PorterDuff.Mode.SRC_ATOP);

        setListener();
    }

    public void setListener(){
        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                    AnimateBuilder.animateTextColor(textNormalColor, textOnTapColor, FFTextButton.this);
                    AnimateBuilder.animateBackgroundTintColor(btnNormalColor, btnOnTapColor, FFTextButton.this);
                }
                else if (event.getAction() == android.view.MotionEvent.ACTION_UP ||
                        event.getAction() == MotionEvent.ACTION_CANCEL) {
                    AnimateBuilder.animateTextColor(textOnTapColor, textNormalColor, FFTextButton.this);
                    AnimateBuilder.animateBackgroundTintColor(btnOnTapColor, btnNormalColor, FFTextButton.this);
                }
                return false;
            }
        });
    }





}
