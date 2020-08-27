package com.snail.labaffinity.app;

import android.app.Activity;
import android.app.Application;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.netease.nis.bugrpt.CrashHandler;
import com.snail.collie.Collie;
import com.snail.collie.CollieListener;
import com.snail.collie.Config;
import com.snail.collie.battery.BatteryInfo;
import com.snail.collie.mem.TrackMemoryInfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import cn.campusapp.router.Router;

/**
 * Author: hzlishang
 * Data: 16/10/11 下午12:44
 * Des:
 * version:
 */
public class LabApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.init(getApplicationContext());
//        CrashReport.initCrashReport(getApplicationContext(), "e7f834a1e0", BuildConfig.DEBUG);
        sApplication = this;
        Router.initBrowserRouter(this);
        Router.initActivityRouter(getApplicationContext());
        FirebaseAnalytics.getInstance(this);
        Collie.getInstance().init(this, new Config(true, true, true, true, true, true), new CollieListener() {

            @Override
            public void onBatteryCost(BatteryInfo batteryInfo) {
                Log.v("Collie",  " 电量流量消耗 " +batteryInfo.cost);

            }

            @Override
            public void onTrafficStats(String activityName, long value) {
                Log.v("Collie", "" + activityName + " 流量消耗 " + value * 1.0f / (1024 * 1024) + "M");
            }

            @Override
            public void onAppColdLaunchCost(long duration) {
                Log.v("Collie", "启动耗时 " + duration);
            }

            @Override
            public void onActivityLaunchCost(Activity activity, long duration,boolean finishNow) {
                Log.v("Collie", "activity启动耗时 " + activity + " " + duration + " finishNow "+finishNow);
//                if (duration > 800) {
//                toast 可能导致短时间内存泄露
//                    Toast.makeText(activity, "耗时 " + duration + "ms", Toast.LENGTH_SHORT).show();
//                }
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
                Log.v("Collie", "Activity " + activity + " ANR  " );

            }
        });
    }

    private static Application sApplication;

    public static Application getContext() {
        return sApplication;
    }

}
