package com.snail.collie.mem

import android.os.Process
import android.system.Os
import android.util.Log
import java.io.File

object FdLeakTrack {

    fun collectLeakFds() {
        val fdFile = File("/proc/" + Process.myPid() + "/fd/")
        val files = fdFile.listFiles()
        files?.forEach { file ->
            try {
                 Os.readlink(file.absolutePath);
            } catch (e: Exception) {
            }
        }
    }
}