package com.snail.kotlin.core

import android.app.ActivityManager
import android.content.Context
import android.os.Process
import android.text.TextUtils

object ProcessUtil {
    var sProcName: String? = null

    @JvmStatic
    fun getProcessName(cxt: Context): String? {
        if (!TextUtils.isEmpty(sProcName)) {
            return sProcName
        }
        val pid = Process.myPid()
        val am: ActivityManager = cxt.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningApps: List<ActivityManager.RunningAppProcessInfo> = am.runningAppProcesses
            ?: return null
        for (procInfo in runningApps) {
            if (procInfo.pid == pid) {
                return procInfo.processName.also { sProcName = it }
            }
        }
        return null
    }
}