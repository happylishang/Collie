package com.snail.kotlin

import com.snail.kotlin.battery.BatteryStatsTracker.IBatteryListener
import com.snail.kotlin.mem.MemoryLeakTrack.ITrackMemoryListener
import com.snail.kotlin.fps.ITrackFpsListener
import com.snail.kotlin.startup.LauncherTracker
import com.snail.kotlin.trafficstats.ITrackTrafficStatsListener

interface CollieListener : LauncherTracker.ILaunchTrackListener, ITrackFpsListener, ITrackMemoryListener, ITrackTrafficStatsListener, IBatteryListener
