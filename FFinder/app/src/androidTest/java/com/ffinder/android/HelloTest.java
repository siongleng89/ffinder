package com.ffinder.android;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by sionglengho on 17/11/16.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class HelloTest {

    @Rule
    public ActivityTestRule<ActivityLaunch> mActivityRule = new ActivityTestRule(ActivityLaunch.class);

    @Test
    public void listGoesOverTheFold() {
        //onView(withId(R.id.layoutDefault)).check(matches(isDisplayed()));
    }
}