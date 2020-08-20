package com.snail.collie.debug;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FloatingFpsView extends FrameLayout {

    private RecyclerView mRecyclerView;
    private List<String> mStringList = new ArrayList<>();
    RecyclerView.Adapter mAdapter = new RecyclerView.Adapter() {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            TextView textView = new TextView(getContext());
            textView.setTextSize(12);
            textView.setPadding(8,16,0,0);
            return new RecyclerView.ViewHolder(textView) {
                @Override
                public String toString() {
                    return super.toString();
                }
            };
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ((TextView) holder.itemView).setText(mStringList.get(position));
        }

        @Override
        public int getItemCount() {
            return mStringList.size();
        }
    };

    public FloatingFpsView(@NonNull Context context) {
        this(context, null);
    }

    public FloatingFpsView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatingFpsView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setBackgroundColor(0x33000000);
        mRecyclerView = new RecyclerView(getContext());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);
        LayoutParams params = new LayoutParams(250, 500);
        addView(mRecyclerView, params);
    }

    public void update(String content) {
        mStringList.add(content);
        mAdapter.notifyItemInserted(mStringList.size() - 1);
        mRecyclerView.scrollToPosition(mStringList.size()-1);
    }
}
