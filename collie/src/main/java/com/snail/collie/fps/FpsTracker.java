package com.snail.collie.fps;

import android.app.Activity;
import android.app.Application;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Choreographer;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;

import com.snail.collie.Collie;
import com.snail.collie.core.ActivityStack;
import com.snail.collie.core.CollieHandlerThread;
import com.snail.collie.core.ITracker;
import com.snail.collie.core.LooperMonitor;
import com.snail.collie.core.SimpleActivityLifecycleCallbacks;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.concurrent.LinkedBlockingQueue;

public class FpsTracker extends LooperMonitor.LooperDispatchListener implements ITracker {

    private ITrackFpsListener mITrackListener;
    private Handler mHandler;
    private Handler mANRHandler;
    private long mStartTime;
    private static FpsTracker sInstance = null;
    private CollectItem mCollectItem;
    private FpsThread mFpsThread;
    private LinkedBlockingQueue<Runnable> mLinkedBlockingQueue = new LinkedBlockingQueue<>();

    public synchronized void setTrackerListener(ITrackFpsListener listener) {
        mITrackListener = listener;
    }

    private FpsTracker() {
        mHandler = new Handler(CollieHandlerThread.getInstance().getHandlerThread().getLooper());
        mANRHandler = new Handler(CollieHandlerThread.getInstance().getHandlerThread().getLooper());
        mCollectItem = new CollectItem();
        mFpsThread = new FpsThread(mLinkedBlockingQueue);
        mFpsThread.start();
    }

    public static FpsTracker getInstance() {
        if (sInstance == null) {
            synchronized (FpsTracker.class) {
                if (sInstance == null) {
                    sInstance = new FpsTracker();
                }
            }
        }
        return sInstance;
    }

    private Object callbackQueueLock;
    private Object[] callbackQueues;
    private Method addTraversalQueue;
    private Method addInputQueue;
    private Method addAnimationQueue;
    private Choreographer choreographer;
    private long frameIntervalNanos = 16666666;
    public static final int CALLBACK_INPUT = 0;
    public static final int CALLBACK_ANIMATION = 1;
    public static final int CALLBACK_TRAVERSAL = 2;
    private static final String ADD_CALLBACK = "addCallbackLocked";
    private boolean mInDoFrame = false;
    private ANRMonitorRunnable mANRMonitorRunnable;
    private SimpleActivityLifecycleCallbacks mSimpleActivityLifecycleCallbacks = new SimpleActivityLifecycleCallbacks() {


        @Override
        public void onActivityPaused(@NonNull Activity activity) {
            super.onActivityPaused(activity);
            pauseTrack(activity.getApplication());
        }

        // 帧率不统计第一帧
        @Override
        public void onActivityResumed(@NonNull final Activity activity) {
            super.onActivityResumed(activity);
            activity.getWindow().getDecorView().getViewTreeObserver().addOnWindowFocusChangeListener(new ViewTreeObserver.OnWindowFocusChangeListener() {
                /**
                 * Called when the current {@link Window } of the activity gains or loses* focus.  This is the best indicator of whether this activity is visible
                 * to the user.  The default implementation clears the key tracking
                 * state, so should always be called.
                 */
                @Override
                public void onWindowFocusChanged(boolean b) {
                    if (b) {
                        resumeTrack();
                        activity.getWindow().getDecorView().getViewTreeObserver().removeOnWindowFocusChangeListener(this);
                    }
                }
            });
        }
    };


    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void dispatchStart() {
        super.dispatchStart();
        mStartTime = SystemClock.uptimeMillis();
        if (mANRMonitorRunnable == null) {
            mANRMonitorRunnable = new ANRMonitorRunnable(new WeakReference<Activity>(ActivityStack.getInstance().getTopActivity())) {
                @Override
                public void run() {
                    if (this.activityRef != null && this.activityRef.get() != null && !this.invalid) {
                        synchronized (FpsTracker.this) {
                            if (mITrackListener != null) {
                                mITrackListener.onANRAppear(this.activityRef.get());
                            }
                        }
                    }
                }
            };
        } else {
            mANRMonitorRunnable.activityRef = new WeakReference<Activity>(ActivityStack.getInstance().getTopActivity());
        }
        mANRMonitorRunnable.invalid = false;
        mLinkedBlockingQueue.add(new Runnable() {
            @Override
            public void run() {
                mANRHandler.removeCallbacksAndMessages(null);
                mANRHandler.postDelayed(mANRMonitorRunnable, 5000);
            }
        });
    }

    /**
     * Message 内部移除后 ，looper找不到mStartTime归零防止误判
     */

