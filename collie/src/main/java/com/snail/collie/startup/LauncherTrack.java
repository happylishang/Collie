package com.snail.collie.startup;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.snail.collie.Collie;
import com.snail.collie.battery.BatteryStatsTrack;
import com.snail.collie.core.ActivityStack;
import com.snail.collie.core.CollieHandlerThread;
import com.snail.collie.core.ITracker;
import com.snail.collie.core.SimpleActivityLifecycleCallbacks;
import com.snail.collie.debug.DebugHelper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LauncherTrack implements ITracker {

    private static LauncherTrack sInstance;
    private Handler mHandler;

    private LauncherTrack() {
        mHandler = new Handler(CollieHandlerThread.getInstance().getHandlerThread().getLooper());
    }

    public static LauncherTrack getInstance() {
        if (sInstance == null) {
            synchronized (DebugHelper.class) {
                if (sInstance == null) {
                    sInstance = new LauncherTrack();
                }
            }
        }
        return sInstance;
    }

    private int launcherFlag;
    private static int createFlag = 1;
    private static int resumeFlag = 1 << 1;
    private static int startFlag = createFlag | resumeFlag;
    private long mActivityLaucherTimeStamp = LauncherHelpProvider.sStartUpTimeStamp;

    private SimpleActivityLifecycleCallbacks mSimpleActivityLifecycleCallbacks = new SimpleActivityLifecycleCallbacks() {


        @Override
        public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
            super.onActivityCreated(activity, bundle);
            launcherFlag |= createFlag;
        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {
            super.onActivityPaused(activity);
            mActivityLaucherTimeStamp = SystemClock.uptimeMillis();
            launcherFlag = 0;
        }

        //  准确来说 是在Activity的onWindowFocusChanged才可见
        @Override
        public void onActivityResumed(@NonNull final Activity activity) {
            super.onActivityResumed(activity);
            launcherFlag |= resumeFlag;
            activity.getWindow().getDecorView().getViewTreeObserver().addOnWindowFocusChangeListener(new ViewTreeObserver.OnWindowFocusChangeListener() {
                @Override
                public void onWindowFocusChanged(boolean b) {
                    if (b && (launcherFlag ^ startFlag) == 0) {
                        final boolean isColdStarUp = ActivityStack.getInstance().getBottomActivity() == activity;
                        final long coldLauncherTime = SystemClock.uptimeMillis() - LauncherHelpProvider.sStartUpTimeStamp;
                        final long activityLauncherTime = SystemClock.uptimeMillis() - mActivityLaucherTimeStamp;
                        activity.getWindow().getDecorView().getViewTreeObserver().removeOnWindowFocusChangeListener(this);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (isColdStarUp) {
                                    for (ILaucherTrackListener laucherTrackListener : mILaucherTrackListenerSet) {
                                        laucherTrackListener.onColdLaucherCost(coldLauncherTime);
                                    }
                                }
                                for (ILaucherTrackListener laucherTrackListener : mILaucherTrackListenerSet) {
                                    laucherTrackListener.onActivityStartCost(activity, activityLauncherTime);
                                }

                            }
                        });
                    }
                }
            });
        }

    };


    @Override
    public void destroy(Application application) {
        Collie.getInstance().removeActivityLifecycleCallbacks(mSimpleActivityLifecycleCallbacks);
    }

    @Override
    public void startTrack(Application application) {
        Collie.getInstance().addActivityLifecycleCallbacks(mSimpleActivityLifecycleCallbacks);
    }

    @Override
    public void pauseTrack(Application application) {

    }

    private Set<ILaucherTrackListener> mILaucherTrackListenerSet = new HashSet<>();

    public void addILaucherTrackListener(ILaucherTrackListener laucherTrackListener) {
        mILaucherTrackListenerSet.add(laucherTrackListener);
    }

    public void removeILaucherTrackListener(ILaucherTrackListener laucherTrackListener) {
        mILaucherTrackListenerSet.remove(laucherTrackListener);
    }

    public interface ILaucherTrackListener {

        void onColdLaucherCost(long duration);

        void onActivityStartCost(Activity activity, long duration);
    }
}
