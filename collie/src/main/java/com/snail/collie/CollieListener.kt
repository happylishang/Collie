package com.snail.collie

import com.snail.collie.battery.BatteryStatsTracker.IBatteryListener
import com.snail.collie.mem.MemoryLeakTrack.ITrackMemoryListener
import com.snail.collie.fps.ITrackFpsListener
import com.snail.collie.startup.LauncherTracker
import com.snail.collie.trafficstats.ITrackTrafficStatsListener

interface CollieListener : LauncherTracker.ILaunchTrackListener, ITrackFpsListener, ITrackMemoryListener, ITrackTrafficStatsListener, IBatteryListener
