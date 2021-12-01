package com.snail.labaffinity.app

import android.content.Context
import android.graphics.Canvas
import android.os.SystemClock
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.snail.labaffinity.utils.LogUtils

class MyButton constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
    AppCompatTextView(
        context,
        attrs,
        defStyleAttr
    ) {
    constructor(context: Context?) : this(context, null) {}
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0) {}


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        SystemClock.sleep(1000)
        LogUtils.v("onDraw")
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        LogUtils.v("onAttachedToWindow")
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        LogUtils.v("onWindowFocusChanged")
    }
}