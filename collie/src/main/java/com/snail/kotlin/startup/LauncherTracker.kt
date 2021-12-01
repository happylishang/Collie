package com.snail.kotlin.startup

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.graphics.Canvas
import android.os.*
import android.util.Log
import android.view.Choreographer
import android.view.View
import android.view.ViewGroup
import com.snail.collie.core.ProcessUtil
import com.snail.kotlin.core.CollieHandlerThread
import com.snail.kotlin.core.ITracker
import com.snail.kotlin.core.SimpleActivityLifecycleCallbacks

object LauncherTracker : ITracker {

    private var collectHandler: Handler = Handler(CollieHandlerThread.looper)
    private var codeStartUp = false
    private var launcherFlag = 0
    private var createFlag = 1
    private var lastActivityPauseTimeStamp: Long = 0
    private val mUIHandler = Handler(Looper.getMainLooper())
    var iLaunchTrackListener: ILaunchTrackListener? = null
    private var sStartUpTimeStamp = 0L
    private val activityLifecycleCallbacks: Application.ActivityLifecycleCallbacks =
        object : SimpleActivityLifecycleCallbacks() {
            override fun onActivityCreated(p0: Activity, p1: Bundle?) {
                super.onActivityCreated(p0, p1)
                // 重新开始或者第一个Activity
                if (lastActivityPauseTimeStamp == 0L) {
                    lastActivityPauseTimeStamp = SystemClock.uptimeMillis()
                }
                launcherFlag = createFlag
                val currentTimeStamp = lastActivityPauseTimeStamp
                mUIHandler.post {
                    if (p0.isFinishing) {
                        collectInfo(p0, currentTimeStamp, true)
                        lastActivityPauseTimeStamp = SystemClock.uptimeMillis()
                    }
                }
            }

            override fun onActivityResumed(p0: Activity) {
                super.onActivityResumed(p0)
                if (launcherFlag == createFlag) {
                    val currentTimeStamp = lastActivityPauseTimeStamp
                    (p0.window.decorView as ViewGroup).addView(CustomerView(p0, currentTimeStamp))
                }
                launcherFlag = 0
            }

            override fun onActivityPaused(p0: Activity) {
                super.onActivityPaused(p0)
                lastActivityPauseTimeStamp = SystemClock.uptimeMillis()
                launcherFlag = 0
                //
                codeStartUp = false
            }

            override fun onActivityStopped(p0: Activity) {
                super.onActivityStopped(p0)
                // 退到后台
                if (launcherFlag == 0) {
                    launcherFlag = 0
                    lastActivityPauseTimeStamp = 0
                }
            }
        }


    override fun destroy(application: Application) {
        application.unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks)
        iLaunchTrackListener = null
    }

    override fun startTrack(application: Application) {
        sStartUpTimeStamp = SystemClock.uptimeMillis()
        application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
        collectHandler.post {
            codeStartUp = isForegroundProcess(application)
        }
    }

    override fun pauseTrack(application: Application) {

    }

    private fun collectInfo(activity: Activity, lastPauseTimeStamp: Long, finishNow: Boolean) {

        val currentTimeStamp = SystemClock.uptimeMillis()
        val activityStartCost = currentTimeStamp - lastPauseTimeStamp
        collectHandler.post {
            iLaunchTrackListener?.let {
                if (codeStartUp) {
                    val coldLauncherTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                        currentTimeStamp - Process.getStartUptimeMillis() else
                        SystemClock.uptimeMillis() - sStartUpTimeStamp
                    it.onAppColdLaunchCost(coldLauncherTime, ProcessUtil.getProcessName())
                    codeStartUp = false
                }

                it.onActivityLaunchCost(activity, activityStartCost, finishNow)
            }

        }

    }

    // 判断进程启动的时候是否是前台进程
    private fun isForegroundProcess(context: Context): Boolean {
        val runningAppProcesses =
            (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).runningAppProcesses
        for (info in runningAppProcesses) {
            if (Process.myPid() == info.pid) {
                return info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
            }
        }
        return false
    }

    interface ILaunchTrackListener {
        fun onAppColdLaunchCost(duration: Long, procName: String?)
        fun onActivityLaunchCost(activity: Activity?, duration: Long, finishNow: Boolean)
    }

    class CustomerView(val activity: Activity, private val lastPauseTimeStamp: Long) :
        View(activity) {
        override fun onDraw(canvas: Canvas?) {
            super.onDraw(canvas)
            Log.v("Collie", "CustomerView")
            Choreographer.getInstance().postFrameCallback {
                collectInfo(activity, lastPauseTimeStamp, false)
            }
        }
    }
}