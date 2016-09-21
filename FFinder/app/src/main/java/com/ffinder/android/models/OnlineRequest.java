package com.ffinder.android.models;

import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

/**
 * Created by SiongLeng on 19/9/2016.
 */
public class OnlineRequest {
    private String reason;
    private StackTraceElement[] stackTrace;
    private ValueEventListener valueEventListener;

    public OnlineRequest(String reason) {
        stackTrace = Thread.currentThread().getStackTrace();
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "OnlineRequest{" + "stackTrace=" + Arrays.toString(stackTrace) + " reason="+reason+'}';
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public StackTraceElement[] getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(StackTraceElement[] stackTrace) {
        this.stackTrace = stackTrace;
    }

    public ValueEventListener getValueEventListener() {
        return valueEventListener;
    }

    public void setValueEventListener(ValueEventListener valueEventListener) {
        this.valueEventListener = valueEventListener;
    }
}