package com.licaigc;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.licaigc.android.PackageUtils;
import com.licaigc.lang.Transformer;
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
     * Is host app in debug mode
     */
    private static boolean sIsDebug;

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

    private static String sChannel;

    /**
     *  主线程调用
     */
    public static final boolean initialize(Context context, boolean isDebug, final String channel) {
        sContext = context.getApplicationContext();
        sIsDebug = isDebug;
        sChannel = channel;
        sHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_JPUSH_ALIAS: {
                        String alias = Track.getRefId();
                        Set<String> tag = new HashSet<>();
                        tag.add(PackageUtils.getVersionName().replace(".", "_") + (sIsDebug ? "_test" : ""));   // 版本号
                        tag.add(channel);                                                                       // 渠道
                        String pkgName = AndroidBaseLibrary.getContext().getPackageName();
                        if (Constants.PKG_TIMI.compareToIgnoreCase(pkgName) != 0
                                && PackageUtils.isTimiInstalled()) {                // 安装 timi 的用户
                            tag.add("Timi记账");
                        }
                        if (Constants.PKG_TALICAI.compareToIgnoreCase(pkgName) != 0
                                && PackageUtils.isTalicaiInstalled()) {             // 安装 她理财 的用户
                            tag.add("她理财");
                        }
                        if (Constants.PKG_GUIHUA.compareToIgnoreCase(pkgName) != 0
                                && PackageUtils.isHaoguihuaInstalled()) {           // 安装 好规划 的用户
                            tag.add("好规划");
                        }
                        if (Constants.PKG_JIJINDOU.compareToIgnoreCase(pkgName) != 0
                                && PackageUtils.isJijindouInstalled()) {            // 安装 基金豆 的用户
                            tag.add("基金豆");
                        }
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
        JPushInterface.stopCrashHandler(context);
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

    public static boolean isDebug() {
        return sIsDebug;
    }

    public static int getAppId() {
        return sAppId;
    }

    public static int getPrimaryColor() {
        return sPrimaryColor;
    }

    public static String getChannel() {
        return sChannel;
    }

    //
    private static final TagAliasCallback sAliasCallback = new TagAliasCallback() {
        @Override
        public void gotResult(int code, String alias, Set<String> tags) {
            String logs ;
            switch (code) {
                case 0:
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Set alias(");
                    if (!TextUtils.isEmpty(alias)) {
                        stringBuilder.append(alias);
                    }
                    stringBuilder.append(") and tag(");
                    String[] tagsArr = Transformer.stringCollection2Strings(tags);
                    if (tagsArr.length > 0) {
                        stringBuilder.append(tagsArr[0]);
                        for (int i = 1; i < tagsArr.length; i++) {
                            stringBuilder.append("," + tagsArr[i]);
                        }
                    }
                    stringBuilder.append(") success");
                    Log.i("JPush", stringBuilder.toString());
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
