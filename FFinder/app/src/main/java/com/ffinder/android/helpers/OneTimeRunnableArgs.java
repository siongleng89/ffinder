package com.ffinder.android.helpers;

/**
 * Created by SiongLeng on 2/9/2016.
 */
public abstract class OneTimeRunnableArgs<T> implements Runnable  {

    T[] m_args;
    boolean runBefore;

    public OneTimeRunnableArgs() {
    }

    public void run(T... args) {
        if(!runBefore){
            runBefore = true;
            setArgs(args);
            run();
        }
    }

    public void setArgs(T... args) {
        m_args = args;
    }

    public int getArgCount() {
        return m_args == null ? 0 : m_args.length;
    }

    public T[] getArgs() {
        return m_args;
    }

    public T getFirstArg() {
        if(getArgCount() > 0){
            return getArgs()[0];
        }
        return null;
    }

}
