package com.snail.kotlin

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import com.snail.kotlin.core.ActivityStack.push
import com.snail.kotlin.core.ActivityStack.markStart
import com.snail.kotlin.core.ActivityStack.markStop
import com.snail.kotlin.core.ActivityStack.pop
import com.snail.kotlin.trafficstats.TrafficStatsTracker.trafficStatsListener
import com.snail.kotlin.startup.LauncherTracker.iLaunchTrackListener
import com.snail.kotlin.fps.ITrackFpsListener
import com.snail.kotlin.mem.MemoryLeakTrack.ITrackMemoryListener
import com.snail.kotlin.battery.BatteryStatsTracker.IBatteryListener
import com.snail.kotlin.trafficstats.TrafficStatsTracker
import com.snail.kotlin.trafficstats.ITrackTrafficStatsListener
import com.snail.kotlin.mem.MemoryLeakTrack
import com.snail.kotlin.fps.FpsTracker
import com.snail.kotlin.debug.DebugHelper
import com.snail.kotlin.battery.BatteryStatsTracker
import com.snail.kotlin.startup.LauncherTracker
import com.snail.kotlin.startup.LauncherTracker.ILaunchTrackListener
import com.snail.kotlin.core.CollieHandlerThread
import kotlin.jvm.Volatile
import com.snail.kotlin.battery.BatteryInfo
import com.snail.kotlin.mem.TrackMemoryInfo
import java.util.ArrayList
import java.util.HashSet

class Collie private constructor() {
    private val mHandler: Handler = Handler(CollieHandlerThread.looper)
    private val mITrackListener: ITrackFpsListener
    private val mITrackMemoryLeakListener: ITrackMemoryListener
    private val mIBatteryListener: IBatteryListener
    private val mCollieListeners: MutableList<CollieListener> = ArrayList()
    private val mActivityLifecycleCallbacks = HashSet<Application.ActivityLifecycleCallbacks>()
    fun addActivityLifecycleCallbacks(callbacks: Application.ActivityLifecycleCallbacks) {
        mActivityLifecycleCallbacks.add(callbacks)
    }

    fun removeActivityLifecycleCallbacks(callbacks: Application.ActivityLifecycleCallbacks) {
        mActivityLifecycleCallbacks.remove(callbacks)
    }

