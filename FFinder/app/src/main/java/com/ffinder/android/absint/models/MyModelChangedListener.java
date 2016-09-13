package com.ffinder.android.absint.models;

import com.ffinder.android.models.MyModel;

/**
 * Created by SiongLeng on 1/9/2016.
 */
public abstract class MyModelChangedListener {

    public abstract void onChanged(MyModel newMyModel, String changedProperty);

}
