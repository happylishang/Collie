package com.snail.collie.debugview;

import android.content.Context;
import android.os.Handler;

import com.snail.collie.BuildConfig;
import com.snail.collie.core.ActivityStack;
import com.snail.collie.core.CollieHandlerThread;

/**
 * 掉帧检测
 */
public class DebugHelper {

    private static volatile DebugHelper sInstance = null;

    private FloatingFpsView mDebugCollieView;
    private Handler mHandler;
    private FloatHelper mFloatHelper;

    private DebugHelper() {
        mHandler = new Handler(CollieHandlerThread.getInstance().getHandlerThread().getLooper());
    }

    public static DebugHelper getInstance() {
        if (sInstance == null) {
            synchronized (DebugHelper.class) {
                if (sInstance == null) {
                    sInstance = new DebugHelper();
                }
            }
        }
        return sInstance;
    }

    public void show(final Context context) {
        if (mFloatHelper != null && mFloatHelper.isShowing()) {
            return;
        }
        if (mDebugCollieView == null) {
            mDebugCollieView = new FloatingFpsView(context);
            mFloatHelper = new FloatHelper(context);
            mFloatHelper.setAlignSide(false)
                    .setInitPosition(context.getResources().getDisplayMetrics().widthPixels - MeasureUtil.getMeasuredWidth(mDebugCollieView, 0), 80);

        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mFloatHelper.setView(mDebugCollieView)
                        .show(ActivityStack.getInstance().getTopActivity());
            }
        });
    }

    public void hide() {
        if (BuildConfig.DEBUG) {
            if (mFloatHelper != null) {
                mFloatHelper.destroy();
            }
        }
    }

    public void update(final String content) {

        if (mFloatHelper != null && mFloatHelper.isShowing()) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mDebugCollieView.update(content);
                }
            });
        }
    }
}
