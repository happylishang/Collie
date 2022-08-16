package com.snail.collie.core

import android.os.HandlerThread

object CollieHandlerThread : HandlerThread("collie_thread") {

    init {
        start()
    }

}