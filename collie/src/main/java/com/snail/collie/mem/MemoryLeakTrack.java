package com.snail.collie.mem;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Debug;
import android.os.Handler;
import android.os.Process;
import android.os.SystemClock;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.snail.collie.Collie;
import com.snail.collie.core.ActivityStack;
import com.snail.collie.core.CollieHandlerThread;
import com.snail.collie.core.ITracker;
import com.snail.collie.core.SimpleActivityLifecycleCallbacks;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class MemoryLeakTrack implements ITracker {

    private static volatile MemoryLeakTrack sInstance = null;
    private long mStep;
    private static int M = 1024 * 1024;

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
            if (mStep++ % 3 == 0) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        collectMemoryInfo(activity.getApplication());
                    }
                });
            }
            //  退后台，GC 找LeakActivity
            if (ActivityStack.getInstance().isInBackGround()) {
                return;
            }
            Runtime.getRuntime().gc();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (!ActivityStack.getInstance().isInBackGround()) {
                            return;
                        }
                        SystemClock.sleep(100);
                        System.runFinalization();
                        HashMap<String, Integer> hashMap = new HashMap<>();
                        for (Map.Entry<Activity, String> activityStringEntry : mActivityStringWeakHashMap.entrySet()) {
                            String name = activityStringEntry.getKey().getClass().getName();
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

    private static String display;

    private TrackMemoryInfo collectMemoryInfo(Application application) {

        if (TextUtils.isEmpty(display)) {
            display = "" + application.getResources().getDisplayMetrics().widthPixels + "*" + application.getResources().getDisplayMetrics().heightPixels;
        }
        ActivityManager activityManager = (ActivityManager) application.getSystemService(Context.ACTIVITY_SERVICE);
        // 系统内存
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        SystemMemory systemMemory = new SystemMemory();
        systemMemory.availMem = memoryInfo.availMem >> 20;
        systemMemory.totalMem = memoryInfo.totalMem >> 20;
        systemMemory.lowMemory = memoryInfo.lowMemory;
        systemMemory.threshold = memoryInfo.threshold >> 20;

        //java内存
        Runtime rt = Runtime.getRuntime();
        JavaMemory javaMemory = new JavaMemory();
        javaMemory.freeMemory = rt.freeMemory() >> 20;
        javaMemory.maxMemory = rt.maxMemory() >> 20;
        javaMemory.totalMemory = rt.totalMemory() >> 20;

        //进程Native内存

        NativeMemory nativeMemory = new NativeMemory();
        Debug.MemoryInfo debugMemoryInfo = new Debug.MemoryInfo();
        Debug.getMemoryInfo(debugMemoryInfo);

        nativeMemory.nativeHeapAllocatedSize = Debug.getNativeHeapAllocatedSize() >> 20;
        nativeMemory.nativeHeapFreeSize = Debug.getNativeHeapFreeSize() >> 20;
        nativeMemory.nativeHeapSize = Debug.getNativeHeapSize() >> 20;

        TrackMemoryInfo trackMemoryInfo = new TrackMemoryInfo();
        trackMemoryInfo.javaMemory = javaMemory;
        trackMemoryInfo.systemMemoryInfo = systemMemory;
        trackMemoryInfo.nativeMemory = nativeMemory;

        trackMemoryInfo.procName = getProcessName(Process.myPid());
        trackMemoryInfo.display = display;
        return trackMemoryInfo;
    }


    private static String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }

}
