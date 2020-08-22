package com.snail.collie;

import android.app.Activity;

import com.snail.collie.fps.ITrackFpsListener;
import com.snail.collie.mem.MemoryLeakTrack;
import com.snail.collie.startup.LauncherTrack;
import com.snail.collie.trafficstats.ITrackTrafficStatsListener;

public interface CollieListener extends LauncherTrack.ILaucherTrackListener, ITrackFpsListener, MemoryLeakTrack.ITrackMemoryListener, ITrackTrafficStatsListener {

}
