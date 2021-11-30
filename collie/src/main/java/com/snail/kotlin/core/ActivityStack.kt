package com.snail.kotlin.core

import android.app.Activity
import java.util.ArrayList

object ActivityStack {

    private val mActivities: MutableList<Activity> = ArrayList()

    @Volatile
    private var mCurrentSate = 0

    fun push(activity: Activity) {
        mActivities.add(0, activity)
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
        return if (mActivities.size > 0) mActivities[0] else null
    }

    fun getBottomActivity(): Activity? {
        return if (mActivities.size > 0) mActivities[mActivities.size - 1] else null
    }

    fun isInBackGround(): Boolean {
        return mCurrentSate == 0
    }

}