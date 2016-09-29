package com.licaigc;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.licaigc.trace.Track;

import java.util.HashSet;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

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
     * 极光推送初始化
     */
    private static final int MSG_JPUSH_ALIAS = 0x1000;

    private static Handler sHandler;

    /**
     * App Id
     */
    private static int sAppId;

    /**
     * Primary Color
     */
    private static int sPrimaryColor;

    /**
     *  主线程调用
     * @param context
     * @return
     */
    public static final boolean initialize(Context context) {
        sContext = context.getApplicationContext();
        sHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_JPUSH_ALIAS: {
                        String alias = Track.getRefId();
                        String tagStr = PackageUtils.getVersionName().replace(".", "_");
                        Set<String> tag = new HashSet<>();
                        tag.add(tagStr);
                        JPushInterface.setAliasAndTags(sContext, alias, tag, sAliasCallback);
                        break;
                    }

                    default:
                        break;
                }
            }
        };

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

        // JPush
        JPushInterface.init(context); // 初始化 JPush
        sHandler.sendEmptyMessage(MSG_JPUSH_ALIAS);

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

    //
    private static final TagAliasCallback sAliasCallback = new TagAliasCallback() {
        @Override
        public void gotResult(int code, String alias, Set<String> tags) {
            String logs ;
            switch (code) {
                case 0:
                    logs = "Set tag and alias success";
                    Log.i("JPush", logs);
                    // 建议这里往 SharePreference 里写一个成功设置的状态。成功设置一次后，以后不必再次设置了。
                    break;
                case 6002:
                    logs = "Failed to set alias and tags due to timeout. Try again after 60s.";
                    Log.i("JPush", logs);
                    // 延迟 60 秒来调用 Handler 设置别名
                    sHandler.sendEmptyMessageDelayed(MSG_JPUSH_ALIAS, 60 * 1000);
                    break;
                default:
                    logs = "Failed with errorCode = " + code;
                    Log.i("JPush", logs);
            }
        }
    };
}
