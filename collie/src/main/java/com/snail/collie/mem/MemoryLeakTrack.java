package com.snail.collie.mem;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.snail.collie.BuildConfig;
import com.snail.collie.core.ActivityStack;
import com.snail.collie.core.ITracker;
import com.snail.collie.core.SimpleActivityLifecycleCallbacks;
import com.snail.collie.debug.DebugHelper;
import com.snail.collie.fps.FpsTracker;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

public class MemoryLeakTrack implements ITracker {


    private ReferenceQueue<Activity> mReferenceQueue = new ReferenceQueue<>();

    private SimpleActivityLifecycleCallbacks mSimpleActivityLifecycleCallbacks = new SimpleActivityLifecycleCallbacks() {
        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
            super.onActivityDestroyed(activity);
            new WeakReference<>(activity, mReferenceQueue);

        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {
            super.onActivityStopped(activity);
        }
    };

    @Override
    public void destroy() {

    }

    @Override
    public void startTrack() {

    }

    @Override
    public void pauseTrack() {

    }

    private void findLeakActivity() {

    }
}
