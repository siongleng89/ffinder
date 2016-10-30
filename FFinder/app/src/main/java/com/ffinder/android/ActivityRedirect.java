package com.ffinder.android;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.ffinder.android.statics.Vars;

import java.util.List;

//this activity is just for those open fffinder by clicking the url
public class ActivityRedirect extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        populatePendingAddUserKey();

        // Check to see if this Activity is the root activity
        if (isTaskRoot()) {
            // This Activity is the only Activity, so
            //  the app wasn't running. So start the app from the
            //  beginning (redirect to MainActivity)
            Intent mainIntent = getIntent(); // Copy the Intent used to launch me
            // Launch the real root Activity (launch Intent)
            mainIntent.setClass(this, ActivityLaunch.class);
            // I'm done now, so finish()
            startActivity(mainIntent);
            finish();
        } else {
            // App was already running, so just finish, which will drop the user
            //  in to the activity that was at the top of the task stack
            finish();
        }

    }


    //user is enter FFfinder through whatsapp msg
    private void populatePendingAddUserKey(){
        final Intent intent = getIntent();
        final String action = intent.getAction();

        if (Intent.ACTION_VIEW.equals(action)) {
            final List<String> segments = intent.getData().getPathSegments();
            if (segments.size() == 2) {
                Vars.pendingAddUserKey = segments.get(1);
            }
        }
    }

}
