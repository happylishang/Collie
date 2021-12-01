package com.snail.kotlin.mem

import com.snail.collie.mem.AppMemory

class TrackMemoryInfo {
    @JvmField
    var procName: String? = null

    @JvmField
    var appMemory: AppMemory? = null

    @JvmField
    var systemMemoryInfo: SystemMemory? = null

    @JvmField
    var display: String? = null

    @JvmField
    var activityCount = 0
}