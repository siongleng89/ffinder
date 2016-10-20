package com.ffinder.android.extentions;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import com.ffinder.android.R;
import com.ffinder.android.utils.AndroidUtils;

/**
 * Created by sionglengho on 20/10/16.
 */
public class ImageButtonWhiteStyle extends ImageButton {

    private Context context;

    public ImageButtonWhiteStyle(Context context) {
        super(context);
        init(context);
    }

    public ImageButtonWhiteStyle(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ImageButtonWhiteStyle(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context){
        this.context = context;

        this.setScaleType(ScaleType.CENTER);

        this.setColorFilter(Color.parseColor("#588e7e"),
                                PorterDuff.Mode.SRC_ATOP);
        AndroidUtils.setButtonBackground(context, this, R.drawable.white_btn);


        setListener();
    }

    public void setListener(){
        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                    ImageButtonWhiteStyle.this.getBackground()
                            .setColorFilter(Color.parseColor("#497462"),
                                    PorterDuff.Mode.SRC_ATOP);
                    ImageButtonWhiteStyle.this.setColorFilter(Color.parseColor("#ffffff"),
                            PorterDuff.Mode.SRC_ATOP);
                }
                else if (event.getAction() == android.view.MotionEvent.ACTION_UP ||
                             event.getAction() == MotionEvent.ACTION_CANCEL) {
                    ImageButtonWhiteStyle.this.getBackground()
                            .setColorFilter(null);
                    ImageButtonWhiteStyle.this.setColorFilter(Color.parseColor("#588e7e"),
                            PorterDuff.Mode.SRC_ATOP);
                }
                return false;
            }
        });
    }


}
