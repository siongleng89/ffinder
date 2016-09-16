package com.ffinder.android.controls;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.ffinder.android.R;
import com.ffinder.android.adapters.LanguagesAdapter;
import com.ffinder.android.enums.AnalyticEvent;
import com.ffinder.android.helpers.Analytics;
import com.ffinder.android.helpers.LocaleHelper;
import com.ffinder.android.models.LanguageModel;
import com.ffinder.android.models.MyModel;

import java.util.ArrayList;

/**
 * Created by SiongLeng on 8/9/2016.
 */
public class LanguagesDialog {

    private Activity activity;
    private MyModel myModel;
    private ListView listViewLanguage;
    private AlertDialog dialog;
    private ArrayList<LanguageModel> languageModels;
    private LanguagesAdapter languagesAdapter;
    private String originalLanguage;

    public LanguagesDialog(Activity activity, MyModel myModel) {
        this.activity = activity;
        this.myModel = myModel;

        populateLanguageModels();
    }

    public void show(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        View viewInflated = LayoutInflater.from(activity).inflate(R.layout.dialog_languages,
                (ViewGroup) activity.getWindow().getDecorView().findViewById(android.R.id.content), false);

        listViewLanguage = (ListView) viewInflated.findViewById(R.id.listViewLanguage);
        languagesAdapter = new LanguagesAdapter(activity, R.layout.lvitem_language, languageModels, myModel);
        listViewLanguage.setAdapter(languagesAdapter);

        builder.setView(viewInflated);

        // Set up the buttons
        builder.setPositiveButton(activity.getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                for(LanguageModel languageModel : languageModels){
                    if(languageModel.isSelected() && !languageModel.getAbbr().equals(originalLanguage)){
                        Toast.makeText(
                                activity, R.string.language_take_effect_after_restart_toast, Toast.LENGTH_LONG)
                                .show();
                        LocaleHelper.setLocale(activity, languageModel.getAbbr());
                        break;
                    }
                }
                dialog.dismiss();
            }
        });

        setListeners();
    }

    private void populateLanguageModels(){
        languageModels = new ArrayList();
        languageModels.add(new LanguageModel("English", "en"));
        languageModels.add(new LanguageModel("Português", "pt"));

        originalLanguage = LocaleHelper.getLanguage(activity);
        for(LanguageModel languageModel : languageModels){
            if(languageModel.getAbbr().equals(originalLanguage)){
                languageModel.setSelected(true);
            }
        }
    }

    public void setListeners(){
        listViewLanguage.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for(int i = 0; i < languageModels.size(); i++){
                    if(i == position){
                        languageModels.get(i).setSelected(true);
                        Analytics.logEvent(AnalyticEvent.Change_Language, languageModels.get(i).getAbbr());
                    }
                    else{
                        languageModels.get(i).setSelected(false);
                    }
                }
                languagesAdapter.notifyDataSetChanged();
            }
        });
    }

}
