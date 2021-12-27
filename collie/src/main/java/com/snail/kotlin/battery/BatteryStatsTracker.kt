package com.snail.kotlin.battery

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Handler
import android.os.SystemClock
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import com.snail.kotlin.core.ITracker
import com.snail.kotlin.core.SimpleActivityLifecycleCallbacks
import com.snail.kotlin.debug.DebugHelper
import com.snail.kotlin.core.ActivityStack.getTopActivity
import com.snail.kotlin.core.ActivityStack.isInBackGround
import com.snail.kotlin.core.CollieHandlerThread
import java.util.*

class BatteryStatsTracker private constructor() : ITracker {

    private var mHandler: Handler = Handler(CollieHandlerThread.looper)
    private var display: String? = null
    private var mStartPercent = 0
    private val mSimpleActivityLifecycleCallbacks: SimpleActivityLifecycleCallbacks =
        object : SimpleActivityLifecycleCallbacks() {
            override fun onActivityStarted(activity: Activity) {
                super.onActivityStarted(activity)
                val application = activity.application
                if (mStartPercent == 0 && getTopActivity() === activity) {
                    mHandler.post {
                        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                        val batteryStatus = application.registerReceiver(null, filter)
                        mStartPercent = batteryStatus!!.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    }
                }
            }

            override fun onActivityStopped(activity: Activity) {
                super.onActivityStopped(activity)
                val application = activity.application
                if (isInBackGround()) {
                    mHandler.post {
                        if (mListeners.size > 0) {
                            val batteryInfo = computeBatteryInfo(application)
                            for (listener in mListeners) {
                                listener.onBatteryCost(batteryInfo)
                            }
                        }
                    }
                }
            }
        }

    override fun destroy(application: Application) {
        application.unregisterActivityLifecycleCallbacks(mSimpleActivityLifecycleCallbacks)
    }

    override fun startTrack(application: Application) {
        application.registerActivityLifecycleCallbacks(mSimpleActivityLifecycleCallbacks)
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED)
    }

    override fun pauseTrack(application: Application) {}

    //    似乎并没有必要按照Activity统计耗点，每个界面很难超过1%
    private fun computeBatteryInfo(application: Application): BatteryInfo {
        if (TextUtils.isEmpty(display)) {
            display =
                "" + application.resources.displayMetrics.widthPixels + "*" + application.resources.displayMetrics.heightPixels
        }
        val batteryInfo = BatteryInfo()
        try {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus = application.registerReceiver(null, filter)
            val status = batteryStatus!!.getIntExtra("status", 0)
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
            val scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            batteryInfo.charging = isCharging
            batteryInfo.cost = if (isCharging) 0f else mStartPercent - batteryStatus.getIntExtra(
                BatteryManager.EXTRA_LEVEL, -1
            ).toFloat()
            batteryInfo.duration += (SystemClock.uptimeMillis() - sStartUpTimeStamp) / 1000
            batteryInfo.screenBrightness = getSystemScreenBrightnessValue(application).toFloat()
            batteryInfo.display = display
            batteryInfo.total = scale
            Log.v(
                "Battery",
                "total " + batteryInfo.total + " 用时间 " + batteryInfo.duration / 1000 + " 耗电  " + batteryInfo.cost
            )
        } catch (e: Exception) {
        }
        return batteryInfo
    }

    fun getSystemScreenBrightnessValue(application: Application): Int {
        val contentResolver = application.contentResolver
        val defVal = 125
        return Settings.System.getInt(
            contentResolver,
            Settings.System.SCREEN_BRIGHTNESS, defVal
        )
    }

    private val mListeners: MutableList<IBatteryListener> = ArrayList()
    fun addBatteryListener(listener: IBatteryListener) {
        mListeners.add(listener)
    }

    fun removeBatteryListener(listener: IBatteryListener) {
        mListeners.remove(listener)
    }

    interface IBatteryListener {
        fun onBatteryCost(batteryInfo: BatteryInfo?)
    }

    companion object {
        private var sInstance: BatteryStatsTracker? = null
        private val sStartUpTimeStamp = SystemClock.uptimeMillis()

        @JvmStatic
        val instance: BatteryStatsTracker?
            get() {
                if (sInstance == null) {
                    synchronized(DebugHelper::class.java) {
                        if (sInstance == null) {
                            sInstance = BatteryStatsTracker()
                        }
                    }
                }
                return sInstance
            }
    }

}