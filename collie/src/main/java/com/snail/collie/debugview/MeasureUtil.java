package com.snail.collie.debugview;

import android.util.Pair;
import android.view.View;

/**
 * Created by zyl06 on 1/13/16.
 */
public class MeasureUtil {
    private static final int MODE_SHIFT = 30;
    private static final int MODE_MASK = 0x3 << MODE_SHIFT;
    private static final int UNSPECIFIED = 0 << MODE_SHIFT;
    public static final int EXACTLY = 1 << MODE_SHIFT;
    public static final int AT_MOST = 2 << MODE_SHIFT;

    public static final int WRAP_CONTENT = AT_MOST | (~MODE_MASK);

    public static Pair<Integer, Integer> getMeasuredSize(View view) {
        if (view == null) return null;

        view.measure(WRAP_CONTENT, WRAP_CONTENT);
        return new Pair<>(view.getMeasuredWidth(), view.getMeasuredHeight());
    }

    public static int getMeasuredWidth(View view, int height) {
        view.measure(WRAP_CONTENT, View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
        return view.getMeasuredWidth();
    }

    public static int getMeasuredHeight(View view, int width) {
        view.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY), WRAP_CONTENT);
        return view.getMeasuredHeight();
    }
}
