package com.ffinder.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.ffinder.android.absint.activities.MyActivityAbstract;
import com.ffinder.android.adapters.SettingsAdapter;
import com.ffinder.android.controls.LanguagesDialog;
import com.ffinder.android.enums.PhoneBrand;
import com.ffinder.android.models.MyModel;
import com.ffinder.android.models.SettingsModel;
import com.ffinder.android.utils.AndroidUtils;

import java.util.ArrayList;
import java.util.List;

public class ActivityKnownIssues extends MyActivityAbstract {

    private ArrayList<String> knownIssuesList;
    private ListView listViewKnownIssues;
    private ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_known_issues);


        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(R.string.known_issues_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        populateKnownIssues();
        listViewKnownIssues = (ListView) findViewById(R.id.lvKnownIssues);
        arrayAdapter = new ArrayAdapter<String>(this, R.layout.lvitem_single_text, R.id.textView, knownIssuesList);
        listViewKnownIssues.setAdapter(arrayAdapter);

        setListeners();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void populateKnownIssues(){
        knownIssuesList = new ArrayList();
        knownIssuesList.add(PhoneBrand.Huawei.name());
        knownIssuesList.add(PhoneBrand.Xiaomi.name());
        knownIssuesList.add(PhoneBrand.Sony.name());
    }


    private void setListeners(){
        listViewKnownIssues.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PhoneBrand brandName = PhoneBrand.valueOf(knownIssuesList.get(position));

                String msg = "";
                if(brandName == PhoneBrand.Xiaomi){
                    msg = getString(R.string.issue_fix_xiaomi);
                }
                else if(brandName == PhoneBrand.Huawei){
                    msg = getString(R.string.issue_fix_huawei);
                }
                else if(brandName == PhoneBrand.Sony){
                    msg = getString(R.string.issue_fix_sony);
                }

                AndroidUtils.showDialog(ActivityKnownIssues.this, "", msg, null, null);
            }
        });
    }

}
