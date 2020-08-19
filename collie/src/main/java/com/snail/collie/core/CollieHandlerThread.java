package com.snail.collie.core;

import android.os.HandlerThread;

public class CollieHandlerThread {

    private static volatile CollieHandlerThread sInstance = null;

    public HandlerThread getHandlerThread() {
        return mHandlerThread;
    }

    private HandlerThread mHandlerThread;

    private CollieHandlerThread() {
        mHandlerThread = new HandlerThread("track_performance");
        mHandlerThread.start();
    }

    public static CollieHandlerThread getInstance() {
        if (sInstance == null) {
            synchronized (CollieHandlerThread.class) {
                if (sInstance == null) {
                    sInstance = new CollieHandlerThread();
                }
            }
        }
        return sInstance;
    }


}
