package com.snail.labaffinity.activity;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.snail.labaffinity.databinding.ActivitySecondBinding;
import com.snail.labaffinity.utils.LogUtils;


public class LauncherTestActivity extends BaseActivity {

    private int count;
    private ActivitySecondBinding mActivitySecondBinding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SystemClock.sleep(1000);
        TextView textView = new TextView(this);
        textView.setText("LauncherTestActivity");
        setContentView(textView);
    }


}
