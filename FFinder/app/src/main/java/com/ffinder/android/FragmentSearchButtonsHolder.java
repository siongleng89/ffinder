package com.ffinder.android;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.ffinder.android.extensions.ButtonSearch;
import com.ffinder.android.helpers.Threadings;

import java.util.HashMap;

/**
 * Created by sionglengho on 26/10/16.
 */
public class FragmentSearchButtonsHolder extends Fragment {

    private HashMap<String, ButtonSearch> storage;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        storage = new HashMap();
    }

    public ButtonSearch getButton(String userId){
        if(!storage.containsKey(userId)){
            ButtonSearch buttonSearch = new ButtonSearch(getContext());
            storage.put(userId, buttonSearch);
        }

        ButtonSearch buttonSearch = storage.get(userId);
        if(buttonSearch.getParent() != null){
            ((ViewGroup) buttonSearch.getParent()).removeAllViews();
        }

        return storage.get(userId);
    }




}
