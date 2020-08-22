package com.snail.collie.mem;

import android.os.Debug;

public class AppMemory {


    public long dalvikPss;//java占用内存大小
    public long nativePss;//前进程总私有已用内存大小
    public long totalPss;//当前进程总内存大小

    //  整体信息
    public Debug.MemoryInfo mMemoryInfo;
}
