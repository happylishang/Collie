package com.snail.collie.core;

import android.app.Application;

public interface ITracker {


    void destroy(Application application);

    void startTrack(Application application);

    void pauseTrack(Application application);

}
