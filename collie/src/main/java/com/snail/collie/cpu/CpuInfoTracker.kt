package com.snail.collie.cpu

import android.app.Application
import com.snail.collie.core.ITracker

class CpuInfoTracker : ITracker {
    // TODO: 2020/8/26 7.0以后 不方便读取，而且这个指标的线上意义不大，留给线下解决
    override fun destroy(application: Application) {}
    override fun startTrack(application: Application) {}
    override fun pauseTrack(application: Application) {}
}