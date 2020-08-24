package com.snail.collie;

public class Config {

    public boolean showDebugView;//是否展示悬浮view
    public boolean userFpsTrack;//是否打开fps
    public boolean userTrafficTrack;//流量监控
    public boolean userMemTrack;//Activity泄露及内存情况
    public boolean userBatteryTrack;//电量
    public boolean userStartUpTrack;//电量

    public Config(boolean showDebugView, boolean userFpsTrack, boolean userTrafficTrack, boolean userMemTrack, boolean userBatteryTrack, boolean userStartUpTrack) {
        this.showDebugView = showDebugView;
        this.userFpsTrack = userFpsTrack;
        this.userTrafficTrack = userTrafficTrack;
        this.userMemTrack = userMemTrack;
        this.userBatteryTrack = userBatteryTrack;
        this.userStartUpTrack = userStartUpTrack;
    }
}
