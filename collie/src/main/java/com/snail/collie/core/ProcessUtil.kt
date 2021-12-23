package com.snail.collie.core

import android.os.Process
import android.text.TextUtils
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException

object ProcessUtil {
    var sProcName: String? = null
    @JvmStatic
    val processName: String?
        get() {
            if (!TextUtils.isEmpty(sProcName)) {
                return sProcName
            }
            var reader: BufferedReader? = null
            try {
                reader = BufferedReader(FileReader("/proc/" + Process.myPid() + "/cmdline"))
                var processName = reader.readLine()
                if (!TextUtils.isEmpty(processName)) {
                    processName = processName.trim { it <= ' ' }
                }
                return processName.also { sProcName = it }
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
            } finally {
                try {
                    reader?.close()
                } catch (exception: IOException) {
                    exception.printStackTrace()
                }
            }
            return null
        }
}