package com.snail.labaffinity.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.snail.labaffinity.BuildConfig;
import com.snail.labaffinity.databinding.ActivityMainBinding;
import com.tencent.bugly.crashreport.CrashReport;

public class MainActivity extends BaseActivity {

    private int count;
    ActivityMainBinding mResultProfileBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mResultProfileBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mResultProfileBinding.getRoot());
        mResultProfileBinding.contentMain1.first.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CrashReport.testJavaCrash();
//                CrashReport.testANRCrash();
            }
        });
        mResultProfileBinding.contentMain1.second.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CrashReport.testNativeCrash();
            }
        });
        mResultProfileBinding.contentMain1.third.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("InvalidAnalyticsName")
            @Override
            public void onClick(View v) {


                FirebaseAnalytics.getInstance(getApplicationContext()).setUserProperty("versionCode", Integer.toString(BuildConfig.VERSION_CODE));

                Bundle bundle = new Bundle();
                bundle.putString("platform", "android");
                FirebaseAnalytics.getInstance(getApplicationContext()).logEvent("androidLaunch", bundle);

                FirebaseAnalytics.getInstance(getApplicationContext()).logEvent("user_engagement", null);
                Intent mIntent=new Intent(MainActivity.this, FpsTestActivity.class);
                startActivity(mIntent);
            }


        });

    }

}
