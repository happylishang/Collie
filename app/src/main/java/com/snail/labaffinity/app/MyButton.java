package com.snail.labaffinity.app;

import android.content.Context;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.widget.Button;

import com.snail.labaffinity.utils.LogUtils;

public class MyButton extends androidx.appcompat.widget.AppCompatTextView {
    public MyButton(Context context) {
        super(context);
    }

    public MyButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        SystemClock.sleep(1000);
        LogUtils.v("onDraw");
    }
}
