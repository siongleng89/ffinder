package com.ffinder.android.extensions;

import java.util.ArrayList;

/**
 * Created by SiongLeng on 27/9/2016.
 */
public class LimitedArrayList<S> extends ArrayList {

    private int limit = 5;

    public LimitedArrayList() {
    }

    public LimitedArrayList(int limit) {
        this.limit = limit;
    }

    @Override
    public boolean add(Object o) {

        while (size() + 1 > limit){
            this.remove(0);
        }

        return super.add(o);
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
