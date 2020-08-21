package com.snail.collie.mem;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
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
import java.util.WeakHashMap;

public class MemoryLeakTrack implements ITracker {

    private HashSet<WeakReference<Activity>> weakReferences = new HashSet<>();
    private ReferenceQueue<Activity> mReferenceQueue = new ReferenceQueue<>();
    private Handler mHandler = new Handler(CollieHandlerThread.getInstance().getHandlerThread().getLooper());
    private SimpleActivityLifecycleCallbacks mSimpleActivityLifecycleCallbacks = new SimpleActivityLifecycleCallbacks() {
        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
            super.onActivityDestroyed(activity);
            weakReferences.add(new WeakReference<>(activity, mReferenceQueue));
        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {
            super.onActivityStopped(activity);
            if (ActivityStack.getInstance().isInBackGround()) {
                System.gc();

                for (WeakReference<Activity> item : weakReferences) {
                    Log.v("MemoryLeakTrack", "" + item.get());
                }

//                mHandler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        Log.v("MemoryLeakTrack", "start");
//                        try {
//                            Reference k;
//                            while ((k = mReferenceQueue.remove()) != null) {
//                                weakReferences.remove(k);
//                            }
//
//                            for (WeakReference<Activity> item : weakReferences) {
//                                Log.v("MemoryLeakTrack", "" + item.get());
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//
//
//                    }
//                }, 1000);
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
