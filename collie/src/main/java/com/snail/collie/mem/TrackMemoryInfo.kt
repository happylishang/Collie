package com.snail.collie.mem

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