package com.snail.labaffinity.activity;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.snail.collie.Collie;
import com.snail.labaffinity.databinding.ActivitySecondBinding;
import com.snail.labaffinity.utils.LogUtils;


public class FpsTestActivity extends BaseActivity {

    private int count;
    private ActivitySecondBinding mActivitySecondBinding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Collie.getInstance().showDebugView(this);

        mActivitySecondBinding = ActivitySecondBinding.inflate(getLayoutInflater());
        setContentView(mActivitySecondBinding.getRoot());
        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(this);
//        linearLayoutManager.setItemPrefetchEnabled(false);

        mActivitySecondBinding.recy.setLayoutManager(linearLayoutManager);
        mActivitySecondBinding.recy.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new RecyclerView.ViewHolder(new TextView(FpsTestActivity.this)) {
                    {

                    }
                };
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//                LogUtils.v("位置 " + position);
                if (position > 10)
                    SystemClock.sleep(50);
                ((TextView) holder.itemView).setHeight(500);
                ((TextView) holder.itemView).setText("位置 " + position);

//                Log.d("lishang", Log.getStackTraceString(new Throwable()));

            }

            @Override
            public int getItemCount() {
                return 1000;
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        LogUtils.v("onResume");
    }
}
