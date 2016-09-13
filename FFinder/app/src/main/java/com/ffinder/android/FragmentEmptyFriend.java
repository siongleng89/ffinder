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
 * Created by SiongLeng on 8/9/2016.
 */
public class FragmentEmptyFriend extends Fragment {

    private Button btnShareKey;
    private MyModel myModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View emptyFriendView = inflater.inflate(R.layout.fragment_empty_friend, container, false);

        btnShareKey = (Button) emptyFriendView.findViewById(R.id.btnShareKey);

        setListeners();
        return emptyFriendView;
    }

    public void setMyModel(MyModel myModel) {
        this.myModel = myModel;
    }

    private void setListeners(){
        btnShareKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserKeyDialog userKeyDialog = new UserKeyDialog(getActivity(), myModel);
                userKeyDialog.show();
            }
        });
    }

}
