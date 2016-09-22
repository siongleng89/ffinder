package com.ffinder.android;

import android.app.Activity;
import android.os.Bundle;
import com.ffinder.android.utils.Logs;

/**
 * Created by SiongLeng on 22/9/2016.
 */
public class ActivityDummy extends Activity {

    @Override
    public void onCreate( Bundle icicle ) {
        super.onCreate( icicle );
        Logs.show("Dummy is opened");
        finish();
    }

}
