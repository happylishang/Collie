package com.snail.kotlin.battery

/**
 * Author: snail
 * Data: 2021/12/22.
 * Des:
 * version:
 */
class BatteryInfo {

    @JvmField
    var charging: Boolean = false

    @JvmField
    var activityName: String? = null

    @JvmField
    var cost: Float = 0f

    @JvmField
    var duration: Long = 0

    @JvmField
    var display: String? = null

    @JvmField
    var total: Int = 0

    @JvmField
    var voltage: Int = 0

    @JvmField
    var screenBrightness: Float = 0f
}