package com.snail.collie.trafficstats;

import android.app.Activity;

public interface ITrackTrafficStatsListener {

    void onTrafficStats(Activity activity, long value);

}
