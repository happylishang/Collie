package com.snail.collie.mem;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.snail.collie.BuildConfig;
import com.snail.collie.Collie;
import com.snail.collie.core.ActivityStack;
import com.snail.collie.core.CollieHandlerThread;
import com.snail.collie.core.ITracker;
import com.snail.collie.core.SimpleActivityLifecycleCallbacks;
import com.snail.collie.debug.DebugHelper;
import com.snail.collie.fps.FpsTracker;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Map;
import java.util.WeakHashMap;

public class MemoryLeakTrack implements ITracker {

    private static volatile MemoryLeakTrack sInstance = null;

    private MemoryLeakTrack() {
    }

    public static MemoryLeakTrack getInstance() {
        if (sInstance == null) {
            synchronized (MemoryLeakTrack.class) {
                if (sInstance == null) {
                    sInstance = new MemoryLeakTrack();
                }
            }
        }
        return sInstance;
    }

    private Handler mHandler = new Handler(CollieHandlerThread.getInstance().getHandlerThread().getLooper());
    private WeakHashMap<Activity, String> mActivityStringWeakHashMap = new WeakHashMap<>();
    private SimpleActivityLifecycleCallbacks mSimpleActivityLifecycleCallbacks = new SimpleActivityLifecycleCallbacks() {
        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
            super.onActivityDestroyed(activity);
            mActivityStringWeakHashMap.put(activity, activity.getClass().getSimpleName());
        }

        @Override
        public void onActivityStopped(@NonNull final Activity activity) {
            super.onActivityStopped(activity);
            //  退后台，GC 找LeakActivity
            if (ActivityStack.getInstance().isInBackGround()) {
                return;
            }
            Runtime.getRuntime().gc();
            SystemClock.sleep(100);
            System.runFinalization();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (!ActivityStack.getInstance().isInBackGround()) {
                            return;
                        }
                        for (Map.Entry<Activity, String> activityStringEntry : mActivityStringWeakHashMap.entrySet()) {
                            Log.v("MemoryLeakTrack", "" + activityStringEntry.getKey());
                        }
                    } catch (Exception ignored) {
                    }
                }
            }, 5000);
        }
    };

    @Override
    public void destroy() {
        Collie.getInstance().removeActivityLifecycleCallbacks(mSimpleActivityLifecycleCallbacks);
    }

    @Override
    public void startTrack() {
        Collie.getInstance().addActivityLifecycleCallbacks(mSimpleActivityLifecycleCallbacks);
    }

    @Override
    public void pauseTrack() {

    }

    public interface OnMemoryLeakListener {

    }
}
