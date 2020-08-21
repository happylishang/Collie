package com.snail.collie.mem;

import android.app.Activity;
import android.os.Handler;
import android.os.SystemClock;

import androidx.annotation.NonNull;

import com.snail.collie.Collie;
import com.snail.collie.core.ActivityStack;
import com.snail.collie.core.CollieHandlerThread;
import com.snail.collie.core.ITracker;
import com.snail.collie.core.SimpleActivityLifecycleCallbacks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
                        HashMap<String, Integer> hashMap = new HashMap<>();
                        for (Map.Entry<Activity, String> activityStringEntry : mActivityStringWeakHashMap.entrySet()) {
                            String name=activityStringEntry.getKey().getClass().getName();
                            Integer value = hashMap.get(name);
                            if (value == null) {
                                hashMap.put(name, 1);
                            } else {
                                hashMap.put(name, value + 1);
                            }
                        }
                        for (Map.Entry<String, Integer> entry : hashMap.entrySet()) {
                            if (mMemoryLeakListeners.size() > 0) {
                                for (ITrackMemoryLeakListener listener : mMemoryLeakListeners) {
                                    listener.onLeakActivity(entry.getKey(), entry.getValue());
                                }
                            }
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

    private Set<ITrackMemoryLeakListener> mMemoryLeakListeners = new HashSet<>();

    public void addOnMemoryLeakListener(ITrackMemoryLeakListener leakListener) {
        mMemoryLeakListeners.add(leakListener);
    }

    public void removeOnMemoryLeakListener(ITrackMemoryLeakListener leakListener) {
        mMemoryLeakListeners.remove(leakListener);
    }

    public interface ITrackMemoryLeakListener {

        void onLeakActivity(String activity, int count);
    }


}
