package com.snail.kotlin.startup

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import com.snail.collie.startup.LauncherTracker
import com.snail.kotlin.core.ITracker
import com.snail.kotlin.core.SimpleActivityLifecycleCallbacks

object LauncherTracker : ITracker {

    private var mHandler: Handler? = null
    private var markCodeStartUp = false
    private var launcherFlag = 0
    private var createFlag = 1
    private var resumeFlag = 1 shl 1
    private var startFlag = createFlag or resumeFlag
    private var mActivityLauncherTimeStamp: Long = 0
    private val mUIHandler = Handler(Looper.getMainLooper())
    
    private val activityLifecycleCallbacks: Application.ActivityLifecycleCallbacks =
        object : SimpleActivityLifecycleCallbacks() {
            override fun onActivityCreated(p0: Activity, p1: Bundle?) {
                super.onActivityCreated(p0, p1)
                if (mActivityLauncherTimeStamp == 0L) {
                    mActivityLauncherTimeStamp = SystemClock.uptimeMillis()
                }

            }

            override fun onActivityResumed(p0: Activity) {
                super.onActivityResumed(p0)
            }

            override fun onActivityPaused(p0: Activity) {
                super.onActivityPaused(p0)
                mActivityLauncherTimeStamp = SystemClock.uptimeMillis()
            }
        }


    override fun destroy(application: Application) {

    }

    override fun startTrack(application: Application) {

    }

    override fun pauseTrack(application: Application) {
    }
}