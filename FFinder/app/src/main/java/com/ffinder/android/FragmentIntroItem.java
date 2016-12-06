package com.ffinder.android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.ffinder.android.absint.adapters.ViewPagerListener;
import com.ffinder.android.helpers.AndroidUtils;

/**
 * Created by sionglengho on 5/12/16.
 */
public class FragmentIntroItem extends Fragment {

    private ImageView imgViewMain, imgViewNext,
            indicator1, indicator2, indicator3, indicator4, indicator5, indicator6;
    private TextView txtViewMain, txtViewEnd;
    private ViewPagerListener viewPagerListener;


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        View rootView = inflater.inflate(
                R.layout.fragment_intro_item, container, false);
        Bundle args = getArguments();

        imgViewMain = (ImageView) rootView.findViewById(R.id.imgViewMain);
        imgViewNext = (ImageView) rootView.findViewById(R.id.imgViewNext);
        indicator1 = (ImageView) rootView.findViewById(R.id.indicator1);
        indicator2 = (ImageView) rootView.findViewById(R.id.indicator2);
        indicator3 = (ImageView) rootView.findViewById(R.id.indicator3);
        indicator4 = (ImageView) rootView.findViewById(R.id.indicator4);
        indicator5 = (ImageView) rootView.findViewById(R.id.indicator5);
        indicator6 = (ImageView) rootView.findViewById(R.id.indicator6);

        txtViewMain = (TextView) rootView.findViewById(R.id.txtViewMain);
        txtViewEnd = (TextView) rootView.findViewById(R.id.txtViewEnd);

        txtViewMain.setText(args.getString("content"));
        imgViewMain.setImageDrawable(ContextCompat.getDrawable(getActivity(), args.getInt("imageId", 0)));

        int index = args.getInt("index", 0);
        int total = args.getInt("total", 0);

        if(index == 0){
            ViewCompat.setAlpha(indicator1, 1);
        }
        else if(index == 1){
            ViewCompat.setAlpha(indicator2, 1);
        }
        else if(index == 2){
            ViewCompat.setAlpha(indicator3, 1);
        }
        else if(index == 3){
            ViewCompat.setAlpha(indicator4, 1);
        }
        else if(index == 4){
            ViewCompat.setAlpha(indicator5, 1);
        }
        else if(index == 5){
            ViewCompat.setAlpha(indicator6, 1);
        }

        if (index + 1 == total){
            txtViewEnd.setVisibility(View.VISIBLE);
            imgViewNext.setVisibility(View.GONE);
            setOnEndListener();
        }
        else{
            txtViewEnd.setVisibility(View.GONE);
            imgViewNext.setVisibility(View.VISIBLE);
            setNextListener(index);
        }

        return rootView;
    }

    private void setNextListener(final int currentIndex){
        imgViewNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (viewPagerListener != null){
                    viewPagerListener.goToIndex(currentIndex + 1);
                }
            }
        });
    }

    private void setOnEndListener(){
        imgViewMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (viewPagerListener != null){
                    viewPagerListener.onEnded();
                }
            }
        });

        txtViewEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (viewPagerListener != null){
                    viewPagerListener.onEnded();
                }
            }
        });

    }

    public void setViewPagerListener(ViewPagerListener viewPagerListener) {
        this.viewPagerListener = viewPagerListener;
    }
}
