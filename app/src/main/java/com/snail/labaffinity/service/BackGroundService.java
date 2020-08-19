package com.snail.labaffinity.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;

import com.snail.labaffinity.utils.LogUtils;

/**
 * Author: hzlishang
 * Data: 16/7/5 下午2:17
 * Des:
 * version:
 */
public class BackGroundService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.v("onStartCommand");
        return START_STICKY;
    }
}
