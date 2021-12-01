package com.snail.kotlin.fps

import android.app.Activity

interface ITrackFpsListener {

    fun onFpsTrack(
        activity: Activity,
        currentCostMils: Long,
        currentDropFrame: Long,
        isInFrameDraw: Boolean,
        averageFps: Long
    )

    fun onANRAppear(activity: Activity?)

}