package com.licaigc;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

/**
 * Created by walfud on 2016/7/8.
 */
public class ManifestUtils {
    public static final String TAG = "ManifestUtils";

    public static String getMeta(String key) {
        Context appContext = AndroidBaseLibrary.getContext();
        PackageManager packageManager = appContext.getPackageManager();
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(appContext.getPackageName(), PackageManager.GET_META_DATA);
            if (applicationInfo.metaData != null) {
                return applicationInfo.metaData.getString(key);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
}
