package com.snail.labaffinity.activity;

import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.SystemClock;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Debug.stopMethodTracing();

            }
        },10000);
//        SystemClock.sleep(2000);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
//        SystemClock.sleep(1000);
    }
}
