package com.ffinder.android.absint.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import com.ffinder.android.helpers.Analytics;

/**
 * Created by SiongLeng on 16/9/2016.
 */
public class MyActivityAbstract extends AppCompatActivity {

    public MyActivityAbstract() {

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Analytics.logToScreen(this);
    }
}
