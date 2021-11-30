package com.snail.collie;

import com.snail.collie.battery.BatteryStatsTracker;
import com.snail.collie.fps.ITrackFpsListener;
import com.snail.collie.mem.MemoryLeakTrack;
import com.snail.collie.startup.LauncherTracker;
import com.snail.kotlin.trafficstats.ITrackTrafficStatsListener;

public interface CollieListener extends LauncherTracker.ILaunchTrackListener, ITrackFpsListener, MemoryLeakTrack.ITrackMemoryListener, ITrackTrafficStatsListener, BatteryStatsTracker.IBatteryListener {

}
