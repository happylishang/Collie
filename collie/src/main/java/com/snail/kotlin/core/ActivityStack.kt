package com.snail.kotlin.core

import android.app.Activity
import java.util.ArrayList

object ActivityStack {

    private val mActivities: MutableList<Activity> = mutableListOf()

    @Volatile
    private var mCurrentSate = 0

    fun push(activity: Activity) {
        mActivities.add(0, activity)
        CollieHandlerThread.start()
    }

    fun getSize(): Int {
        return mActivities.size
    }

    fun pop(activity: Activity) {
        mActivities.remove(activity)
    }

    fun markStart() {
        mCurrentSate++
    }

    fun markStop() {
        mCurrentSate--
    }

    fun getTopActivity(): Activity? {
        return   mActivities.first()
    }

    fun getBottomActivity(): Activity? {
        return mActivities.last()
    }

    fun isInBackGround(): Boolean {
        return mCurrentSate == 0
    }

}