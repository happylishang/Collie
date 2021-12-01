package com.snail.kotlin

import com.snail.collie.battery.BatteryStatsTracker.IBatteryListener
import com.snail.collie.fps.ITrackFpsListener
import com.snail.collie.mem.MemoryLeakTrack.ITrackMemoryListener
import com.snail.kotlin.startup.LauncherTracker
import com.snail.kotlin.trafficstats.ITrackTrafficStatsListener

interface CollieListener : LauncherTracker.ILaunchTrackListener, ITrackFpsListener, ITrackMemoryListener, ITrackTrafficStatsListener, IBatteryListener
