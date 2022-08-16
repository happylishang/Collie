package com.snail.labaffinity.activity;

import android.os.Bundle;
import android.os.SystemClock;

import com.snail.labaffinity.app.MyButton;
import com.snail.labaffinity.databinding.ActivitySecondBinding;
import com.snail.labaffinity.utils.LogUtils;


public class LauncherTestActivity extends BaseActivity {

    private int count;
    private ActivitySecondBinding mActivitySecondBinding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyButton textView = new MyButton(this);
        textView.setText("LauncherTestActivity");
        setContentView(textView);
    }

    @Override
    protected void onResume() {
        super.onResume();

//      Debug.stopMethodTracing();


//        SystemClock.sleep(2000);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public void onAttachedToWindow() {
//        绘制之前
        super.onAttachedToWindow();
//        SystemClock.sleep(1000);
    }
}
