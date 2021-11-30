package com.snail.collie.startup;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.snail.collie.Collie;
import com.snail.collie.core.CollieHandlerThread;
import com.snail.collie.core.ITracker;
import com.snail.collie.core.ProcessUtil;
import com.snail.collie.core.SimpleActivityLifecycleCallbacks;
import com.snail.kotlin.core.ActivityStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;


public class LauncherTracker implements ITracker {

    private static LauncherTracker sInstance;
    private final Handler mHandler;
    private boolean markCodeStartUp;

    private LauncherTracker() {
        mHandler = new Handler(CollieHandlerThread.getInstance().getHandlerThread().getLooper());
    }

    public static LauncherTracker getInstance() {
        if (sInstance == null) {
            synchronized (LauncherTracker.class) {
                if (sInstance == null) {
                    sInstance = new LauncherTracker();
                }
            }
        }
        return sInstance;
    }

    private int launcherFlag;
    private static int createFlag = 1;
    private static int resumeFlag = 1 << 1;
    private static int startFlag = createFlag | resumeFlag;
    private long mActivityLauncherTimeStamp;
    private Handler mUIHandler = new Handler(Looper.getMainLooper());
    private SimpleActivityLifecycleCallbacks mSimpleActivityLifecycleCallbacks = new SimpleActivityLifecycleCallbacks() {

        @Override
        public void onActivityCreated(@NonNull final Activity activity, @Nullable Bundle bundle) {
            if (mActivityLauncherTimeStamp == 0 ||
                    ActivityStack.INSTANCE.getBottomActivity() == null
                    || ActivityStack.INSTANCE.getBottomActivity() == activity) {
                mActivityLauncherTimeStamp = SystemClock.uptimeMillis();
            }
            super.onActivityCreated(activity, bundle);
            launcherFlag |= createFlag;
            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (activity.isFinishing()) {
                        if ((launcherFlag ^ createFlag) == 0) {
                            collectInfo(activity);
                            launcherFlag = 0;
                            mActivityLauncherTimeStamp = SystemClock.uptimeMillis();
                        }
                    }
                }
            });
        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {
            super.onActivityStopped(activity);
            if (ActivityStack.INSTANCE.isInBackGround()) {
                mActivityLauncherTimeStamp = 0;
            }
        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {
            super.onActivityPaused(activity);
            mActivityLauncherTimeStamp = SystemClock.uptimeMillis();
            // 闪屏可能存在不调用resume的场景 onCreate中国直接finish
            launcherFlag = 0;
        }

        //  准确来说 是在Activity的onWindowFocusChanged才可见
        //   但是这个界面可能不会显示 onCreate中直接finish的话就会
        @Override
        public void onActivityResumed(@NonNull final Activity activity) {
            super.onActivityResumed(activity);
            if (launcherFlag != 0 && (launcherFlag & resumeFlag) == 0) {
                launcherFlag |= resumeFlag;
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                    //  P之后有改动，第一帧可见提前了 可以认为onActivityResumed之后
                    mUIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            //  执行完resume才可见，这里还没执行完
                            collectInfo(activity);
                        }
                    });
                } else {
                    final ViewTreeObserver.OnWindowFocusChangeListener listener = new ViewTreeObserver.OnWindowFocusChangeListener() {

                        /**
                         * Called when the current {@link Window } of the activity gains or loses* focus.  This is the best indicator of whether this activity is visible
                         * to the user.  The default implementation clears the key tracking
                         * state, so should always be called.
                         */
                        @Override
                        public void onWindowFocusChanged(boolean b) {
                            // 可能resume立刻finish
                            collectInfo(activity);
                            activity.getWindow().getDecorView().getViewTreeObserver().removeOnWindowFocusChangeListener(this);
                        }
                    };
                    activity.getWindow().getDecorView().getViewTreeObserver().addOnWindowFocusChangeListener(listener);
                    //   如果finish超前执行，那就在下一个消息是计算
                    mUIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (activity.isFinishing()) {
                                collectInfo(activity);
                                activity.getWindow().getDecorView().getViewTreeObserver().removeOnWindowFocusChangeListener(listener);

                            }
                        }
                    });
                }

            }
        }

    };

    private void collectInfo(final Activity activity) {
        final long coldLauncherTime = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ?
                SystemClock.uptimeMillis() - Process.getStartUptimeMillis() :
                SystemClock.uptimeMillis() - LauncherHelpProvider.sStartUpTimeStamp;
        final long activityLauncherTime = SystemClock.uptimeMillis() - mActivityLauncherTimeStamp;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!markCodeStartUp) {
                    if (isForegroundProcess(activity)) {
                        for (ILaunchTrackListener launcherTrackListener : mILaucherTrackListenerSet) {
                            launcherTrackListener.onAppColdLaunchCost(coldLauncherTime, ProcessUtil.getProcessName());
                        }
                    }
                    markCodeStartUp = true;
                }
                for (ILaunchTrackListener launcherTrackListener : mILaucherTrackListenerSet) {
                    launcherTrackListener.onActivityLaunchCost(activity, activityLauncherTime, activity.isFinishing());
                }

            }
        });
    }

    @Override
    public void destroy(Application application) {
        Collie.getInstance().removeActivityLifecycleCallbacks(mSimpleActivityLifecycleCallbacks);
    }

    @Override
    public void startTrack(Application application) {
        Collie.getInstance().addActivityLifecycleCallbacks(mSimpleActivityLifecycleCallbacks);
        Log.v("Collie", "mIsColdStarUp " + markCodeStartUp);
    }

    @Override
    public void pauseTrack(Application application) {

    }

    private Set<ILaunchTrackListener> mILaucherTrackListenerSet = new HashSet<>();

    public void addLaunchTrackListener(ILaunchTrackListener laucherTrackListener) {
        mILaucherTrackListenerSet.add(laucherTrackListener);
    }

    public void removeLaunchTrackListener(ILaunchTrackListener laucherTrackListener) {
        mILaucherTrackListenerSet.remove(laucherTrackListener);
    }

    public interface ILaunchTrackListener {

        void onAppColdLaunchCost(long duration, String procName);

        void onActivityLaunchCost(Activity activity, long duration, boolean finishNow);
    }


    //  判断进程启动的时候是否是前台进程
    public static boolean isForegroundProcess(Context application) {
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = ((ActivityManager) application.getSystemService(Context.ACTIVITY_SERVICE)).getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : runningAppProcesses) {
            if (Process.myPid() == info.pid) {
                return info.importance == IMPORTANCE_FOREGROUND;
            }
        }
        return false;
    }
}
