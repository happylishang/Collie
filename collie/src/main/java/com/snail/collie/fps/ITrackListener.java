package com.snail.collie.fps;

public interface ITrackListener {

    void onHandlerMessageCost(final long currentCostMils, final long currentDropFrame, final boolean isInFrameDraw, long averageFps);

}
