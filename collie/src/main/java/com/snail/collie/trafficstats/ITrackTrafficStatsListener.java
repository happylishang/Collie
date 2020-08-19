package com.snail.collie.trafficstats;

public interface ITrackTrafficStatsListener {

    void onTrafficStats(String activityName, long value);

}
