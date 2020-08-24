package com.snail.labaffinity.app;

import android.app.Activity;
import android.app.Application;
import android.os.SystemClock;
import android.text.TextUtils;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.netease.nis.bugrpt.CrashHandler;
import com.snail.collie.Collie;
import com.snail.collie.CollieListener;
import com.snail.collie.Config;
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
        Collie.getInstance().init(this, new Config(), new CollieListener() {

            @Override
            public void onTrafficStats(String activityName, long value) {

            }

            @Override
            public void onColdLaunchCost(long duration) {

            }

            @Override
            public void onActivityLaunchCost(Activity activity, long duration) {

            }

            @Override
            public void onLeakActivity(String activity, int count) {

            }

            @Override
            public void onCurrentMemoryCost(TrackMemoryInfo trackMemoryInfo) {

            }

            @Override
            public void onFpsTrack(Activity activity, long currentCostMils, long currentDropFrame, boolean isInFrameDraw, long averageFps) {

            }
        });
    }

    private static Application sApplication;

    public static Application getContext() {
        return sApplication;
    }

}
