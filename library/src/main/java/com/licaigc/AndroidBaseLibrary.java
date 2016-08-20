package com.licaigc;

import android.content.Context;

/**
 * Created by walfud on 2016/7/7.
 */
public class AndroidBaseLibrary {
    public static final String TAG = "AndroidBaseLibrary";

    /**
     * Should be application context
     */
    private static Context sContext;

    /**
     *
     * @param context
     * @return
     */
    public static final boolean initialize(Context context) {
        sContext = context.getApplicationContext();
        Constants.APP_ID = Constants.getAppId();

        return true;
    }

    /**
     *
     * @return Application context
     */
    public static Context getContext() {
        return sContext;
    }
}
