package com.snail.collie;

import com.snail.collie.fps.ITrackFpsListener;
import com.snail.collie.mem.MemoryLeakTrack;
import com.snail.collie.startup.LauncherTracker;
import com.snail.collie.trafficstats.ITrackTrafficStatsListener;

public interface CollieListener extends LauncherTracker.ILaunchTrackListener, ITrackFpsListener, MemoryLeakTrack.ITrackMemoryListener, ITrackTrafficStatsListener {

}
