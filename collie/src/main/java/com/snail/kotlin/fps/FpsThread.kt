package com.snail.kotlin.fps

import com.snail.kotlin.trafficstats.TrafficStatsTracker
import java.util.concurrent.LinkedBlockingQueue

/**
 * Author: snail
 * Data: 2021/10/9.
 * Des:
 * version:
 */
class FpsThread : Thread() {


    var mLinkedBlockingQueue: LinkedBlockingQueue<Runnable>? = null


    override fun run() {
        super.run()
        while (true) {
            try {
                val runnable = mLinkedBlockingQueue?.take()
                runnable?.run()
            } catch (ignored: Exception) {
            }
        }
    }
}