package com.ffinder.android;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by SiongLeng on 30/8/2016.
 */
public class FragmentNextAdsCd extends Fragment {

    public static FragmentNextAdsCd newInstance() {
        return new FragmentNextAdsCd();
    }


    public FragmentNextAdsCd() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_next_ads_cd, container, false);
    }

}
