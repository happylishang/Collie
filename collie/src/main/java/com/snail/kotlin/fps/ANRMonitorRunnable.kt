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

abstract class ANRMonitorRunnable(var invalid: Boolean, var activityRef: WeakReference<Activity>) : Runnable