package com.snail.kotlin.fps

import android.app.Activity

/**
 * Author: snail
 * Data: 2021/10/9.
 * Des:
 * version:
 */
class CollectItem constructor(

    @JvmField var activity: Activity? = null,
    @JvmField var sumCost: Long = 0,
    @JvmField var sumFrame: Int = 0
)