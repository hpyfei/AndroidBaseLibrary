package com.licaigc.android;

import com.licaigc.AndroidBaseLibrary;

/**
 * Created by walfud on 2015/10/3.
 */
public class DensityUtils {

    public static final String TAG = "DensityTransformer";

    public static int dp2px(double dp){
        final double scale = AndroidBaseLibrary.getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5);
    }

    public static int px2dp(double px){
        final double scale = AndroidBaseLibrary.getContext().getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5);
    }
}
