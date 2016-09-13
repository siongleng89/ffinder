package com.ffinder.android;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.ffinder.android.absint.activities.IAppsIntroductionListener;
import com.ffinder.android.controls.UserKeyDialog;
import com.ffinder.android.enums.PreferenceType;
import com.ffinder.android.models.MyModel;
import com.ffinder.android.utils.PreferenceUtils;

/**
 * Created by SiongLeng on 13/9/2016.
 */
public class FragmentAppsIntroduction extends Fragment {

    private Button btnOk;
    private IAppsIntroductionListener appsIntroductionListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View appsIntroductionView = inflater.inflate(R.layout.fragment_apps_introduction, container, false);

        btnOk = (Button) appsIntroductionView.findViewById(R.id.btnOk);

        setListeners();
        return appsIntroductionView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        appsIntroductionListener = (IAppsIntroductionListener) context;
    }

    private void setListeners(){
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferenceUtils.put(getContext(), PreferenceType.SeenAppsIntroduction, "1");
                appsIntroductionListener.onCompleteAppsIntroduction();
            }
        });
    }

}
