package com.ffinder.android.absint.databases;

import android.support.annotation.NonNull;
import com.fasterxml.jackson.core.type.TypeReference;
import com.ffinder.android.enums.Status;
import com.google.firebase.database.DatabaseError;

/**
 * Created by SiongLeng on 30/8/2016.
 */
public abstract class FirebaseListener<T> {

    private Class type;

    public FirebaseListener(Class type) {
        this.type = type;
    }

    public FirebaseListener() {
    }

    public abstract void onResult(T result, Status status);

    public void onResult(T result, DatabaseError databaseError){
        if(databaseError != null){
            onResult(result, Status.Failed);
        }
        else{
            onResult(result, Status.Success);
        }
    }

    public void onResult(DatabaseError databaseError){
        if(databaseError != null){
            onResult(null, Status.Failed);
        }
        else{
            onResult(null, Status.Success);
        }
    }

    public Class getType() {
        return type;
    }
}
