package com.snail.kotlin

class Config(val showDebugView: Boolean, //是否展示悬浮view
             val useFpsTrack: Boolean,//是否打开fps
             val useTrafficTrack: Boolean,//流量监控
             val useMemTrack: Boolean,//Activity泄露及内存情况
             val useBatteryTrack: Boolean, //电量
             val useStartUpTrack: Boolean)//启动