package com.snail.collie.debug

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import java.lang.IllegalArgumentException

/**
 * 悬浮窗，需权限
 *
 * @author hejiangjie on 2020/5/19
 */
class FloatHelper(private val mContext: Context) {
    private var mWindow: SimpleFloatWindow? = null
    private var mChild: View? = null
    private var mInitWidth = WindowManager.LayoutParams.WRAP_CONTENT
    private var mInitHeight = WindowManager.LayoutParams.WRAP_CONTENT
    private var mGravity = Gravity.NO_GRAVITY
    private var mInitX = 0
    private var mInitY = 0
    private var mNeedReload = false
    private var mAlignSide = false
    private val isMove = false
    private val mPermissionDialog: AlertDialog? = null
    fun setAlignSide(alignSide: Boolean): FloatHelper {
        mAlignSide = alignSide
        return this
    }

    fun setSize(width: Int, height: Int): FloatHelper {
        mInitWidth = width
        mInitHeight = height
        mNeedReload = true
        return this
    }

    fun setInitPosition(x: Int, y: Int): FloatHelper {
        mGravity = Gravity.START or Gravity.TOP
        mInitX = x
        mInitY = y
        mNeedReload = true
        return this
    }

    fun setView(view: View): FloatHelper {
        if (mChild !== view) {
            mChild = view
            mNeedReload = true
        }
        return this
    }

    /**
     * 显示悬浮窗(需先保证已有权限)
     *
     * @return
     */
    fun show(activity: Activity?): Boolean {
        if (!hasOverlayPermission(mContext)) {
            showTips(activity)
            return false
        }
        if (isShowing) {
            return true
        }
        checkSetupWindow()
        if (mWindow != null) {
            mWindow!!.open()
        }
        return true
    }

    private fun showTips(activity: Activity?) {
        AlertDialog.Builder(activity)
            .setMessage("您需要打开悬浮窗权限") //可以直接设置这三种button
            .setPositiveButton("确定") { dialog, which ->
                requestOverlayPermission(activity, PERMISSIONS_REQUEST_OVERLAY)
                dialog.dismiss()
            }
            .setNegativeButton("取消") { dialog, which -> dialog.dismiss() }
            .create().show()
    }

    /**
     * 关闭悬浮窗
     */
    fun close() {
        if (mWindow != null) {
            mWindow!!.close()
            mWindow = null
        }
    }

    /**
     * 是否存在悬浮窗（显示/最小化）
     *
     * @return
     */
    val isShowing: Boolean
        get() = mWindow != null && mWindow!!.isOpen

    /**
     * 资源回收清理
     */
    fun destroy() {
        close()
        mChild = null
    }

    private fun createFloatWindow(context: Context): SimpleFloatWindow {
        return SimpleFloatWindow(context)
    }

    private fun checkSetupWindow() {
        if (mWindow == null || mNeedReload) {
            if (mWindow != null) {
                mWindow!!.close()
            }
            mWindow = createFloatWindow(mContext)
            mWindow!!.loadView(mChild)
        }
    }

    private inner class SimpleFloatWindow(context: Context) : FrameLayout(context) {
        private var mWindowManager: WindowManager? = null
        private var mLayoutParams: WindowManager.LayoutParams? = null
        private val mMinimized = false
        private val mLastFloatX = 0
        private val mLastFloatY = 0
        private val downX = 0f
        private val downY = 0f
        private val moveX = 0f
        private val moveY = 0f
        var isOpen: Boolean
            private set
        private val origDownX = 0f
        private val origDownY = 0f
        private fun initFloatWindowParams(context: Context) {
            mWindowManager =
                context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            mLayoutParams = WindowManager.LayoutParams()
            mLayoutParams!!.packageName = context.packageName
            mLayoutParams!!.width = mInitWidth
            mLayoutParams!!.height = mInitHeight
            mLayoutParams!!.flags = (WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mLayoutParams!!.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                mLayoutParams!!.type = WindowManager.LayoutParams.TYPE_PHONE
            }
            mLayoutParams!!.format = PixelFormat.RGBA_8888
            mLayoutParams!!.gravity = mGravity
            mLayoutParams!!.x = mInitX
            mLayoutParams!!.y = mInitY
        }

        fun loadView(view: View?) {
            removeAllViews()
            this.addView(
                view,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        }

        fun open() {
            try {
                mWindowManager!!.updateViewLayout(this, mLayoutParams)
            } catch (e: IllegalArgumentException) {
                mWindowManager!!.addView(this, mLayoutParams)
            }
            isOpen = true
        }

        fun close() {
            if (mWindowManager != null && isShowing) {
                mWindowManager!!.removeView(this)
                removeAllViews()
            }
            isOpen = false
        }

        init {
            initFloatWindowParams(context)
            mNeedReload = false
            isOpen = false
        }
    }

    companion object {
        private const val TAG = "FloatHelper"
        const val PERMISSIONS_REQUEST_OVERLAY = 1231

        /**
         * 判断悬浮窗动态权限
         *
         * @param context
         * @return
         */
        fun hasOverlayPermission(context: Context?): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else true
        }

        /**
         * 请求悬浮窗权限
         *
         * @param activity
         * @param reqCode
         * @return
         */
        fun requestOverlayPermission(activity: Activity?, reqCode: Int): Boolean {
            if (activity == null || hasOverlayPermission(activity)) {
                return false
            }
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + activity.packageName)
            )
            activity.startActivityForResult(intent, 1000)
            return true
        }
    }
}