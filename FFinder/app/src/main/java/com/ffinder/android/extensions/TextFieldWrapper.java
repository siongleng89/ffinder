package com.ffinder.android.extensions;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.text.method.DigitsKeyListener;
import android.text.method.KeyListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import com.ffinder.android.R;
import com.ffinder.android.enums.AnimateType;
import com.ffinder.android.helpers.AnimateBuilder;
import com.ffinder.android.helpers.Strings;

/**
 * Created by sionglengho on 21/10/16.
 */
public class TextFieldWrapper extends RelativeLayout {

    private Context context;
    private String wrapperTitle;
    private LinearLayout layoutWrapper;
    private TextView txtLabel, txtError;
    private EditText editTxtField;
    private ImageView imgViewHelp;
    private int labelNormalColor, labelOnFocusColor;

    public TextFieldWrapper(Context context) {
        super(context);
        init(context);
    }

    public TextFieldWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);
        extractAttrs(attrs);
        init(context);
    }

    public TextFieldWrapper(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        extractAttrs(attrs);
        init(context);
    }

    private void extractAttrs(AttributeSet attrs){
        TypedArray a=getContext().obtainStyledAttributes(
                attrs,
                R.styleable.TextFieldWrapper);

        wrapperTitle = a.getString(R.styleable.TextFieldWrapper_wrapperTitle);

        if(Strings.isEmpty(wrapperTitle)){
            wrapperTitle = "Lorem ipsum";
        }

        //Don't forget this
        a.recycle();
    }

    @Override
    protected Parcelable onSaveInstanceState() {

        Bundle bundle = new Bundle();
        bundle.putParcelable("instanceState", super.onSaveInstanceState());
        bundle.putString("currentEdit", editTxtField.getText().toString());
        bundle.putBoolean("isFocused", editTxtField.hasFocus());
        return bundle;

    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {

        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            editTxtField.setText(bundle.getString("currentEdit"));
            if (bundle.getBoolean("isFocused")) {
                editTxtField.requestFocus();
            }
            super.onRestoreInstanceState(bundle.getParcelable("instanceState"));
            return;
        }

        super.onRestoreInstanceState(state);
    }

    private void init(Context context){
        this.context = context;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.wrapper_text_field, this, true);

        labelNormalColor = ContextCompat.getColor(context, R.color.colorNormalText);
        labelOnFocusColor = ContextCompat.getColor(context, R.color.colorPrimaryDark);


        layoutWrapper = (LinearLayout) this.findViewById(R.id.layoutWrapper);
        txtLabel = (TextView) layoutWrapper.findViewById(R.id.txtLabel);
        txtError = (TextView) layoutWrapper.findViewById(R.id.txtError);
        imgViewHelp = (ImageView) layoutWrapper.findViewById(R.id.imgViewHelp);
        //cannot use id and must use tag for rotation problem
        editTxtField = (EditText) layoutWrapper.findViewWithTag("editTxtField");

        txtLabel.setText(wrapperTitle);

        setListeners();
    }


    public boolean validateNotEmpty(String errorMsg){
        if (Strings.isEmpty(editTxtField.getText().toString())){
            setError(errorMsg);
            return false;
        }
        else{
            clearError();
            return true;
        }
    }

    public void setError(String errorMsg){
        txtError.setText(errorMsg);
        AnimateBuilder.fadeIn(context, txtError);
    }

    public void clearError(){
        AnimateBuilder.build(context, txtError).setAnimateType(AnimateType.alpha)
                .setDurationMs(200).setValue(0).setFinishCallback(new Runnable() {
            @Override
            public void run() {
                txtError.setVisibility(GONE);
            }
        }).start();
    }

    public void setNumericOnly(){
        KeyListener keyListener = DigitsKeyListener.getInstance("0123456789-");
        editTxtField.setKeyListener(keyListener);
    }

    public void setHelpClickListener(OnClickListener clickListener){
        imgViewHelp.setVisibility(VISIBLE);
        imgViewHelp.setOnClickListener(clickListener);
    }

    private void setListeners(){
        editTxtField.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    AnimateBuilder.animateTextColor(labelNormalColor, labelOnFocusColor, txtLabel);
                }else {
                    AnimateBuilder.animateTextColor(labelOnFocusColor, labelNormalColor, txtLabel);
                }
            }
        });
    }


    public String getText(){
        return editTxtField.getText().toString();
    }

    public void setText(String text){
        editTxtField.setText(text);
    }

    public EditText getEditTxtField() {
        return editTxtField;
    }



}
