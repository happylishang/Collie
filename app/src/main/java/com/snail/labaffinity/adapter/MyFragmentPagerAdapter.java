package com.snail.labaffinity.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

/**
 * Author: lishang
 * Data: 17/1/5 下午2:10
 * Des:
 * version:
 */

public class MyFragmentPagerAdapter extends FragmentPagerAdapter {

    public MyFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return MyFragment.newInstance(String.valueOf(position));
    }

    @Override
    public int getCount() {
        return 10;
    }
}
