package com.snail.kotlin.trafficstats

import android.app.Activity

class TrafficStatsItem(
   var activity: Activity?,
   var trafficCost: Long,
   var sequence: Int,
   var activityName: String?
)