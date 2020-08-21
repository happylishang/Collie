package com.snail.collie.core;

import android.app.Activity;
import android.app.Application;

import java.util.ArrayList;
import java.util.List;

public class ActivityStack {

    private static volatile ActivityStack sInstance = null;

    private List<Activity> mActivities = new ArrayList<>();
    private boolean isInBackGround;

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
        isInBackGround = false;
    }

    public void markInBackGround() {
        isInBackGround = getTopActivity() == getBottomActivity();
    }

    public Activity getTopActivity() {

        return mActivities.size() > 0 ? mActivities.get(0) : null;
    }

    public Activity getBottomActivity() {

        return mActivities.size() > 0 ? mActivities.get(mActivities.size() - 1) : null;
    }

    public boolean isInBackGround() {
        return isInBackGround;
    }
}
