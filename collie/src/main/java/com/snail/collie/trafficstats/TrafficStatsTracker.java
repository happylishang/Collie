package com.snail.collie.trafficstats;

import android.app.Activity;
import android.net.TrafficStats;
import android.os.Process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TrafficStatsTracker {

    private static volatile TrafficStatsTracker sInstance = null;
    private HashMap<Activity, TrafficStatsItem> mHashMap = new HashMap<>();
    private long mCurrentStats;
    private static int sSequence;
    private List<ITrackTrafficStatsListener> mStatsListeners = new ArrayList<>();

    public void addTackTrafficStatsListener(ITrackTrafficStatsListener listener) {
        mStatsListeners.add(listener);
    }

    public void removeTrackTrafficStatsListener(ITrackTrafficStatsListener listener) {

        mStatsListeners.remove(listener);
    }

    private TrafficStatsTracker() {
    }

    public static TrafficStatsTracker getInstance() {
        if (sInstance == null) {
            synchronized (TrafficStatsTracker.class) {
                if (sInstance == null) {
                    sInstance = new TrafficStatsTracker();
                }
            }
        }
        return sInstance;
    }

    public void markActivityStart(Activity activity) {
        if (mHashMap.get(activity) == null) {
            TrafficStatsItem item = new TrafficStatsItem();
            item.activity = activity;
            item.sequence = sSequence++;
            item.trafficCost = 0;
            item.activityName = activity.getClass().getSimpleName();
            mHashMap.put(activity,item);
        }
        mCurrentStats = TrafficStats.getUidRxBytes(Process.myUid());
    }

    //  以pause为中断点
    public void markActivityPause(Activity activity) {
        TrafficStatsItem item = mHashMap.get(activity);
        if (item != null) {
            item.trafficCost += TrafficStats.getUidRxBytes(Process.myUid()) - mCurrentStats;
        }
    }

    //   防止泄露
    public void markActivityDestroy(Activity activity) {
        TrafficStatsItem item = mHashMap.get(activity);
        if (item != null) {
            item.activity = null;
            for (ITrackTrafficStatsListener trafficStatsListener : mStatsListeners) {
                trafficStatsListener.onTrafficStats(item.activityName, item.trafficCost);
                mHashMap.remove(activity);
            }
        }
    }

}
