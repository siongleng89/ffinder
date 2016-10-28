package com.ffinder.android.helpers;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.ffinder.android.R;
import com.ffinder.android.enums.OverlayType;
import com.ffinder.android.extensions.FFTextButton;

import java.util.ArrayList;

/**
 * Created by sionglengho on 23/10/16.
 */
public class OverlayBuilder {

    private Activity activity;
    private OverlayType overlayType;
    private String title;
    private String content;
    private String checkboxTitle;
    private View contentView;
    private View customView;
    private Runnable onDismiss;
    private ArrayList<Runnable> runnables;
    private ArrayList<String> btnTexts;
    private AppCompatCheckBox checkbox;

    public static OverlayBuilder build(Activity activity){
        OverlayBuilder overlayBuilder = new OverlayBuilder(activity);
        return overlayBuilder;
    }

    public OverlayBuilder(Activity activity) {
        this.activity = activity;
        this.runnables = new ArrayList();
        this.btnTexts = new ArrayList();
    }

    public OverlayBuilder setOverlayType(OverlayType overlayType) {
        this.overlayType = overlayType;
        return this;
    }

    public OverlayBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public OverlayBuilder setContent(String content) {
        this.content = content;
        return this;
    }

    public OverlayBuilder setCheckboxTitle(String checkboxTitle) {
        this.checkboxTitle = checkboxTitle;
        return this;
    }

    public OverlayBuilder setOnDismissRunnable(Runnable onDismiss) {
        this.onDismiss = onDismiss;
        return this;
    }

    public OverlayBuilder setContentView(View contentView) {
        this.contentView = contentView;
        return this;
    }

    public OverlayBuilder setCustomView(View customView) {
        this.customView = customView;
        return this;
    }

    public OverlayBuilder setRunnables(Runnable... runnables) {
        for(Runnable runnable : runnables){
            this.runnables.add(runnable);
        }
        return this;
    }

    public OverlayBuilder setBtnTexts(String... texts) {
        for(String text : texts){
            this.btnTexts.add(text);
        }
        return this;
    }

    public AlertDialog show(){

        View viewInflated = null;

        switch (overlayType){
            case Loading:

                viewInflated = LayoutInflater.from(activity).inflate(R.layout.dialog_loading_layout,
                        (ViewGroup) activity.getWindow().getDecorView().findViewById(android.R.id.content), false);

                TextView loadingTextView = (TextView) viewInflated.findViewById(R.id.loadingTextView);
                loadingTextView.setText(content);
                break;

            case CustomView:
                viewInflated = LayoutInflater.from(activity).inflate(R.layout.dialog_empty_layout,
                        (ViewGroup) activity.getWindow().getDecorView().findViewById(android.R.id.content), false);


                ((RelativeLayout) viewInflated.findViewById(R.id.layoutContent)).addView(customView);
                break;

            case OkCancel:

                viewInflated = LayoutInflater.from(activity).inflate(R.layout.dialog_normal_layout,
                        (ViewGroup) activity.getWindow().getDecorView().findViewById(android.R.id.content), false);


                btnTexts.add(activity.getString(R.string.ok));
                btnTexts.add(activity.getString(R.string.cancel));
                break;

            case OkOnly:

                viewInflated = LayoutInflater.from(activity).inflate(R.layout.dialog_normal_layout,
                        (ViewGroup) activity.getWindow().getDecorView().findViewById(android.R.id.content), false);


                btnTexts.add(activity.getString(R.string.ok));
                break;

            case YesNo:

                viewInflated = LayoutInflater.from(activity).inflate(R.layout.dialog_normal_layout,
                        (ViewGroup) activity.getWindow().getDecorView().findViewById(android.R.id.content), false);


                btnTexts.add(activity.getString(R.string.yes));
                btnTexts.add(activity.getString(R.string.no));
                break;

            case CustomButtons:

                viewInflated = LayoutInflater.from(activity).inflate(R.layout.dialog_normal_layout,
                        (ViewGroup) activity.getWindow().getDecorView().findViewById(android.R.id.content), false);

                break;

        }

        if(!Strings.isEmpty(title)) {
            View layoutTitle = viewInflated.findViewById(R.id.layoutTitle);
            if(layoutTitle != null){
                layoutTitle.setVisibility(View.VISIBLE);
            }

            TextView txtTitle = (TextView) viewInflated.findViewById(R.id.txtTitle);
            if (txtTitle != null){
                txtTitle.setText(title);
            }
        }

        if(!Strings.isEmpty(content)) {
            TextView txtContent = (TextView) viewInflated.findViewById(R.id.txtContent);
            if (txtContent != null){
                txtContent.setText(content);
                txtContent.setVisibility(View.VISIBLE);
            }
        }

        if(!Strings.isEmpty(checkboxTitle)) {
            View layoutCheckbox = viewInflated.findViewById(R.id.layoutCheckbox);
            if(layoutCheckbox != null){
                layoutCheckbox.setVisibility(View.VISIBLE);
            }


            checkbox = (AppCompatCheckBox) viewInflated.findViewById(R.id.checkboxView);
            if (checkbox != null){
                checkbox.setText(checkboxTitle);
            }
        }

        if(contentView != null){
            ViewGroup layoutContent = (ViewGroup) viewInflated.findViewById(R.id.layoutContent);
            if(layoutContent != null){
                layoutContent.addView(contentView);
            }
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(viewInflated);

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if(onDismiss != null) onDismiss.run();
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        for(int i = 0; i < btnTexts.size(); i++){
            int resId = activity.getResources().getIdentifier("btn" + i, "id", activity.getPackageName());
            FFTextButton ffTextButton = (FFTextButton) viewInflated.findViewById(resId);
            ffTextButton.setText(btnTexts.get(i));

            ffTextButton.setVisibility(View.VISIBLE);

            ffTextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
        }

        for(int i = 0; i < runnables.size(); i++){
            int resId = activity.getResources().getIdentifier("btn" + i, "id", activity.getPackageName());
            FFTextButton ffTextButton = (FFTextButton) viewInflated.findViewById(resId);

            final int finalI = i;
            ffTextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    runnables.get(finalI).run();
                    dialog.dismiss();
                }
            });
        }

        return dialog;

    }

    public boolean isChecked(){
        if(checkbox != null){
            return checkbox.isChecked();
        }
        return false;
    }

}
