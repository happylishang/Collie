package com.snail.collie.trafficstats

import android.app.Activity

interface ITrackTrafficStatsListener {

    fun onTrafficStats(activity: Activity, value: Long)

}