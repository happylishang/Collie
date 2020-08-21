package com.snail.labaffinity.activity;

import android.app.Activity;
import android.app.ListActivity;
import android.net.http.SslError;
import android.os.Bundle;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LeakTestActivity extends BaseActivity {

    public static List<Activity> sActivity=new ArrayList<>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sActivity .add(this);
    }
}
