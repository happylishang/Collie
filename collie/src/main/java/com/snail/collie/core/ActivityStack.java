package com.snail.collie.core;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

 public class ActivityStack {

    private static volatile ActivityStack sInstance = null;

    private List<Activity> mActivities = new ArrayList<>();

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

    public Activity getTopActivity() {

        return mActivities.size() > 0 ? mActivities.get(0) : null;
    }

     public Activity getBottomActivity() {

         return mActivities.size() > 0 ? mActivities.get(mActivities.size() - 1) : null;
     }
}
