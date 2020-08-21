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

    private MemoryLeakTrack(){}

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
    private HashSet<WeakReference<Activity>> weakReferences = new HashSet<>();
    private ReferenceQueue<Activity> mReferenceQueue = new ReferenceQueue<>();
    private Handler mHandler = new Handler(CollieHandlerThread.getInstance().getHandlerThread().getLooper());
    private WeakHashMap<Activity ,String> mActivityStringWeakHashMap=new WeakHashMap<>();
    private SimpleActivityLifecycleCallbacks mSimpleActivityLifecycleCallbacks = new SimpleActivityLifecycleCallbacks() {
        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
            super.onActivityDestroyed(activity);
            weakReferences.add(new WeakReference<>(activity, mReferenceQueue));
            mActivityStringWeakHashMap.put(activity,activity.getClass().getSimpleName());
        }

        @Override
        public void onActivityStopped(@NonNull final Activity activity) {
            super.onActivityStopped(activity);
            if (ActivityStack.getInstance().isInBackGround()) {
                Runtime.getRuntime().gc();
                System.gc();
                SystemClock.sleep(100);
                System.runFinalization();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.v("MemoryLeakTrack", "start");
                        try {
//                            Reference k;
//                            while ((k = mReferenceQueue.poll()) != null) {
//                                weakReferences.remove(k);
//                            }
//                            for (WeakReference<Activity> item : weakReferences) {
//                                Log.v("MemoryLeakTrack", "" + (item.get()==null?"d":item.get()));
//                            }

//                            WeakHashMap<Activity ,String>


                            for (Map.Entry<Activity ,String> activityStringEntry : mActivityStringWeakHashMap.entrySet()) {
                                Log.v("MemoryLeakTrack", "" +  activityStringEntry.getKey());
                            }


                        } catch (Exception e) {
                            Log.v("MemoryLeakTrack", "3");
                        }
                    }
                }, 5000);
            }
        }
    };

    @Override
    public void destroy() {

    }

    @Override
    public void startTrack() {
        Collie.getInstance().addActivityLifecycleCallbacks(mSimpleActivityLifecycleCallbacks);
    }

    @Override
    public void pauseTrack() {

    }

    private void findLeakActivity() {

    }
}
