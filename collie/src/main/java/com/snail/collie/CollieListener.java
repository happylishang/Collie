package com.snail.collie;

import android.app.Activity;

public interface CollieListener {

   void onFpsTrack(Activity activity, long currentFps, long currentDropFrame, long averageFps);

}
