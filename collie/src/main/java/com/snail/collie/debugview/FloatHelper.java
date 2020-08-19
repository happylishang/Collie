package com.snail.collie.debugview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;


/**
 * 悬浮窗，需权限
 *
 * @author hejiangjie on 2020/5/19
 */
public class FloatHelper {

    private static final String TAG = "FloatHelper";
    public static final int PERMISSIONS_REQUEST_OVERLAY = 1231;

    private SimpleFloatWindow mWindow = null;
    private Context mContext;

    private View mChild;

    private int mInitWidth = WindowManager.LayoutParams.WRAP_CONTENT;
    private int mInitHeight = WindowManager.LayoutParams.WRAP_CONTENT;

    private int mGravity = Gravity.NO_GRAVITY;
    private int mInitX = 0, mInitY = 0;

    private boolean mNeedReload = false;
    private boolean mAlignSide;
    private boolean isMove = false;

    public FloatHelper setAlignSide(boolean alignSide) {
        this.mAlignSide = alignSide;
        return this;
    }

    public FloatHelper(@NonNull Context context) {
        mContext = context;
    }

    public FloatHelper setSize(int width, int height) {
        mInitWidth = width;
        mInitHeight = height;
        mNeedReload = true;
        return this;
    }

    public FloatHelper setInitPosition(int x, int y) {
        mGravity = Gravity.START| Gravity.TOP;
        mInitX = x;
        mInitY = y;
        mNeedReload = true;
        return this;
    }

    public FloatHelper setView(View view) {
        if (mChild != view) {
            mChild = view;
            mNeedReload = true;
            checkSetupWindow();
        }
        return this;
    }

    /**
     * 显示悬浮窗(需先保证已有权限)
     *
     * @return
     */
    public boolean show(Activity activity) {
        if (!hasOverlayPermission(mContext)) {

            requestOverlayPermission(activity, FloatHelper.PERMISSIONS_REQUEST_OVERLAY);
            return false;
        }
        if (isShowing()) {
            return true;
        }

        checkSetupWindow();
        if (mWindow != null) {
            mWindow.open();
        }
        return true;
    }

    /**
     * 关闭悬浮窗
     */
    public void close() {
        if (mWindow != null) {
            mWindow.close();
            mWindow = null;
        }
    }

    /**
     * 是否存在悬浮窗（显示/最小化）
     *
     * @return
     */
    public boolean isShowing() {
        return mWindow != null && mWindow.isOpen();
    }

    /**
     * 资源回收清理
     */
    public void destroy() {
        close();
        mChild = null;
    }


    /**
     * 判断悬浮窗动态权限
     *
     * @param context
     * @return
     */
    public static boolean hasOverlayPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        }

        return true;
    }

    /**
     * 请求悬浮窗权限
     *
     * @param activity
     * @param reqCode
     * @return
     */
    public static boolean requestOverlayPermission(Activity activity, int reqCode) {
        if (activity == null || hasOverlayPermission(activity)) {
            return false;
        }

        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + activity.getPackageName()));
        activity.startActivityForResult(intent, reqCode);
        return true;
    }

    private SimpleFloatWindow createFloatWindow(Context context) {
        return new SimpleFloatWindow(context);
    }

    private void checkSetupWindow() {
        if (mWindow == null || mNeedReload) {
            if (mWindow != null) {
                mWindow.close();
            }
            mWindow = createFloatWindow(mContext);
            mWindow.loadView(mChild);
        }
    }

    private class SimpleFloatWindow extends FrameLayout {
        private WindowManager mWindowManager = null;
        private WindowManager.LayoutParams mLayoutParams = null;
        private boolean mMinimized;
        private int mLastFloatX, mLastFloatY;

        private float downX, downY;
        private float moveX, moveY;

        private boolean mIsShowing;
        private float origDownX;
        private float origDownY;

        public SimpleFloatWindow(@NonNull Context context) {
            super(context);
            initFloatWindowParams(context);
            mNeedReload = false;
            mIsShowing = false;
        }

        private void initFloatWindowParams(Context context) {
            mWindowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
            mLayoutParams = new WindowManager.LayoutParams();
            mLayoutParams.packageName = context.getPackageName();
            mLayoutParams.width = mInitWidth;
            mLayoutParams.height = mInitHeight;
            mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            }
            mLayoutParams.format = PixelFormat.RGBA_8888;
            mLayoutParams.gravity = mGravity;
            mLayoutParams.x = mInitX;
            mLayoutParams.y = mInitY;
        }


        public void loadView(View view) {
            this.removeAllViews();
            this.addView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }

        public boolean isOpen() {
            return mIsShowing;
        }

        public void open() {
            try {
                mWindowManager.updateViewLayout(this, mLayoutParams);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, e.getMessage());
                mWindowManager.addView(this, mLayoutParams);
            }
            mIsShowing = true;
        }

        public void close() {
            if (mWindowManager != null) {
                mWindowManager.removeView(this);
                removeAllViews();
            }
            mIsShowing = false;
        }

        public void onMove(View view, float dx, float dy) {
            if (!mMinimized) {
                mLayoutParams.x += dx;
                mLayoutParams.y += dy;
                mWindowManager.updateViewLayout(this, mLayoutParams);
            }
        }

        public void onMinimize() {
            if (!mMinimized) {
                mMinimized = true;
                mLastFloatX = mLayoutParams.x;
                mLastFloatY = mLayoutParams.y;
                mLayoutParams.x = mLayoutParams.y = 0;
                mLayoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.START;
                mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE //使应用页面事件不被拦截（主要是键盘输入）
                        | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
                mWindowManager.updateViewLayout(this, mLayoutParams);
            }
        }

        public void onExpand() {
            if (mMinimized) {
                mLayoutParams.gravity = Gravity.NO_GRAVITY;
                mLayoutParams.x = mLastFloatX;
                mLayoutParams.y = mLastFloatY;
                mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
                mWindowManager.updateViewLayout(this, mLayoutParams);
                mMinimized = false;
            }
        }
    }

}
