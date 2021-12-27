package com.snail.collie.mem

import android.os.Debug

class AppMemory {
    @JvmField
    var dalvikPss //java占用内存大小
            : Long = 0
    @JvmField
    var nativePss //前进程总私有已用内存大小
            : Long = 0
    @JvmField
    var totalPss //当前进程总内存大小
            : Long = 0

    //  整体信息
    @JvmField
    var mMemoryInfo: Debug.MemoryInfo? = null
}