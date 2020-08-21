package com.snail.labaffinity.app;

import android.app.Activity;
import android.app.Application;
import android.text.TextUtils;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.netease.nis.bugrpt.CrashHandler;
import com.snail.collie.Collie;
import com.snail.collie.CollieListener;
import com.snail.collie.Config;

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
            public void onFpsTrack(Activity activity, long currentFps, long currentDropFrame, long averageFps) {

            }
        });
    }

    private static Application sApplication;

    public static Application getContext() {
        return sApplication;
    }

    private static String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }
}
