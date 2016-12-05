package com.ffinder.android;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.ffinder.android.absint.activities.MyActivityAbstract;
import com.ffinder.android.absint.adapters.ViewPagerListener;
import com.ffinder.android.adapters.IntroAdapter;
import com.ffinder.android.enums.PreferenceType;
import com.ffinder.android.helpers.PreferenceUtils;

public class ActivityIntro extends MyActivityAbstract {

    IntroAdapter introAdapter;
    ViewPager viewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        introAdapter = new IntroAdapter(getSupportFragmentManager(), this, viewPagerListener);
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(introAdapter);
    }


    private ViewPagerListener viewPagerListener = new ViewPagerListener() {
        @Override
        public void onEnded() {
            PreferenceUtils.put(ActivityIntro.this, PreferenceType.SeenAppsIntroduction, "1");
            Intent intent =  new Intent(ActivityIntro.this, ActivitySetup.class);
            startActivity(intent);
            finish();
        }
    };
}
