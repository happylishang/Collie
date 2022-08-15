# Collie

轻量级Android性能监测工具

* FPS监测:  利用Looper的printLoop来实现
* 流量监测： Trafficstats
* 耗电 ：Battery BroadCast 似乎意义不是特别大
* 内存占用：Debug
* 内存泄漏：weakHashMap
* 启动耗时：ContentProvier+onwindforcus


技术文档：[Android线上轻量级APM性能监测方案](https://juejin.im/post/6872151038305140744)



### 使用方法 mavenCenter

app的build.gradle添加

	    implementation 'io.github.happylishang:collie:1.1.5'
	  
 

Application中添加

	       Collie.getInstance().init(this, new Config(true, true, true, true, true, true), new CollieListener() {
	
	            @Override
	            public void onTrafficStats(Activity activity, long value) {
	                Log.v("Collie", "" + activity.getClass().getSimpleName() + " 流量消耗 " + value * 1.0f / (1024 * 1024) + "M");
	
	            }
	
	            @Override
	            public void onBatteryCost(BatteryInfo batteryInfo) {
	                Log.v("Collie",  " 电量流量消耗 " +batteryInfo.cost);
	
	            }
	
	            @Override
	            public void onAppColdLaunchCost(long duration ,String processName) {
	                Log.v("Collie", "启动耗时 " + duration +" processName "+processName);
	            }
	
	            @Override
	            public void onActivityLaunchCost(Activity activity, long duration,boolean finishNow) {
	                Log.v("Collie", "activity启动耗时 " + activity + " " + duration + " finishNow "+finishNow);
	            }
	
	            @Override
	            public void onLeakActivity(String activity, int count) {
	                Log.v("Collie", "内存泄露 " + activity + " 数量 " + count);
	            }
	
	            @Override
	            public void onCurrentMemoryCost(TrackMemoryInfo trackMemoryInfo) {
	                Log.v("Collie", "内存  " + trackMemoryInfo.procName + " java内存  "
	                        + trackMemoryInfo.appMemory.dalvikPss + " native内存  " +
	                        trackMemoryInfo.appMemory.nativePss);
	            }
	
	            @Override
	            public void onFpsTrack(Activity activity, long currentCostMils, long currentDropFrame, boolean isInFrameDraw, long averageFps) {
	                if (currentDropFrame >= 2)
	                    Log.v("Collie", "Activity " + activity + " 掉帧 " + currentDropFrame + " 是否因为Choro 绘制掉帧 " + isInFrameDraw + " 1s 平均帧率" + averageFps);
	            }
	
	            @Override
	            public void onANRAppear(Activity activity) {
	                Log.v("Collie", "Activity " + activity + " ANR  " );
	
	            }
	        });
	    }	   
	    
	
