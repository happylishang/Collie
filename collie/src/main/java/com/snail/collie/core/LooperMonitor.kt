package com.snail.collie.core

import android.os.Build
import android.os.Looper
import android.os.MessageQueue
import android.os.SystemClock
import android.util.Printer
import androidx.annotation.CallSuper
import java.lang.Exception
import java.util.*

object LooperMonitor : MessageQueue.IdleHandler {

    private var looper: Looper
    private var printer: LooperPrinter? = null

    init {
        Objects.requireNonNull(Looper.getMainLooper())
        looper = Looper.getMainLooper()
        resetPrinter()
        addIdleHandler(looper)
    }

    @Synchronized
    private fun addIdleHandler(looper: Looper) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            looper.queue.addIdleHandler(this)
        } else {
            try {
                val queue = ReflectUtils.get<MessageQueue>(looper?.javaClass, "mQueue", looper)
                queue?.addIdleHandler(this)
            } catch (e: Exception) {
            }
        }
    }


    @Synchronized
    private fun removeIdleHandler(looper: Looper) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            looper.queue.removeIdleHandler(this)
        } else {
            try {
                val queue = ReflectUtils.get<MessageQueue>(looper.javaClass, "mQueue", looper)
                queue?.removeIdleHandler(this)
            } catch (e: Exception) {
            }
        }
    }

    private const val CHECK_TIME = 60 * 1000L
    private var lastCheckPrinterTime: Long = 0

    override fun queueIdle(): Boolean {
        //  定期重置
        if (SystemClock.uptimeMillis() - lastCheckPrinterTime >= LooperMonitor.CHECK_TIME) {
            resetPrinter()
            lastCheckPrinterTime = SystemClock.uptimeMillis()
        }
        return true
    }

    private var isReflectLoggingError = false

    @Synchronized
    private fun resetPrinter() {
        var originPrinter: Printer? = null
        try {
            if (!isReflectLoggingError) {
                originPrinter = ReflectUtils.get(looper.javaClass, "mLogging", looper)
                if (originPrinter === printer && null != printer) {
                    return
                }
            }
        } catch (e: Exception) {
            isReflectLoggingError = true
        }
        looper.setMessageLogging(LooperPrinter(originPrinter).also { printer = it })
    }

    class LooperPrinter(var originPrinter: Printer?) : Printer {

        override fun println(x: String?) {

            originPrinter?.let {
                if (it == this@LooperPrinter) {
                    return
                }
                it.println(x)
            }
            x?.let {
                if (x[0] == '>' || x[0] == '<') {
                    dispatch(x[0] == '>', x)
                }
            }
        }
    }

    abstract class LooperDispatchListener {
        var isHasDispatchStart = false

        open fun dispatchStart() {}
        open fun dispatchEnd() {}

        @CallSuper
        fun onDispatchStart(x: String?) {
            isHasDispatchStart = true
            dispatchStart()
        }

        @CallSuper
        fun onDispatchEnd(x: String?) {
            isHasDispatchStart = false
            dispatchEnd()
        }
    }

    private val listeners = mutableSetOf<LooperDispatchListener>()

    private fun dispatch(isBegin: Boolean, log: String) {
        for (listener in listeners) {
            if (isBegin) {
                if (!listener.isHasDispatchStart) {
                    listener.onDispatchStart(log)
                }
            } else {
                if (listener.isHasDispatchStart) {
                    listener.onDispatchEnd(log)
                }
            }
        }
    }

    @Synchronized
    fun release() {
        if (printer != null) {
            listeners.clear()
            looper.setMessageLogging(printer?.originPrinter)
            removeIdleHandler(looper)
            printer = null
        }
    }

    fun register(listener: LooperDispatchListener) {
        listeners.add(listener)
    }

    fun unregister(listener: LooperDispatchListener?) {
        listeners.remove(listener)
    }
}
