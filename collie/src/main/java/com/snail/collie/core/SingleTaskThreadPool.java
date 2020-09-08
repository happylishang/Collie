package com.snail.collie.core;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by zyl06 on 1/29/16.
 */
public class SingleTaskThreadPool {
    private static final String TAG = "ThreadPool";

    /* 单例 */
    private static SingleTaskThreadPool sInstance;



    private ThreadPoolExecutor executor = null;
    ExecutorService mExecutorService;

    private SingleTaskThreadPool() {
        init();
    }

    public static SingleTaskThreadPool getInstance() {
        if (sInstance == null) {
            synchronized (SingleTaskThreadPool.class) {
                if (sInstance == null) {
                    sInstance = new SingleTaskThreadPool();
                }
            }
        }

        return sInstance;
    }

    public void init() {
        mExecutorService = Executors.newSingleThreadExecutor();
    }

    /**
     * 增加新的任务 每增加一个新任务，都要唤醒任务队列
     *
     * @param newTask
     */
    public void addTask(Runnable newTask) {
        mExecutorService.execute(newTask);
    }

    /**
     * 销毁线程池
     */
    public synchronized void destroy() {

        if (executor != null && !executor.isShutdown()) {
            mExecutorService.shutdown();
        }
    }

}