    @Override
    public void dispatchEnd() {
        super.dispatchEnd();
        mANRMonitorRunnable.invalid = true;
        if (mStartTime > 0) {
            final long cost = SystemClock.uptimeMillis() - mStartTime;
            final boolean isDoFrame = mInDoFrame;
            mLinkedBlockingQueue.add(new Runnable() {
                @Override
                public void run() {
                    collectInfoAndDispatch(ActivityStack.getInstance().getTopActivity(), cost, isDoFrame);
                }
            });
            if (mInDoFrame) {
                addFrameCallBack();
                mInDoFrame = false;
            }
        }
    }


    private void addFrameCallBack() {
        addFrameCallback(CALLBACK_INPUT, new Runnable() {
            @Override
            public void run() {
                mInDoFrame = true;
            }
        }, true);
    }

    private void collectInfoAndDispatch(final Activity activity, final long cost, final boolean inDoFrame) {
        //  不记录正常帧帧率
        if (cost <= 16) {
            mCollectItem.sumCost++;
            mCollectItem.sumCost += Math.max(16, cost);
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (FpsTracker.this) {
                    if (activity != null) {
                        mCollectItem.activity = activity;
                        mCollectItem.sumCost += Math.max(16, cost);
                        mCollectItem.sumFrame++;
                        if (mCollectItem.sumFrame > 3) {
                            long averageFps = Math.min(60, mCollectItem.sumCost > 0 ? mCollectItem.sumFrame * 1000 / mCollectItem.sumCost : 60);
                            if (mITrackListener != null) {
                                mITrackListener.onFpsTrack(activity, cost, Math.max(1, cost / 16 - 1), inDoFrame, averageFps);
                            }
                        }
                        //   不过度累积
                        if (mCollectItem.sumFrame > 60) {
                            resetCollectItem();
                        }
                    }
                }
            }
        });
    }


    public long getFrameIntervalNanos() {
        return frameIntervalNanos;
    }


    private Method reflectChoreographerMethod(Object instance, String name, Class<?>... argTypes) {
        try {
            Method method = instance.getClass().getDeclaredMethod(name, argTypes);
            method.setAccessible(true);
            return method;
        } catch (Exception e) {

        }
        return null;
    }

    private <T> T reflectObject(Object instance, String name) {
        try {
            Field field = instance.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return (T) field.get(instance);
        } catch (Exception e) {
            e.printStackTrace();

        }
        return null;
    }


    /**
     * 监测不同的阶段  Input 、动画、布局
     * 简化处理FPS的时候， 没必要区分的这么细
     **/
    private synchronized void addFrameCallback(int type, Runnable callback, boolean isAddHeader) {

        try {
            synchronized (callbackQueueLock) {
                Method method = null;
                switch (type) {
                    case CALLBACK_INPUT:
                        method = addInputQueue;
                        break;
                    case CALLBACK_ANIMATION:
                        method = addAnimationQueue;
                        break;
                    case CALLBACK_TRAVERSAL:
                        method = addTraversalQueue;
                        break;
                }
                if (null != method) {
                    method.invoke(callbackQueues[type], !isAddHeader ? SystemClock.uptimeMillis() : -1, callback, null);
                }
            }
        } catch (Exception ignored) {

        }
    }

    @Override
    public void destroy(Application application) {
        sInstance = null;
    }


    @Override
    public void startTrack(Application application) {
        Collie.getInstance().addActivityLifecycleCallbacks(mSimpleActivityLifecycleCallbacks);
    }

    private void resumeTrack() {
        if (choreographer == null) {
            choreographer = Choreographer.getInstance();
            callbackQueueLock = reflectObject(choreographer, "mLock");
            callbackQueues = reflectObject(choreographer, "mCallbackQueues");
            frameIntervalNanos = reflectObject(choreographer, "mFrameIntervalNanos");
            addInputQueue = reflectChoreographerMethod(callbackQueues[CALLBACK_INPUT], ADD_CALLBACK, long.class, Object.class, Object.class);
            addAnimationQueue = reflectChoreographerMethod(callbackQueues[CALLBACK_ANIMATION], ADD_CALLBACK, long.class, Object.class, Object.class);
            addTraversalQueue = reflectChoreographerMethod(callbackQueues[CALLBACK_TRAVERSAL], ADD_CALLBACK, long.class, Object.class, Object.class);
        }
        LooperMonitor.register(this);
        addFrameCallBack();
    }

    @Override
    public void pauseTrack(Application application) {
        LooperMonitor.unregister(this);
        mHandler.removeCallbacksAndMessages(null);
        mANRHandler.removeCallbacksAndMessages(null);
        try {
            mLinkedBlockingQueue.clear();
        } catch (Exception ignored) {
        }
        resetCollectItem();
        mStartTime = 0;
    }

    private void resetCollectItem() {
        mCollectItem.sumCost = 0;
        mCollectItem.sumFrame = 0;
        mCollectItem.activity = null;
    }
}
