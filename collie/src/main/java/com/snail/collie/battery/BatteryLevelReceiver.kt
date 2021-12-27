package com.snail.collie.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager

class BatteryLevelReceiver : BroadcastReceiver() {

    var currentBatteryLevel = 0
        private set
    var totalBatteryPercent = 0
        private set
    var isCharging = false
        private set
    var voltage = 0
        private set

    override fun onReceive(context: Context, intent: Intent) {
        //当前剩余电量
        currentBatteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        //电量最大值
        totalBatteryPercent = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
        val status = intent.getIntExtra("status", 0)
        isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
    }
}