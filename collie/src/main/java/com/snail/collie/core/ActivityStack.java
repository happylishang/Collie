package com.snail.collie.core;

import android.app.Activity;
import android.app.Application;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ActivityStack {

    private static volatile ActivityStack sInstance = null;
    private List<Activity> mActivities = new ArrayList<>();
    private int mCurrentSate;

    private ActivityStack() {
    }

    public static ActivityStack getInstance() {
        if (sInstance == null) {
            synchronized (ActivityStack.class) {
                if (sInstance == null) {
                    sInstance = new ActivityStack();
                }
            }
        }
        return sInstance;
    }

    public void push(Activity activity) {
        mActivities.add(0, activity);
    }

    public void pop(Activity activity) {
        mActivities.remove(activity);
    }

    public void markResume() {
        mCurrentSate++;
    }

    public void markStop() {
        mCurrentSate--;
    }

    public Activity getTopActivity() {

        return mActivities.size() > 0 ? mActivities.get(0) : null;
    }

    public boolean isInBackGround() {
        return mCurrentSate == 0;
    }
}
