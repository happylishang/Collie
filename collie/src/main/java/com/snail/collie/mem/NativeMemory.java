package com.snail.collie.mem;

public class NativeMemory {

    public long nativeHeapFreeSize;//当前进程navtive堆中已经剩余的内存大小
    public long nativeHeapAllocatedSize;//前进程navtive堆中已使用的内存大小
    public long nativeHeapSize;//当前进程navtive堆本身总的内存大小
}
