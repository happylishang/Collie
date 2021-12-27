package com.snail.collie.debug

import android.util.Pair
import android.view.View

/**
 * Created by zyl06 on 1/13/16.
 */
object MeasureUtil {
    private const val MODE_SHIFT = 30
    private const val MODE_MASK = 0x3 shl MODE_SHIFT
    private const val UNSPECIFIED = 0 shl MODE_SHIFT
    const val EXACTLY = 1 shl MODE_SHIFT
    const val AT_MOST = 2 shl MODE_SHIFT
    const val WRAP_CONTENT = AT_MOST or MODE_MASK.inv()
    fun getMeasuredSize(view: View?): Pair<Int, Int>? {
        if (view == null) return null
        view.measure(WRAP_CONTENT, WRAP_CONTENT)
        return Pair(view.measuredWidth, view.measuredHeight)
    }

    fun getMeasuredWidth(view: View, height: Int): Int {
        view.measure(
            WRAP_CONTENT,
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
        )
        return view.measuredWidth
    }

    fun getMeasuredHeight(view: View, width: Int): Int {
        view.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            WRAP_CONTENT
        )
        return view.measuredHeight
    }
}