package com.snail.collie.cpu;

import android.app.Application;

import com.snail.collie.core.ITracker;

public class CpuInfoTracker implements ITracker {

    // TODO: 2020/8/26 7.0以后 不方便读取，而且这个指标的线上意义不大，留给线下解决
    @Override
    public void destroy(Application application) {

    }

    @Override
    public void startTrack(Application application) {

    }

    @Override
    public void pauseTrack(Application application) {

    }
}
