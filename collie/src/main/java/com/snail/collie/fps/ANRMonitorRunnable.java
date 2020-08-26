package com.snail.collie.fps;

import android.app.Activity;

import java.lang.ref.WeakReference;

public abstract class ANRMonitorRunnable implements Runnable {

    public WeakReference<Activity> getActivityRef() {
        return mActivityRef;
    }

    private WeakReference<Activity> mActivityRef;

    public ANRMonitorRunnable(WeakReference<Activity> mActivityRef) {
        this.mActivityRef = mActivityRef;
    }

}
