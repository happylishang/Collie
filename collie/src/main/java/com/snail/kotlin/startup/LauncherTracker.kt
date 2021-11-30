package com.snail.kotlin.startup

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.SystemClock
import com.snail.kotlin.core.ITracker
import com.snail.kotlin.core.SimpleActivityLifecycleCallbacks

object LauncherTracker : ITracker {


    private var lastPauseTimeStamp = SystemClock.uptimeMillis()


    private val activityLifecycleCallbacks: Application.ActivityLifecycleCallbacks =
        object : SimpleActivityLifecycleCallbacks() {
            override fun onActivityCreated(p0: Activity, p1: Bundle?) {
                super.onActivityCreated(p0, p1)
            }

            override fun onActivityResumed(p0: Activity) {
                super.onActivityResumed(p0)
            }

            override fun onActivityPaused(p0: Activity) {
                super.onActivityPaused(p0)
            }
        }


    override fun destroy(application: Application) {

    }

    override fun startTrack(application: Application) {
    }

    override fun pauseTrack(application: Application) {
    }
}