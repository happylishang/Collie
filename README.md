# Collie

轻量级Android性能监测工具

* FPS监测:  利用Looper的printLoop来实现
* 流量监测： Trafficstats
* 耗电 ：Battery BroadCast 似乎意义不是特别大
* 内存占用：Debug
* 内存泄漏：weakHashMap
* 启动耗时：ContentProvier+onwindforcus


技术文档：[Android线上轻量级APM性能监测方案](https://juejin.im/post/6872151038305140744)



## App性能如何量化

如何衡量一个APP性能好坏？直观感受就是：启动快、流畅、不闪退、耗电少等感官指标，反应到技术层面包装下就是：FPS（帧率）、界面渲染速度、Crash率、网络、CPU使用率、电量损耗速度等，一般挑其中几个关键指标作为APP质量的标尺。目前也有多种开源APM监控方案，但大部分偏向离线检测，对于线上监测而言显得太重，可能会适得其反，方案简单对比如下：


   SDK     		|  现状与问题       | 是否推荐直接线上使用     |
--------------------|------------------|-----------------------|
腾讯matrix       | 功能全，但是重，而且运行测试期间经常Crash      | 否   |
腾讯GT       	   | 2018年之后没更新，关注度低，本身功能挺多，也挺重性价比还不如matrix  |否   |
网易Emmagee      | 2018年之后没更新，几乎没有关注度，重 |否        |
听云App          |  适合监测网络跟启动，场景受限  | 否   |

还有其他多种APM检测工具，功能复杂多样，但其实很多指标并不是特别重要，实现越复杂，线上风险越大，因此，并不建议直接使用。而且，分析多家APP的实现原理，其核心思路基本相同，且门槛也并不是特别高，建议自研一套，在灵活性、安全性上更有保障，更容易做到轻量级。本文主旨就是**围绕几个关键指标**：FPS、内存（内存泄漏）、界面启动、流量等，实现**轻量级**的线上监测。


## 核心性能指标拆解

* 稳定性：Crash统计

 Crash统计与聚合有比较通用的策略，比如Firebase、Bugly等，不在本文讨论范围
 
*  网络请求

每个APP的网络请求一般都存在统一的Hook点，门槛很低，且各家请求协议与SDK有别，很难实现统一的网络请求监测，其次，想要真正定位网络请求问题，可能牵扯整个请求的链路，更适合做一套网络全链路监控APM，也不在讨论范围。

* 冷启动时间及各个Activity页面启动时间 (存在统一方案)
* 页面FPS、卡顿、ANR    （存在统一方案）
* 内存统计及内存泄露侦测 （存在统一方案）
* 流量消耗   （存在统一方案）
* 电量   （存在统一方案）
* CPU使用率（CPU）：还没想好咋么用，7.0之后实现机制也变了，先不考虑

线上监测的重点就聚焦后面几个，下面逐个拆解如何实现。

### 启动耗时


直观上说界面启动就是：从点击一个图标到看到下一个界面首帧，如果这个过程耗时较长，用户会会感受到顿挫，影响体验。从场景上说，启动耗时间简单分两种：

* 冷启动耗时：在APP未启动的情况从，从点击桌面icon 到看到闪屏Activity的首帧（非默认背景） 
* 界面启动耗：APP启动后，从上一个界面pause，到下一个界面首帧可见， 

本文粒度较粗，主要聚焦Activity，这里有个比较核心的时机：Activity首帧可见点，这个点究竟在什么时候？经分析测试发现，不同版本表现不一，在Android 10 之前这个点与onWindowFocusChanged回调点基本吻合，在Android  10 之后，系统做了优化，将首帧可见的时机提前到onWindowFocusChanged之前，可以简单看做onResume（或者onAttachedToWindow）之后，对于一开始点击icon的点，可以约等于APP进程启动的点，拿到了上面两个时间点，就可以得到冷启动耗时。

APP进程启动的点可以通过加载一个空的ContentProvider来记录，因为ContentProvider的加载时机比较靠前，早于Application的onCreate之前，相对更准确一点，很多SDK的初始也采用这种方式，实现如下：
	
	public class LauncherHelpProvider extends ContentProvider {
	
	    // 用来记录启动时间
	    public static long sStartUpTimeStamp = SystemClock.uptimeMillis();
	    ...
	    
	    }

这样就得到了冷启动的开始时间，如何得到第一个Activity界面可见的时间呢？比较简单的做法是在SplashActivity中进行打点，对于Android 10 以前的，可以在onWindowFocusChanged中打点，在Android 10以后，可以在onResume之后进行打点。不过，做SDK需要减少对业务的入侵，可以借助Applicattion监听Activity Lifecycle无入侵获取这个时间点。对于Android 10之前系统， 可以利用ViewTreeObserve监听nWindowFocusChange回调，达到无入侵获取onWindowFocusChanged调用点，示意代码如下

       application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
           ....
           @Override
        public void onActivityResumed(@NonNull final Activity activity) {
            super.onActivityResumed(activity);
            launcherFlag |= resumeFlag;
            
              <!--添加onWindowFocusChanged 监听-->
            	activity.getWindow().getDecorView().getViewTreeObserver().addOnWindowFocusChangeListener(new ViewTreeObserver.OnWindowFocusChangeListener() {
            	<!--onWindowFocusChanged回调-->
                @Override
                public void onWindowFocusChanged(boolean b) {
                    if (b && (launcherFlag ^ startFlag) == 0) {
                       <!--判断是不是首个Activity-->
                        final boolean isColdStarUp = ActivityStack.getInstance().getBottomActivity() == activity;
                        <!--获取首帧可见距离启动的时间-->
                        final long coldLauncherTime = SystemClock.uptimeMillis() - LauncherHelpProvider.sStartUpTimeStamp;
                        final long activityLauncherTime = SystemClock.uptimeMillis() - mActivityLauncherTimeStamp;
                        activity.getWindow().getDecorView().getViewTreeObserver().removeOnWindowFocusChangeListener(this);
                        <!--异步线程处理回调，减少UI线程负担-->
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (isColdStarUp) {
                                //todo 监听到冷启动耗时
                                ...


对于Android 10以后的系统，可以在onActivityResumed回调时添加一UI线程Message来达到监听目的，代码如下

        @Override
        public void onActivityResumed(@NonNull final Activity activity) {
            super.onActivityResumed(activity);
            if (launcherFlag != 0 && (launcherFlag & resumeFlag) == 0) {
                launcherFlag |= resumeFlag;
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                    //  10 之后有改动，第一帧可见提前了 可认为onActivityResumed之后
                    mUIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            <!--获取第一帧可见时间点-->                        }
                    });
                }

如此就可以检测到冷启动耗时。APP启动后，各Activity启动耗时计算逻辑类似，首帧可见点沿用上面方案即可，不过这里还缺少上一个界面暂停的点，经分析测试，锚在上一个Actiivty pause的时候比较合理，因此Activity启动耗时定义如下：

	Activity启动耗时 = 当前Activity 首帧可见 - 上一个Activity onPause被调用

同样为了减轻对业务入侵，也依赖registerActivityLifecycleCallbacks来实现：补全上方缺失

       application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
	
		   @Override
	        public void onActivityPaused(@NonNull Activity activity) {
	            super.onActivityPaused(activity);
	            <!--记录上一个Activity pause节点-->
	            mActivityLauncherTimeStamp = SystemClock.uptimeMillis();
	            launcherFlag = 0;
	        }
	        ...
        @Override
        public void onActivityResumed(@NonNull final Activity activity) {
            super.onActivityResumed(activity);
            launcherFlag |= resumeFlag;
           <!--参考上面获取首帧的点-->
                 ...
 
到这里就获取了两个比较关键的启动耗时，不过，时机使用中可能存在各种异常场景：比如闪屏页在onCreate或者onResume中调用了finish跳转首页，对于这种场景就需要额外处理，比如在onCreate中调用了finish，onResume可能不会被调用，这个时候就要在 onCreate之后进行统计，同时利用用Activity.isFinishing()标识这种场景，其次，启动耗时对于不同配置也是不一样的，不能用绝对时间衡量，只能横向对比，简单线上效果如下：

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/c061d878832f4245804988d698f4554c~tplv-k3u1fbpfcp-zoom-1.image)

![](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/b8b1072f95114a9f816d7e81933749ee~tplv-k3u1fbpfcp-zoom-1.image)


![](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/b8b1072f95114a9f816d7e81933749ee~tplv-k3u1fbpfcp-zoom-1.image)
	
### 流畅度及FPS(Frames Per Second）监测

FPS是图像领域中的定义，指画面每秒传输帧数，每秒帧数越多，显示的动作就越流畅。FPS可以作为衡量流畅度的一个指标，但是，从各厂商的报告来看，仅用FPS来衡量是否流畅并不科学。电影或视频的FPS并不高，30的FPS即可满足人眼需求，稳定在30FPS的动画，并不会让人感到卡顿，但如果FPS 很不稳定的话，就很容易感知到卡顿，注意，这里有个词叫**稳定**。举个**极端**例子：前500ms刷新了59帧，后500ms只绘制一帧，即使达到了60FPS，仍会感知卡顿，这里就突出**稳定**的重要性。不过FPS也并不是完全没用，可以用其上限定义流畅，用其下限可以定义卡顿，对于中间阶段的感知，FPS无能为力，如下示意：

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/ead10033dc78436c9b77e52b5ba6d5ac~tplv-k3u1fbpfcp-zoom-1.image)

上面那个是极端例子，Android 系统中，VSYNC会杜绝16ms内刷新两次，那么在中间的情况下怎么定义流畅？比如，FPS降低到50会卡吗？答案是不一定。50的FPS如果是均分到各个节点，用户是感知不到掉帧的，但，如果丢失的10帧全部在一次绘制点，那就能明显感知卡顿，这个时候，**瞬时帧率**的意义更大，如下

![](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/478bfa7fae144ec0bb8f87bc8439b3ba~tplv-k3u1fbpfcp-zoom-1.image)


Matrix给的卡顿标准：

![](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/3e52ff90fe76495fa656d02e96a123fc~tplv-k3u1fbpfcp-zoom-1.image)

总之，相比1s平均FPS，瞬时掉帧程度的严重性更能反应界面流畅程度，因此FPS监测的重点是侦测瞬时掉帧程度。在应用中，FPS对动画及列表意义较大，**监测开始的时机**放在界面启动并展示第一帧之后，这样就能跟启动完美衔接起来，

        // 帧率不统计第一帧
        @Override
        public void onActivityResumed(@NonNull final Activity activity) {
            super.onActivityResumed(activity);
            activity.getWindow().getDecorView().getViewTreeObserver().addOnWindowFocusChangeListener(new ViewTreeObserver.OnWindowFocusChangeListener() {
                @Override
                public void onWindowFocusChanged(boolean b) {
                    if (b) {
                    <!--界面可见后，开始侦测FPS-->
                        resumeTrack();
                        activity.getWindow().getDecorView().getViewTreeObserver().removeOnWindowFocusChangeListener(this);
   		...
      }

侦测停止的时机也比较简单在onActivityPaused：界面失去焦点，无法与用户交互的时候

        @Override
        public void onActivityPaused(@NonNull Activity activity) {
            super.onActivityPaused(activity);
            pauseTrack(activity.getApplication());
        }

如何侦测瞬时FPS？有两种常用方式

* 360 ArgusAPM类实现方式：  监测Choreographer两次Vsync时间差 
* BlockCanary的实现方式：监测UI线程单条Message执行时间

360的实现依赖Choreographer VSYNC回调，具体实现如下：循环添加Choreographer.FrameCallback

	Choreographer.getInstance().postFrameCallback(new Choreographer.FrameCallback() {
	
		@Override
		    public void doFrame(long frameTimeNanos) {
		        mFpsCount++;
		        mFrameTimeNanos = frameTimeNanos;
		        if (isCanWork()) {
		            //注册下一帧回调
		            Choreographer.getInstance().postFrameCallback(this);
		        } else {
		            mCurrentCount = 0;
		        }
		    }
	});

这种监听有个问题就是，监听过于频繁，因为在无需界面刷新的时候Choreographer.FrameCallback还是不断循环执行，浪费CPU资源，对线上运行采集并不友好，相比之下BlockCanary的监听单个Message执行要友善的多，而且同样能够涵盖UI绘制耗时、两帧之间的耗时，额外执行负担较低，也是本文采取的策略，核心实现参照Matrix：

* 监听Message执行耗时
* 通过反射循环添加Choreographer.FrameCallback区分doFrame耗时

为Looper设置一个LooperPrinter，根据回传信息头区分消息执行开始于结束，计算Message耗时：原理如下

		  public static void loop() {
		            ...
		            if (logging != null) {
		                logging.println(">>>>> Dispatching to " + msg.target + " " +
		                        msg.callback + ": " + msg.what);
		            }
		             ...
		            if (logging != null) {
		                logging.println("<<<<< Finished to " + msg.target + " " + msg.callback);
		            }
		            
自定义LooperPrinter如下：
     
     	    class LooperPrinter implements Printer {
	        
	        @Override
	        public void println(String x) {
	           ...
	            if (isValid) {
	            <!--区分开始结束，计算消息耗时-->
	                dispatch(x.charAt(0) == '>', x);
	        }

利用回调参数">>>>"与"<<<"的 区别即可诊断出Message执行耗时，从而确定是否导致掉帧。以上实现针对所有UI Message，原则上UI线程所有的消息都应该保持轻量级，任何消息超时都应当算作异常行为，所以，直接拿来做掉帧监测没特大问题的。但是，有些特殊情况可能对FPS计算有一些误判，比如，在touch时间里往UI线程塞了很多消息，单条一般不会影响滚动，但多条聚合可能会带来影响，如果没跳消息执行时间很短，这种方式就可能统计不到，当然这种业务的写法本身就存在问题，所以先不考虑这种场景。

Choreographer有个方法addCallbackLocked，通过这个方法添加的任务会被加入到VSYNC回调，会跟Input、动画、UI绘制一起执行，因此可以用来作为鉴别是否是UI重绘的Message，看看是不是重绘或者触摸事件导致的卡顿掉帧。Choreographer源码如下：

        @UnsupportedAppUsage
        public void addCallbackLocked(long dueTime, Object action, Object token) {
            CallbackRecord callback = obtainCallbackLocked(dueTime, action, token);
            CallbackRecord entry = mHead;
            if (entry == null) {
                mHead = callback;
                return;
            }
            if (dueTime < entry.dueTime) {
                callback.next = entry;
                mHead = callback;
                return;
            }
            while (entry.next != null) {
                if (dueTime < entry.next.dueTime) {
                    callback.next = entry.next;
                    break;
                }
                entry = entry.next;
            }
            entry.next = callback;
        }

该方法不为外部可见，因此需要通过反射获取，
        
    private synchronized void addFrameCallback(int type, Runnable callback, boolean isAddHeader) {

        try {
        	 <!--反射获取方法-->
                addInputQueue = reflectChoreographerMethod(0 “addCallbackLocked”, long.class, Object.class, Object.class);
              <!--添加回调-->
                if (null != method) {
                    method.invoke(callbackQueues[type], !isAddHeader ? SystemClock.uptimeMillis() : -1, callback, null);
                }

 然后在每次执行结束后，重新将callback添加回Choreographer的Queue，监听下一次UI绘制。

    @Override
    public void dispatchEnd() {
        super.dispatchEnd();
        if (mStartTime > 0) {
            long cost = SystemClock.uptimeMillis() - mStartTime;
            <!--计算耗时-->
            collectInfoAndDispatch(ActivityStack.getInstance().getTopActivity(), cost, mInDoFrame);
            if (mInDoFrame) {
            <!--监听下一次UI绘制-->
                addFrameCallBack();
                mInDoFrame = false;
            }
        }
    }
    
这样就能检测到每次Message执行的时间，它可以直接用来计算**瞬时帧率**，

	瞬时掉帧程度 = Message耗时/16 -1 （不足1 可看做1）

瞬时掉帧小于2次可以认为没有发生抖动，如果出现了单个Message执行过长，可认为发生了掉帧，流畅度与瞬时帧率监测大概就是这样。不过，同启动耗时类似，不同配置结果不同，不能用绝对时间衡量，只能横向对比，简单线上效果如下：


![](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/50cc555e0b5b48958fdadc9d3e850b33~tplv-k3u1fbpfcp-zoom-1.image)

![](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/9f4edc35fc884637b6ae4de792bf72f3~tplv-k3u1fbpfcp-zoom-1.image)



### 内存泄露及内存使用侦测

内存泄露有个比较出名的库LeakCanary，实现原理也比较清晰，就是利用弱引用+ReferenceQueue，其实只用弱引用也可以做，ReferenceQueue只是个辅助作用，LeakCanary除了泄露检测还有个堆栈Dump的功能，虽然很好，但是这个功能并不适合线上，而且，只要能监听到Activity泄露，本地分析原因是比较快的，没必要将堆栈Dump出来。因此，本文只实现Activity泄露监测能力，不在线上分析原因。而且，参考LeakCanary，改用一个WeakHashMap实现上述功能，不在主动暴露ReferenceQueue这个对象。WeakHashMap最大的特点是其key对象被自动弱引用，可以被回收，利用这个特点，用其key监听Activity回收就能达到泄露监测的目的。核心实现如下：

       application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
	
	        @Override
	        public void onActivityDestroyed(@NonNull Activity activity) {
	            super.onActivityDestroyed(activity);
	            <!--放入map，进行监听-->
	            mActivityStringWeakHashMap.put(activity, activity.getClass().getSimpleName());
	        }

	        @Override
	        public void onActivityStopped(@NonNull final Activity activity) {
	            super.onActivityStopped(activity);
	            //  退后台，GC 找LeakActivity
	            if (!ActivityStack.getInstance().isInBackGround()) {
	                return;
	            }
	            Runtime.getRuntime().gc();
	            mHandler.postDelayed(new Runnable() {
	                @Override
	                public void run() {
	                    try {
	                        if (!ActivityStack.getInstance().isInBackGround()) {
	                            return;
	                        }
	                        try {
	                            //   申请个稍微大的对象，促进GC
	                            byte[] leakHelpBytes = new byte[4 * 1024 * 1024];
	                            for (int i = 0; i < leakHelpBytes.length; i += 1024) {
	                                leakHelpBytes[i] = 1;
	                            }
	                        } catch (Throwable ignored) {
	                        }
	                        Runtime.getRuntime().gc();
	                        SystemClock.sleep(100);
	                        System.runFinalization();
	                        HashMap<String, Integer> hashMap = new HashMap<>();
	                        for (Map.Entry<Activity, String> activityStringEntry : mActivityStringWeakHashMap.entrySet()) {
	                            String name = activityStringEntry.getKey().getClass().getName();
	                            Integer value = hashMap.get(name);
	                            if (value == null) {
	                                hashMap.put(name, 1);
	                            } else {
	                                hashMap.put(name, value + 1);
	                            }
	                        }
	                        if (mMemoryListeners.size() > 0) {
	                            for (Map.Entry<String, Integer> entry : hashMap.entrySet()) {
	                                for (ITrackMemoryListener listener : mMemoryListeners) {
	                                    listener.onLeakActivity(entry.getKey(), entry.getValue());
	                                }
	                            }
	                        }
	                    } catch (Exception ignored) {
	                    }
	                }
	            }, 10000);
	        }
        
线上选择监测没必要实时，将其延后到APP进入后台的时候，在APP进入后台之后主动触发一次GC，然后延时10s，进行检查，之所以延时10s，是因为GC不是同步的，为了让GC操作能够顺利执行完，这里选择10s后检查。在检查前分配一个4M的大内存块，再次确保GC执行，之后就可以根据WeakHashMap的特性，查找有多少Activity还保留在其中，这些Activity就是泄露Activity。

> 关于内存检测

内存检测比较简单，弄清几个关键的指标就行，这些指标都能通过 Debug.MemoryInfo获取

	        Debug.MemoryInfo debugMemoryInfo = new Debug.MemoryInfo();
	        Debug.getMemoryInfo(debugMemoryInfo);
	        appMemory.nativePss = debugMemoryInfo.nativePss >> 10;
	        appMemory.dalvikPss = debugMemoryInfo.dalvikPss >> 10;
	        appMemory.totalPss = debugMemoryInfo.getTotalPss() >> 10;

这里关心三个就行，

* TotalPss（整体内存，native+dalvik+共享）
* nativePss （native内存）
* dalvikPss （java内存 OOM原因）

一般而言total是大于nativ+dalvik的，因为它包含了共享内存，理论上我们只关心native跟dalvik就行，以上就是关于内存的监测能力，不过内存泄露不是100%正确，暴露明显问题即可，效果如下：

![](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/1874ed2feba4466abb317a492dbe50c3~tplv-k3u1fbpfcp-zoom-1.image)


### 流量监测

流量监测的实现相对简单，利用系统提供的TrafficStats.getUidRxBytes方法，配合Actvity生命周期，即可获取每个Activity的流量消耗。具体做法：在Activity start的时候记录起点，在pause的时候累加，最后在Destroyed的时候统计整个Activity的流量消耗，如果想要做到Fragment维度，就要具体业务具体分析了，简单实现如下

       application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {

	        @Override
	        public void onActivityStarted(@NonNull Activity activity) {
	            super.onActivityStarted(activity);
	            <!--开始记录-->
	            markActivityStart(activity);
	        }
	
	        @Override
	        public void onActivityPaused(@NonNull Activity activity) {
	            super.onActivityPaused(activity);
	            <!--累加-->
	            markActivityPause(activity);
	        }
			
	        @Override
	        public void onActivityDestroyed(@NonNull Activity activity) {
	            super.onActivityDestroyed(activity);
	            <!--统计结果，并通知回调-->
	            markActivityDestroy(activity);
	        }
	    };


![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/ba7e92e31be64c39b0244ddce2fdf1da~tplv-k3u1fbpfcp-zoom-1.image)


![](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/46c18c99588d45af9659edca8158691e~tplv-k3u1fbpfcp-zoom-1.image)



### 电量检测

Android电量状态能通过一下方法实时获取，只是对于分析来说有点麻烦，需要根据不同手机、不同配置做聚合，单处采集很简单

	            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
	            android.content.Intent batteryStatus = application.registerReceiver(null, filter);
	            int status = batteryStatus.getIntExtra("status", 0);
	            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
	                    status == BatteryManager.BATTERY_STATUS_FULL;
	            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

不过并不能获取绝对电量，只能看百分比，因为对单个Activity来做电量监测并不靠谱，往往都是0，可以在APP推到后台后，对真个在线时长的电池消耗做监测，这个可能还能看出一些电量变化。


### CPU使用监测

没想好怎么弄，显不出力

## 数据整合与基线制定

APP端只是完成的数据的采集，数据的整合及根系还是要依赖后台数据分析，根据不同配置，不同场景才能制定一套比较合理的基线，而且，这种**基线肯定不是绝对**的，只能是相对的，这套基线将来可以作为页面性能评估标准，对Android而言，挺难，机型太多。

## 总结

* 启动有相对靠谱节点
* 瞬时FPS（瞬时掉帧程度）意义更大
* 内存泄露可以一个WeakHashMap简单搞定
* 电量及CPU还不知道怎么用

 [GITHUB链接  Collie ](https://github.com/happylishang/Collie)
