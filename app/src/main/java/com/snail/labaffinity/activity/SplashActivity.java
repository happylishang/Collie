package com.snail.labaffinity.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;

import com.snail.labaffinity.R;
import com.snail.labaffinity.utils.LogUtils;


public class SplashActivity extends BaseActivity {

    private int count;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        LogUtils.v("onCreate " + SystemClock.uptimeMillis());
    }

}
