package com.ffinder.android.absint.helpers;

import com.ffinder.android.enums.Status;

/**
 * Created by SiongLeng on 8/9/2016.
 */
public abstract class RestfulListener<T> {

    public abstract void onResult(T result, Status status);

}
