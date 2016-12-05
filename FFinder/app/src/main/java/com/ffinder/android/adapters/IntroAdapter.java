package com.ffinder.android.adapters;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import com.ffinder.android.FragmentIntroItem;
import com.ffinder.android.R;
import com.ffinder.android.absint.adapters.ViewPagerListener;

/**
 * Created by sionglengho on 5/12/16.
 */
public class IntroAdapter extends FragmentPagerAdapter {

    private Activity activity;
    private ViewPagerListener viewPagerListener;

    public IntroAdapter(FragmentManager fm, Activity activity, ViewPagerListener viewPagerListener) {
        super(fm);
        this.activity = activity;
        this.viewPagerListener = viewPagerListener;
    }

    @Override
    public Fragment getItem(int i) {
        FragmentIntroItem fragment = new FragmentIntroItem();
        fragment.setViewPagerListener(viewPagerListener);
        Bundle args = new Bundle();

        args.putInt("index", i);
        args.putInt("total", 6);

        if(i == 0){
            args.putInt("imageId", R.mipmap.intro_1);
            args.putString("content", activity.getString(R.string.apps_intro1));
        }
        else if(i == 1){
            args.putInt("imageId", R.mipmap.intro_2);
            args.putString("content", activity.getString(R.string.apps_intro2));
        }
        else if(i == 2){
            args.putInt("imageId", R.mipmap.intro_3);
            args.putString("content", activity.getString(R.string.apps_intro3));
        }
        else if(i == 3){
            args.putInt("imageId", R.mipmap.intro_4);
            args.putString("content", activity.getString(R.string.apps_intro4));
        }
        else if(i == 4){
            args.putInt("imageId", R.mipmap.intro_5);
            args.putString("content", activity.getString(R.string.apps_intro5));
        }
        else if(i == 5){
            args.putInt("imageId", R.mipmap.intro_6);
            args.putString("content", activity.getString(R.string.apps_intro6));
        }

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return 6;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "";
    }
}