    private val mActivityLifecycleCallback: Application.ActivityLifecycleCallbacks =
        object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
                push(activity)
                for (item in mActivityLifecycleCallbacks) {
                    item.onActivityCreated(activity, bundle)
                }
            }

            override fun onActivityStarted(activity: Activity) {
                markStart()
                for (item in mActivityLifecycleCallbacks) {
                    item.onActivityStarted(activity)
                }
            }

            override fun onActivityResumed(activity: Activity) {
                for (item in mActivityLifecycleCallbacks) {
                    item.onActivityResumed(activity)
                }
            }

            override fun onActivityPaused(activity: Activity) {
                for (item in mActivityLifecycleCallbacks) {
                    item.onActivityPaused(activity)
                }
            }

            override fun onActivityStopped(activity: Activity) {
                markStop()
                for (item in mActivityLifecycleCallbacks) {
                    item.onActivityStopped(activity)
                }
            }

            override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {
                for (item in mActivityLifecycleCallbacks) {
                    item.onActivitySaveInstanceState(activity, bundle)
                }
            }

            override fun onActivityDestroyed(activity: Activity) {
                for (item in mActivityLifecycleCallbacks) {
                    item.onActivityDestroyed(activity)
                }
                pop(activity)
            }
        }

    fun init(
        application: Application,
        config: Config,
        listener: CollieListener
    ) {
        application.registerActivityLifecycleCallbacks(mActivityLifecycleCallback)
        mCollieListeners.add(listener)
        if (config.useTrafficTrack) {
            trafficStatsListener = object : ITrackTrafficStatsListener {
                override fun onTrafficStats(activity: Activity, value: Long) {
                    for (collieListener in mCollieListeners) {
                        collieListener.onTrafficStats(activity, value)
                    }
                }
            }
            TrafficStatsTracker.startTrack(application)
        }
        if (config.useMemTrack) {
            MemoryLeakTrack.instance!!.startTrack(application)
            MemoryLeakTrack.instance!!.addOnMemoryLeakListener(mITrackMemoryLeakListener)
        }
        if (config.useFpsTrack) {
            FpsTracker.getInstance().setTrackerListener(mITrackListener)
            FpsTracker.getInstance().startTrack(application)
        }
        if (config.showDebugView) {
            DebugHelper.instance!!.startTrack(application)
        }
        if (config.useBatteryTrack) {
            BatteryStatsTracker.instance!!.addBatteryListener(mIBatteryListener)
            BatteryStatsTracker.instance!!.startTrack(application)
        }
        if (config.useStartUpTrack) {
            iLaunchTrackListener = object : ILaunchTrackListener {
                override fun onActivityFocusableCost(
                    activity: Activity?,
                    duration: Long,
                    finishNow: Boolean
                ) {
                    for (collieListener in mCollieListeners) {
                        collieListener.onActivityFocusableCost(activity, duration, finishNow)
                    }
                }

                override fun onAppColdLaunchCost(duration: Long, processName: String?) {
                    for (collieListener in mCollieListeners) {
                        collieListener.onAppColdLaunchCost(duration, processName)
                    }
                }

                override fun onActivityLaunchCost(
                    activity: Activity?,
                    duration: Long,
                    finishNow: Boolean
                ) {
                    for (collieListener in mCollieListeners) {
                        collieListener.onActivityLaunchCost(activity, duration, finishNow)
                    }
                }
            }
            LauncherTracker.startTrack(application)
        }
    }

    fun stop(application: Application) {
        application.unregisterActivityLifecycleCallbacks(mActivityLifecycleCallback)
        CollieHandlerThread.quitSafely()
    }

    companion object {
        @Volatile
        private var sInstance: Collie? = null

        @JvmStatic
        val instance: Collie
            get() {
                if (sInstance == null) {
                    synchronized(Collie::class.java) {
                        if (sInstance == null) {
                            sInstance = Collie()
                        }
                    }
                }
                return sInstance!!
            }
    }

    init {
        mITrackListener = object : ITrackFpsListener {
            override fun onFpsTrack(
                activity: Activity,
                currentCostMils: Long,
                currentDropFrame: Long,
                isInFrameDraw: Boolean,
                averageFps: Long
            ) {
                val currentFps =
                    if (currentCostMils == 0L) 60 else Math.min(60, 1000 / currentCostMils)
                mHandler.post {
                    if (currentDropFrame > 1) DebugHelper.instance!!.update(
                        """实时fps $currentFps
 丢帧 $currentDropFrame 
1s平均fps $averageFps 
本次耗时 $currentCostMils"""
                    )
                    for (collieListener in mCollieListeners) {
                        collieListener.onFpsTrack(
                            activity,
                            currentCostMils,
                            currentDropFrame,
                            isInFrameDraw,
                            averageFps
                        )
                    }
                }
            }

            override fun onANRAppear(activity: Activity?) {
                for (collieListener in mCollieListeners) {
                    collieListener.onANRAppear(activity)
                }
            }
        }
        mITrackMemoryLeakListener = object : ITrackMemoryListener {
            override fun onLeakActivity(activity: String?, count: Int) {
//                Log.v("Collie", "内存泄露 " + activity + " 数量 " + count);
                for (collieListener in mCollieListeners) {
                    collieListener.onLeakActivity(activity, count)
                }
            }

            override fun onCurrentMemoryCost(trackMemoryInfo: TrackMemoryInfo?) {
//                Log.v("Collie", "内存  " + trackMemoryInfo.procName + " java内存  "
//                        + trackMemoryInfo.appMemory.dalvikPss + " native内存  " +
//                        trackMemoryInfo.appMemory.nativePss);
                for (collieListener in mCollieListeners) {
                    collieListener.onCurrentMemoryCost(trackMemoryInfo)
                }
            }
        }
        mIBatteryListener = object : IBatteryListener {
            override fun onBatteryCost(batteryInfo: BatteryInfo?) {
                for (collieListener in mCollieListeners) {
                    collieListener.onBatteryCost(batteryInfo)
                }
            }
        }
    }
}