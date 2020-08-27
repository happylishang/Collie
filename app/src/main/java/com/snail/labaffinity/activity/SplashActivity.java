package com.snail.labaffinity.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.widget.TextView;


import com.snail.labaffinity.utils.LogUtils;

import org.w3c.dom.Text;


public class SplashActivity extends BaseActivity {

    private int count;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView textView = new TextView(this);
        textView.setText("SplashActivity");
        setContentView(textView);
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));

            }
        });
        finish();
        LogUtils.v("onCreate " + SystemClock.uptimeMillis());
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogUtils.v("onStop " + SystemClock.uptimeMillis());
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtils.v("onPause " + SystemClock.uptimeMillis());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.v("onDestroy " + SystemClock.uptimeMillis());
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtils.v("onResume " + SystemClock.uptimeMillis());
    }

    //  第一帧的显示点，基本是在这里
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        LogUtils.v("onWindowFocusChanged " + SystemClock.uptimeMillis());
        super.onWindowFocusChanged(hasFocus);
    }

    protected void onPostResume() {

        super.onPostResume();
        LogUtils.v("onPostResumem" + SystemClock.uptimeMillis());

    }


}
