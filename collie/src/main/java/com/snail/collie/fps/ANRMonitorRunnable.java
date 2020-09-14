package com.snail.collie.fps;

import android.app.Activity;

import java.lang.ref.WeakReference;

public abstract class ANRMonitorRunnable implements Runnable {


    public WeakReference<Activity> activityRef;
    public boolean invalid;

    public ANRMonitorRunnable(WeakReference<Activity> mActivityRef) {
        this.activityRef = mActivityRef;
        this.invalid = false;
    }

}
