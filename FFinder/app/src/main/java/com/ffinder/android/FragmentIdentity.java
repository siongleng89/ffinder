package com.ffinder.android;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.ffinder.android.controls.UserKeyDialog;
import com.ffinder.android.models.MyModel;

/**
 * Created by SiongLeng on 30/8/2016.
 */
public class FragmentIdentity extends Fragment {

    private Button btnShareKey;
    private MyModel myModel;

    public static FragmentIdentity newInstance() {
        return new FragmentIdentity();
    }


    public FragmentIdentity() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View identityFragmentView = inflater.inflate(R.layout.fragment_identity, container, false);

        btnShareKey = (Button) identityFragmentView.findViewById(R.id.btnShareKey);

        setListeners();
        return identityFragmentView;
    }

    public void setMyModel(MyModel newModel){
        this.myModel = newModel;
    }




    public void setListeners(){
        btnShareKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserKeyDialog userKeyDialog = new UserKeyDialog(getActivity(), myModel);
                userKeyDialog.show();
            }
        });
    }







}
