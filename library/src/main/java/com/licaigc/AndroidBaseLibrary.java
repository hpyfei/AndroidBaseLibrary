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
     * App Id
     */
    private static int sAppId;

    /**
     * Primary Color
     */
    private static int sPrimaryColor;

    /**
     *
     * @param context
     * @return
     */
    public static final boolean initialize(Context context) {
        sContext = context.getApplicationContext();

        String pkgName = AndroidBaseLibrary.getContext().getPackageName();

        if (false) {
            // Stub
        } else if (Constants.PKG_TALICAI.equals(pkgName)) {
            sAppId = Constants.APP_ID_TALICAI;
            sPrimaryColor = Constants.APP_PRIMARY_COLOR_TALICAI;
        } else if (Constants.PKG_GUIHUA.equals(pkgName)) {
            sAppId = Constants.APP_ID_GUIHUA;
            sPrimaryColor = Constants.APP_PRIMARY_COLOR_GUIHUA;
        } else if (Constants.PKG_TIMI.equals(pkgName)) {
            sAppId = Constants.APP_ID_TIMI;
            sPrimaryColor = Constants.APP_PRIMARY_COLOR_TIMI;
        } else if (Constants.PKG_JIJINDOU.equals(pkgName)) {
            sAppId = Constants.APP_ID_JIJINDOU;
            sPrimaryColor = Constants.APP_PRIMARY_COLOR_JIJINDOU;
        } else {
            sAppId = Constants.APP_ID_UNKNOWN;
            sPrimaryColor = Constants.APP_PRIMARY_COLOR_UNKNOWN;
        }

        return true;
    }

    /**
     *
     * @return Application context
     */
    public static Context getContext() {
        return sContext;
    }

    public static int getAppId() {
        return sAppId;
    }

    public static int getPrimaryColor() {
        return sPrimaryColor;
    }
}
