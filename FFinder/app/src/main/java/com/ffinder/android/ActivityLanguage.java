package com.ffinder.android;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.ffinder.android.absint.activities.MyActivityAbstract;
import com.ffinder.android.adapters.LanguagesAdapter;
import com.ffinder.android.enums.ActionBarActionType;
import com.ffinder.android.enums.AnalyticEvent;
import com.ffinder.android.helpers.Analytics;
import com.ffinder.android.helpers.LocaleHelper;
import com.ffinder.android.models.LanguageModel;
import com.ffinder.android.models.MyModel;

import java.util.ArrayList;

public class ActivityLanguage extends MyActivityAbstract {

    private ListView listViewLanguage;
    private ArrayList<LanguageModel> languageModels;
    private LanguagesAdapter languagesAdapter;
    private String originalLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);
        enableCustomActionBar();
        setActionBarTitle(R.string.settings_item_language_title);
        addActionToActionBar(ActionBarActionType.Back, false, true);
        addActionToActionBar(ActionBarActionType.OK, false, false);

        populateLanguageModels();

        listViewLanguage = (ListView) findViewById(R.id.listViewLanguage);
        languagesAdapter = new LanguagesAdapter(this, R.layout.lvitem_single_text, languageModels);
        listViewLanguage.setAdapter(languagesAdapter);

        setListeners();
    }

    @Override
    public void onActionButtonClicked(ActionBarActionType actionBarActionType) {
        super.onActionButtonClicked(actionBarActionType);

        if (actionBarActionType == ActionBarActionType.OK){
            confirmLanguage();
        }
    }


    private void confirmLanguage(){
        for(LanguageModel languageModel : languageModels){
            if(languageModel.isSelected() && !languageModel.getAbbr().equals(originalLanguage)){
                Toast.makeText(
                        this, R.string.language_take_effect_after_restart_toast, Toast.LENGTH_LONG)
                        .show();
                LocaleHelper.persist(this, languageModel.getAbbr());
                Analytics.logEvent(AnalyticEvent.Change_Language, languageModel.getAbbr());
                break;
            }
        }
        finish();
    }



    private void populateLanguageModels(){
        languageModels = new ArrayList();
        languageModels.add(new LanguageModel("English", "en"));
        languageModels.add(new LanguageModel("Português", "pt"));
        languageModels.add(new LanguageModel("中文", "zh"));
        languageModels.add(new LanguageModel("Indonesia", "in"));
        languageModels.add(new LanguageModel("Malay", "ms"));

        originalLanguage = LocaleHelper.getLanguage(this);
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
