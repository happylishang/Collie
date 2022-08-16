package com.snail.collie.core

import android.app.Application

interface ITracker {

    fun destroy(application: Application)

    fun startTrack(application: Application)

    fun pauseTrack(application: Application)
}

