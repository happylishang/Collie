package com.snail.kotlin.fps

import java.util.concurrent.LinkedBlockingQueue

/**
 * Author: snail
 * Data: 2021/10/9.
 * Des:
 * version:
 */
class FpsThread(var linkedBlockingQueue: LinkedBlockingQueue<Runnable>?) : Thread() {

    override fun run() {
        super.run()
        while (true) {
            try {
                val runnable = linkedBlockingQueue?.take()
                runnable?.run()
            } catch (ignored: Exception) {
            }
        }
    }
}