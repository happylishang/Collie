package com.snail.labaffinity.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Debug;
import android.os.SystemClock;
import android.util.Log;

import com.snail.collie.Collie;
import com.snail.collie.CollieListener;
import com.snail.collie.Config;
import com.snail.collie.battery.BatteryInfo;
import com.snail.collie.mem.TrackMemoryInfo;

import java.io.File;

import cn.campusapp.router.Router;

/**
 * Author: hzlishang
 * Data: 16/10/11 下午12:44
 * Des:
 * version:
 */
public class LabApplication extends Application {

    public static long sLaunchCost;
    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
        Router.initBrowserRouter(this);
        Router.initActivityRouter(getApplicationContext());
        Collie.getInstance().init(this, new Config(false, true, true, true, true, true), new CollieListener() {

            @Override
            public void onTrafficStats(Activity activity, long value) {
                Log.v("Collie", "" + activity.getClass().getSimpleName() + " 流量消耗 " + value * 1.0f / (1024 * 1024) + "M");

            }

            @Override
            public void onBatteryCost(BatteryInfo batteryInfo) {
                Log.v("Collie", " 电量流量消耗 " + batteryInfo.cost);

            }

            @Override
            public void onAppColdLaunchCost(long duration, String processName) {
                Log.v("Collie", "启动耗时 " + duration + " processName " + processName);
                sLaunchCost = duration;
            }

            @Override
            public void onActivityLaunchCost(Activity activity, long duration, boolean finishNow) {
                Log.v("Collie", "activity启动耗时 " + activity + " " + duration + " finishNow " + finishNow);
            }

            @Override
            public void onLeakActivity(String activity, int count) {
                Log.v("Collie", "内存泄露 " + activity + " 数量 " + count);
            }

            @Override
            public void onCurrentMemoryCost(TrackMemoryInfo trackMemoryInfo) {
                Log.v("Collie", "内存  " + trackMemoryInfo.procName + " java内存  "
                        + trackMemoryInfo.appMemory.dalvikPss + " native内存  " +
                        trackMemoryInfo.appMemory.nativePss);
            }

            @Override
            public void onFpsTrack(Activity activity, long currentCostMils, long currentDropFrame, boolean isInFrameDraw, long averageFps) {
                if (currentDropFrame >= 2)
                    Log.v("Collie", "Activity " + activity + " 掉帧 " + currentDropFrame + " 是否因为Choro 绘制掉帧 " + isInFrameDraw + " 1s 平均帧率" + averageFps);
            }

            @Override
            public void onANRAppear(Activity activity) {
                Log.v("Collie", "Activity " + activity + " ANR  ");

            }

            @Override
            public void onActivityFocusableCost(Activity activity, long duration, boolean finishNow) {
                Log.v("Collie", "Activity 获取焦点" + activity + " "+ duration   );

            }
        });
    }

    private static Application sApplication;

    public static Application getContext() {
        return sApplication;
    }

    public static void startTrace(Context context) {
        File file = new File(context.getExternalFilesDir("android"), SystemClock.uptimeMillis()+"methods.trace");
        Debug.startMethodTracing(file.getAbsolutePath(), 300 * 1024 * 1024);
    }
}
