package com.snail.kotlin.trafficstats

import android.app.Activity

interface ITrackTrafficStatsListener {

    fun onTrafficStats(activity: Activity, value: Long)

}