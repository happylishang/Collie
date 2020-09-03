package com.snail.labaffinity.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.snail.labaffinity.BuildConfig;
import com.snail.labaffinity.databinding.ActivityMainBinding;
import com.tencent.bugly.crashreport.CrashReport;

public class MainActivity extends BaseActivity {

    private int count;
    ActivityMainBinding mResultProfileBinding;
    long start = SystemClock.uptimeMillis();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        mResultProfileBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mResultProfileBinding.getRoot());

        mResultProfileBinding.contentMain1.activityStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent(MainActivity.this, LauncherTestActivity.class);
                startActivity(mIntent);
            }
        });

        mResultProfileBinding.contentMain1.first.setOnClickListener(v -> {
            Intent mIntent = new Intent(MainActivity.this, LeakTestActivity.class);
            startActivity(mIntent);

        });
        mResultProfileBinding.contentMain1.second.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent(MainActivity.this, TrafficTestActivity.class);
                startActivity(mIntent);

            }
        });
        mResultProfileBinding.contentMain1.third.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("InvalidAnalyticsName")
            @Override
            public void onClick(View v) {

                Intent mIntent = new Intent(MainActivity.this, FpsTestActivity.class);
                startActivity(mIntent);
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }
}
