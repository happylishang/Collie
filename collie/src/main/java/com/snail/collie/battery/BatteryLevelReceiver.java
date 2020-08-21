package com.snail.collie.battery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

public class BatteryLevelReceiver extends BroadcastReceiver {

    private volatile float batteryPct;

    @Override
    public void onReceive(Context context, Intent intent) {
        //当前剩余电量
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        //电量最大值
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        //电量百分比
        batteryPct = level / (float) scale;
    }

    public int getCurrentBatteryPercent() {
        return (int) (batteryPct * 100);
    }
}
