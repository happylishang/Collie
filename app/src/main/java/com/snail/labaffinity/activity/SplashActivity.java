package com.snail.labaffinity.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.widget.TextView;


import com.snail.labaffinity.R;
import com.snail.labaffinity.utils.LogUtils;

import org.w3c.dom.Text;


public class SplashActivity extends BaseActivity {

    private int count;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            }
        });
        LogUtils.v("onCreate " + SystemClock.uptimeMillis());
    }


}
