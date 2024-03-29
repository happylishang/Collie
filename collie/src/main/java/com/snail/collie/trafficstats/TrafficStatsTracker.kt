package com.snail.collie.trafficstats

import android.app.Activity
import android.app.Application
import android.net.TrafficStats
import android.os.Bundle
import android.os.Process
import com.snail.collie.core.ITracker
import com.snail.collie.core.SimpleActivityLifecycleCallbacks

object TrafficStatsTracker : ITracker {

    private var currentStatsCost: Long = 0
    private var statsMap: MutableMap<Activity, TrafficStatsItem> = mutableMapOf()
    private var sequence: Int = 0
    var trafficStatsListener: ITrackTrafficStatsListener? = null

    private val applicationLife: SimpleActivityLifecycleCallbacks =
        object : SimpleActivityLifecycleCallbacks() {

            override fun onActivityCreated(p0: Activity, p1: Bundle?) {
                super.onActivityCreated(p0, p1)
                statsMap[p0] = TrafficStatsItem(p0, 0, sequence++, p0.javaClass.simpleName)
                currentStatsCost = TrafficStats.getUidRxBytes(Process.myUid())
            }

            override fun onActivityPaused(p0: Activity) {
                super.onActivityPaused(p0)
                statsMap[p0]?.let {
                    it.trafficCost += TrafficStats.getUidRxBytes(Process.myUid()) - currentStatsCost
                }
            }

            override fun onActivityDestroyed(p0: Activity) {
                super.onActivityDestroyed(p0)
                statsMap[p0]?.let { item ->
                    trafficStatsListener?.onTrafficStats(p0, item.trafficCost)
                }
            }
        }


    override fun destroy(application: Application) {
        application.unregisterActivityLifecycleCallbacks(applicationLife)
    }

    override fun startTrack(application: Application) {
        application.registerActivityLifecycleCallbacks(applicationLife)
    }

    override fun pauseTrack(application: Application) {

    }


}