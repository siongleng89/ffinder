package com.ffinder.android;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.ffinder.android.absint.activities.MyActivityAbstract;
import com.ffinder.android.adapters.SettingsAdapter;
import com.ffinder.android.controls.LanguagesDialog;
import com.ffinder.android.models.MyModel;
import com.ffinder.android.models.SettingsModel;

import java.util.ArrayList;

public class ActivitySettings extends MyActivityAbstract {

    private ListView listViewSettings;
    private SettingsAdapter settingsAdapter;
    private ArrayList<SettingsModel> settingsModels;
    private MyModel myModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(R.string.settings_activity_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        myModel = new MyModel(this);
        populateSettingsModel();
        listViewSettings = (ListView) findViewById(R.id.listViewSettings);
        settingsAdapter = new SettingsAdapter(this, R.layout.lvitem_setting, settingsModels, myModel);
        listViewSettings.setAdapter(settingsAdapter);

        setListeners();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void populateSettingsModel(){
        settingsModels = new ArrayList();
        settingsModels.add(new SettingsModel(getString(R.string.settings_item_language_title)));
        settingsModels.add(new SettingsModel(getString(R.string.vip_title)));
    }

    private void setListeners(){
        listViewSettings.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SettingsModel settingsModel = settingsModels.get(position);
                if(settingsModel.getTitle().equals(getString(R.string.settings_item_language_title))){
                    LanguagesDialog languagesDialog = new LanguagesDialog(ActivitySettings.this, myModel);
                    languagesDialog.show();
                }
                else if(settingsModel.getTitle().equals(getString(R.string.vip_title))){
                    Intent intent = new Intent(ActivitySettings.this, ActivityVip.class);
                    intent.putExtra("userId", myModel.getUserId());
                    startActivity(intent);
                }
            }
        });
    }



}
