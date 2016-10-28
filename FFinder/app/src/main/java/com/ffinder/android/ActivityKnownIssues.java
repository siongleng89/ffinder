package com.ffinder.android;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.ffinder.android.absint.activities.MyActivityAbstract;
import com.ffinder.android.enums.ActionBarActionType;
import com.ffinder.android.enums.OverlayType;
import com.ffinder.android.enums.PhoneBrand;
import com.ffinder.android.helpers.AndroidUtils;
import com.ffinder.android.helpers.OverlayBuilder;

import java.util.ArrayList;

public class ActivityKnownIssues extends MyActivityAbstract {

    private ArrayList<String> knownIssuesList;
    private ListView listViewKnownIssues;
    private ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_known_issues);
        enableCustomActionBar();
        setActionBarTitle(R.string.known_issues_title);
        addActionToActionBar(ActionBarActionType.Back, false, true);

        populateKnownIssues();
        listViewKnownIssues = (ListView) findViewById(R.id.lvKnownIssues);
        arrayAdapter = new ArrayAdapter<String>(this, R.layout.lvitem_single_text, R.id.textView, knownIssuesList);
        listViewKnownIssues.setAdapter(arrayAdapter);

        setListeners();
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

                String msg = AndroidUtils.getPhoneBrandKnownIssue(ActivityKnownIssues.this, brandName);

                OverlayBuilder.build(ActivityKnownIssues.this)
                        .setOverlayType(OverlayType.OkOnly)
                        .setContent(msg)
                        .show();

            }
        });
    }

}
