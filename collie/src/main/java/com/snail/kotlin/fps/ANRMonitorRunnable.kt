package com.snail.kotlin.fps

import android.app.Activity
import java.lang.ref.WeakReference

/**
 * Author: snail
 * Data: 2021/10/9.
 * Des:
 * version:
 */
//构造函数是否可以修改成类同名

abstract class ANRMonitorRunnable(val valid: Boolean, activity: Activity) : Runnable {

    var activityRef: WeakReference<Activity>? = null

    //   构造函数
    init {
        this.activityRef = WeakReference(activity)
    }
}