package com.snail.labaffinity.activity;

import android.os.Bundle;
import android.os.SystemClock;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.snail.labaffinity.R;
import com.snail.labaffinity.utils.LogUtils;

/**
 * Author: hzlishang
 * Data: 16/10/12 上午9:57
 * Des:
 * version:
 */
public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }


    @Override
    protected void onStop() {
        super.onStop();
        LogUtils.v("onStop " + this + SystemClock.uptimeMillis());

    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtils.v("onPause " + this + SystemClock.uptimeMillis());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.v("onDestroy " + this + SystemClock.uptimeMillis());
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtils.v("onResume " + this + SystemClock.uptimeMillis());
    }

    //  第一帧的显示点，基本是在这里
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        LogUtils.v("onWindowFocusChanged " + this + SystemClock.uptimeMillis());
        super.onWindowFocusChanged(hasFocus);
    }

    protected void onPostResume() {

        super.onPostResume();
        LogUtils.v("onPostResumem" + this + SystemClock.uptimeMillis());

    }

}
