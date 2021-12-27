package com.snail.kotlin.debug

import android.app.Activity
import android.app.Application
import android.os.Handler
import com.snail.kotlin.Collie
import com.snail.kotlin.core.SimpleActivityLifecycleCallbacks
import com.snail.kotlin.core.ActivityStack.getTopActivity
import com.snail.kotlin.core.ActivityStack.isInBackGround
import com.snail.kotlin.core.CollieHandlerThread
import com.snail.kotlin.core.ITracker

/**
 * 掉帧检测
 */
class DebugHelper private constructor() : ITracker {
    private var mDebugCollieView: FloatingFpsView? = null
    private val mHandler: Handler = Handler(CollieHandlerThread.looper)
    private var mFloatHelper: FloatHelper? = null
    private val mSimpleActivityLifecycleCallbacks: SimpleActivityLifecycleCallbacks =
        object : SimpleActivityLifecycleCallbacks() {
            override fun onActivityStopped(activity: Activity) {
                super.onActivityStopped(activity)
                if (isInBackGround()) {
                    hide()
                }
            }

            override fun onActivityResumed(activity: Activity) {
                super.onActivityResumed(activity)
                show(activity.application)
            }
        }

    fun show(context: Application) {
        if (mFloatHelper != null && mFloatHelper!!.isShowing) {
            return
        }
        if (mDebugCollieView == null) {
            mDebugCollieView = FloatingFpsView(context)
            mFloatHelper = FloatHelper(context)
            mFloatHelper!!.setAlignSide(false)
                .setInitPosition(
                    context.resources.displayMetrics.widthPixels - MeasureUtil.getMeasuredWidth(
                        mDebugCollieView!!, 0
                    ), 200
                )
        }
        mHandler.post {
            mFloatHelper!!.setView(mDebugCollieView!!)
                .show(getTopActivity())
        }
    }

    fun hide() {
        if (mFloatHelper != null) {
            mFloatHelper!!.destroy()
            mHandler.removeCallbacksAndMessages(null)
        }
    }

    fun update(content: String?) {
        if (mFloatHelper != null && mFloatHelper!!.isShowing) {
            mHandler.post { mDebugCollieView!!.update(content) }
        }
    }

    override fun destroy(application: Application) {}
    override fun startTrack(application: Application) {
        Collie.instance.addActivityLifecycleCallbacks(mSimpleActivityLifecycleCallbacks)
    }

    override fun pauseTrack(application: Application) {}

    companion object {
        @Volatile
        private var sInstance: DebugHelper? = null
        @JvmStatic
        val instance: DebugHelper?
            get() {
                if (sInstance == null) {
                    synchronized(DebugHelper::class.java) {
                        if (sInstance == null) {
                            sInstance = DebugHelper()
                        }
                    }
                }
                return sInstance
            }
    }

}