package com.ffinder.android.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;
import com.ffinder.android.R;
import com.ffinder.android.extensions.CustomAnimationDrawable;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by sionglengho on 27/10/16.
 */
public class SearchAnimationPool {

    private static HashMap<String, ArrayList<Drawable>> storage;

    public static ArrayList<Drawable> getDrawables(Context context, String name, int totalFrames){
//        if(!getStorage().containsKey(name)){
//
//            getStorage().put(name, arr);
//        }
//
//        return getStorage().get(name);

        ArrayList<Drawable> arr = new ArrayList();
        for (int i = 0; i < totalFrames; i++) {
            String id = name + String.format("%02d", i);
            int resID = context.getResources().getIdentifier(id, "drawable", context.getPackageName());
            arr.add(ContextCompat.getDrawable(context, resID));
        }
        return arr;

    }



    public static HashMap<String, ArrayList<Drawable>> getStorage() {
        if(storage == null) storage = new HashMap();
        return storage;
    }
}






