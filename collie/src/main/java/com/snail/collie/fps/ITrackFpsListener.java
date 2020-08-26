package com.snail.collie.fps;

import android.app.Activity;

public interface ITrackFpsListener {

    void onFpsTrack(final Activity activity, final long currentCostMils, final long currentDropFrame, final boolean isInFrameDraw, long averageFps);

    void onANRAppear(final Activity activity, final long currentCostMils);
}
