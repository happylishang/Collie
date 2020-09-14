package com.snail.collie.battery;

import android.app.Activity;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.snail.collie.Collie;
import com.snail.collie.core.ActivityStack;
import com.snail.collie.core.CollieHandlerThread;
import com.snail.collie.core.ITracker;
import com.snail.collie.core.SimpleActivityLifecycleCallbacks;
import com.snail.collie.debug.DebugHelper;
import com.snail.collie.startup.LauncherHelpProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BatteryStatsTracker implements ITracker {
    private static BatteryStatsTracker sInstance;
    private Handler mHandler;
    private String display;
    private int mStartPercent;

    private BatteryStatsTracker() {
        mHandler = new Handler(CollieHandlerThread.getInstance().getHandlerThread().getLooper());
    }

    public static BatteryStatsTracker getInstance() {
        if (sInstance == null) {
            synchronized (DebugHelper.class) {
                if (sInstance == null) {
                    sInstance = new BatteryStatsTracker();
                }
            }
        }
        return sInstance;
    }

    private SimpleActivityLifecycleCallbacks mSimpleActivityLifecycleCallbacks = new SimpleActivityLifecycleCallbacks() {


        @Override
        public void onActivityStarted(@NonNull Activity activity) {
            super.onActivityStarted(activity);
            final Application application = activity.getApplication();
            if (mStartPercent == 0 && ActivityStack.getInstance().getTopActivity() == activity) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                        Intent batteryStatus = application.registerReceiver(null, filter);
                        mStartPercent = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    }
                });
            }
        }

        @Override
        public void onActivityStopped(@NonNull final Activity activity) {
            super.onActivityStopped(activity);
            final Application application = activity.getApplication();
            if (ActivityStack.getInstance().isInBackGround()) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mListeners.size() > 0) {
                            BatteryInfo batteryInfo = computeBatteryInfo(application);
                            for (IBatteryListener listener : mListeners) {
                                listener.onBatteryCost(batteryInfo);
                            }
                        }

                    }
                });
            }
        }
    };

    @Override
    public void destroy(Application application) {
        Collie.getInstance().removeActivityLifecycleCallbacks(mSimpleActivityLifecycleCallbacks);
    }

    @Override
    public void startTrack(Application application) {
        Collie.getInstance().addActivityLifecycleCallbacks(mSimpleActivityLifecycleCallbacks);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
    }


    @Override
    public void pauseTrack(Application application) {

    }

    //    似乎并没有必要按照Activity统计耗点，每个界面很难超过1%
    private BatteryInfo computeBatteryInfo(Application application) {

        if (TextUtils.isEmpty(display)) {
            display = "" + application.getResources().getDisplayMetrics().widthPixels + "*" + application.getResources().getDisplayMetrics().heightPixels;
        }
        BatteryInfo batteryInfo = new BatteryInfo();
        try {
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = application.registerReceiver(null, filter);
            int status = batteryStatus.getIntExtra("status", 0);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            batteryInfo.charging = isCharging;
            batteryInfo.cost = isCharging ? 0 : mStartPercent - batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            batteryInfo.duration += (SystemClock.uptimeMillis() - LauncherHelpProvider.sStartUpTimeStamp) / 1000;
            batteryInfo.screenBrightness = getSystemScreenBrightnessValue(application);
            batteryInfo.display = display;
            batteryInfo.total = scale;
            Log.v("Battery", "total " + batteryInfo.total + " 用时间 " + batteryInfo.duration / 1000 + " 耗电  " + batteryInfo.cost);
        } catch (Exception e) {

        }

        return batteryInfo;
    }

    public int getSystemScreenBrightnessValue(Application application) {
        ContentResolver contentResolver = application.getContentResolver();
        int defVal = 125;
        return Settings.System.getInt(contentResolver,
                Settings.System.SCREEN_BRIGHTNESS, defVal);
    }

    private List<IBatteryListener> mListeners = new ArrayList<>();

    public void addBatteryListener(IBatteryListener listener) {
        mListeners.add(listener);
    }

    public void removeBatteryListener(IBatteryListener listener) {
        mListeners.remove(listener);
    }

    public interface IBatteryListener {
        void onBatteryCost(BatteryInfo batteryInfo);

    }
}
