package com.snail.collie.mem

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Debug
import android.os.Handler
import android.os.SystemClock
import android.text.TextUtils
import com.snail.collie.core.ActivityStack.isInBackGround
import com.snail.collie.core.ProcessUtil.getProcessName
import com.snail.collie.core.ActivityStack.getSize
import com.snail.collie.core.ITracker
import com.snail.collie.core.CollieHandlerThread
import com.snail.collie.Collie
import com.snail.collie.core.SimpleActivityLifecycleCallbacks
import java.lang.Exception
import java.util.*
import kotlin.jvm.Volatile

//WeakHashMap的Key-Value回收原理  还是依赖ref+Queue
class MemoryLeakTrack private constructor() : ITracker {
    private val mHandler = Handler(CollieHandlerThread.looper)
    private val mActivityStringWeakHashMap = WeakHashMap<Activity, String>()
    private val mSimpleActivityLifecycleCallbacks: SimpleActivityLifecycleCallbacks =
        object : SimpleActivityLifecycleCallbacks() {
            override fun onActivityDestroyed(activity: Activity) {
                super.onActivityDestroyed(activity)
                mActivityStringWeakHashMap[activity] = activity.javaClass.simpleName
            }

            override fun onActivityStopped(activity: Activity) {
                super.onActivityStopped(activity)
                //  退后台，GC 找LeakActivity
                if (!isInBackGround()) {
                    return
                }
                mHandler.postDelayed({
                    mallocBigMem()
                    Runtime.getRuntime().gc()
                }, 1000)
                mHandler.postDelayed(Runnable {
                    try {
                        if (!isInBackGround()) {
                            return@Runnable
                        }
                        //  分配大点内存促进GC
                        mallocBigMem()
                        Runtime.getRuntime().gc()
                        SystemClock.sleep(100)
                        System.runFinalization()
                        val hashMap = HashMap<String, Int>()
                        for ((key) in mActivityStringWeakHashMap) {
                            val name = key.javaClass.simpleName
                            val value = hashMap[name]
                            if (value == null) {
                                hashMap[name] = 1
                            } else {
                                hashMap[name] = value + 1
                            }
                        }
                        if (mMemoryListeners.size > 0) {
                            for ((key, value) in hashMap) {
                                for (listener in mMemoryListeners) {
                                    listener.onLeakActivity(key, value)
                                }
                            }
                        }
                    } catch (ignored: Exception) {
                    }
                }, 10000)
            }
        }

    override fun destroy(application: Application) {
        Collie.instance.removeActivityLifecycleCallbacks(mSimpleActivityLifecycleCallbacks)
        mHandler.removeCallbacksAndMessages(null)
    }

    override fun startTrack(application: Application) {
        Collie.instance.addActivityLifecycleCallbacks(mSimpleActivityLifecycleCallbacks)
        mHandler.postDelayed(object : Runnable {
            override fun run() {
                if (mMemoryListeners.size > 0 && !isInBackGround()) {
                    val trackMemoryInfo = collectMemoryInfo(application)
                    for (listener in mMemoryListeners) {
                        listener.onCurrentMemoryCost(trackMemoryInfo)
                    }
                }
                mHandler.postDelayed(this, (30 * 1000).toLong())
            }
        }, (30 * 1000).toLong())
    }

    override fun pauseTrack(application: Application) {}
    private val mMemoryListeners: MutableSet<ITrackMemoryListener> = HashSet()
    fun addOnMemoryLeakListener(leakListener: ITrackMemoryListener) {
        mMemoryListeners.add(leakListener)
    }

    fun removeOnMemoryLeakListener(leakListener: ITrackMemoryListener) {
        mMemoryListeners.remove(leakListener)
    }

    interface ITrackMemoryListener {
        fun onLeakActivity(activity: String?, count: Int)
        fun onCurrentMemoryCost(trackMemoryInfo: TrackMemoryInfo?)
    }

    private fun collectMemoryInfo(application: Application): TrackMemoryInfo {
        if (TextUtils.isEmpty(display)) {
            display =
                "" + application.resources.displayMetrics.widthPixels + "*" + application.resources.displayMetrics.heightPixels
        }
        val activityManager =
            application.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        // 系统内存
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val systemMemory = SystemMemory()
        systemMemory.availMem = memoryInfo.availMem shr 20
        systemMemory.totalMem = memoryInfo.totalMem shr 20
        systemMemory.lowMemory = memoryInfo.lowMemory
        systemMemory.threshold = memoryInfo.threshold shr 20

        //java内存
        val rt = Runtime.getRuntime()

        //进程Native内存
        val appMemory = AppMemory()
        val debugMemoryInfo = Debug.MemoryInfo()
        Debug.getMemoryInfo(debugMemoryInfo)
        appMemory.nativePss = (debugMemoryInfo.nativePss shr 10).toLong()
        appMemory.dalvikPss = (debugMemoryInfo.dalvikPss shr 10).toLong()
        appMemory.totalPss = (debugMemoryInfo.totalPss shr 10).toLong()
        appMemory.mMemoryInfo = debugMemoryInfo
        val trackMemoryInfo = TrackMemoryInfo()
        trackMemoryInfo.systemMemoryInfo = systemMemory
        trackMemoryInfo.appMemory = appMemory
        trackMemoryInfo.procName = getProcessName(application)
        trackMemoryInfo.display = display
        trackMemoryInfo.activityCount = getSize()
        return trackMemoryInfo
    }

    private fun mallocBigMem() {
        val leakHelpBytes = ByteArray(4 * 1024 * 1024)
        var i = 0
        while (i < leakHelpBytes.size) {
            leakHelpBytes[i] = 1
            i += 1024
        }
    }

    companion object {
        @Volatile
        private var sInstance: MemoryLeakTrack? = null
        @JvmStatic
        val instance: MemoryLeakTrack?
            get() {
                if (sInstance == null) {
                    synchronized(MemoryLeakTrack::class.java) {
                        if (sInstance == null) {
                            sInstance = MemoryLeakTrack()
                        }
                    }
                }
                return sInstance
            }
        private var display: String? = null
    }
}