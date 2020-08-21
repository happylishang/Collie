package com.snail.collie.mem;

import android.app.ActivityManager;
import android.os.Debug;

public class TrackMemoryInfo {

    public String procName;
    public JavaMemory javaMemory;
    public NativeMemory nativeMemory;
    public SystemMemory systemMemoryInfo;
    public String display;
}
