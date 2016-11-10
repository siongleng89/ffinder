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
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.ffinder.android.R;
import com.ffinder.android.helpers.AndroidUtils;
import com.ffinder.android.helpers.AnimateBuilder;
import com.ffinder.android.helpers.Threadings;

/**
 * Created by sionglengho on 10/11/16.
 */
public class ButtonTab extends RelativeLayout {

    private Context context;
    private Drawable imageSrcDrawable;
    private String tabText;
    private int backgroundNormalColor, backgroundOnTapColor;
    private ImageView imgViewIcon;
    private TextView txtView;
    private ViewGroup layoutBtn;
    private boolean selected;

    public ButtonTab(Context context) {
        super(context);
        init(context);
    }

    public ButtonTab(Context context, AttributeSet attrs) {
        super(context, attrs);
        extractAttrs(attrs);
        init(context);
    }

    public ButtonTab(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        extractAttrs(attrs);
        init(context);
    }

    private void extractAttrs(AttributeSet attrs){
        TypedArray a=getContext().obtainStyledAttributes(
                attrs,
                R.styleable.ButtonTab);

        imageSrcDrawable = a.getDrawable (R.styleable.ButtonTab_tabImageSrc);
        tabText = a.getString(R.styleable.ButtonTab_tabText);

        //Don't forget this
        a.recycle();
    }

    private void init(Context context){
        this.context = context;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.btn_tab, this, true);

        layoutBtn = (ViewGroup) this.findViewById(R.id.layoutBtn);
        imgViewIcon = (ImageView) layoutBtn.findViewById(R.id.imgViewIcon);
        txtView = (TextView) layoutBtn.findViewById(R.id.txtView);

        int color= Color.parseColor("#000000");
        int red=   (color >> 16) & 0xFF;
        int green= (color >> 8) & 0xFF;
        int blue=  (color >> 0) & 0xFF;

        backgroundNormalColor = Color.argb(0, red, green, blue);
        backgroundOnTapColor = Color.parseColor("#000000");

        imgViewIcon.setImageDrawable(imageSrcDrawable);
        txtView.setText(tabText);

        this.setClickable(true);
        this.setFocusable(true);

        setListener();

    }

    public void setListener(){
        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {

                    if(selected) return false;

                    layoutBtn.getBackground().mutate().setColorFilter((int) backgroundOnTapColor,
                            PorterDuff.Mode.SRC_ATOP);

                }
                else if (event.getAction() == android.view.MotionEvent.ACTION_UP ||
                        event.getAction() == MotionEvent.ACTION_CANCEL) {

                    if(selected) return false;

                    layoutBtn.getBackground().mutate().setColorFilter((int) backgroundNormalColor,
                            PorterDuff.Mode.SRC_ATOP);

                }
                return false;
            }
        });

    }

    public void setSelected(boolean selected) {
        if(this.selected == selected) return;

        this.selected = selected;

        if(selected){
            layoutBtn.getBackground().mutate().setColorFilter((int) backgroundOnTapColor,
                    PorterDuff.Mode.SRC_ATOP);
        }
        else{
            layoutBtn.getBackground().mutate().setColorFilter((int) backgroundNormalColor,
                    PorterDuff.Mode.SRC_ATOP);
        }

    }

}
