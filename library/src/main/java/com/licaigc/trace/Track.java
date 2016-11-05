package com.licaigc.trace;

import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.licaigc.AndroidBaseLibrary;
import com.licaigc.Constants;
import com.licaigc.DeviceInfo;
import com.licaigc.ManifestUtils;
import com.licaigc.PackageUtils;
import com.licaigc.Transformer;
import com.licaigc.algorithm.Aes;
import com.licaigc.algorithm.hash.HashUtils;
import com.licaigc.datetime.TimeRange;
import com.licaigc.debug.DebugInfo;
import com.licaigc.debug.DebugUtils;
import com.licaigc.network.NetworkUtils;
import com.licaigc.rxjava.SimpleEasySubscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import rx.Subscriber;

/**
 * 统计, 打点相关
 * http://www.tuluu.com/platform/cupola/wikis/home
 * Created by walfud on 2016/8/19.
 */
public class Track {
    public static final String TAG = "Track";

    private static final String URL = "http://c.guihua.com/v1/app";
//    private static final String URL = "http://192.168.11.70:33000/v1/app";

    private static final String PREFS_TIMESTAMP = "PREFS_TIMESTAMP";

    // Function
    /**
     * 不知道是什么或者不需要上传就填 `null`
     * @param refer
     * @param userId
     */
    public static void onActivate(String refer, String userId) {
        final Map<String, Object> params = getBasicInfo();
        params.put("action", TraceAction.ACTIVATE.ordinal());
        params.put("refer", refer);
        params.put("ref_id", getRefId());

        final SharedPreferences sharedPreferences = AndroidBaseLibrary.getContext().getSharedPreferences("AndroidBaseLibrary", 0);
        if (System.currentTimeMillis() - sharedPreferences.getLong(PREFS_TIMESTAMP, 0) > 30 * TimeRange.MS_PER_D) {
            params.put("meta", getMeta(userId));
        }

        request(params, new SimpleEasySubscriber() {
            @Override
            public void onSuccess(Object o) {
                super.onSuccess(o);

                if (params.get("meta") != null) {
                    sharedPreferences.edit().putLong(PREFS_TIMESTAMP, System.currentTimeMillis()).apply();
                }
            }
        });
    }

    public static void onLogin(String userId) {
        if (userId == null) {
            return;
        }

        Map<String, Object> params = getBasicInfo();
        params.put("action", TraceAction.LOGIN.ordinal());
        params.put("role", userId);
        request(params);
    }

    public static void onRegist(String userId) {
        if (userId == null) {
            return;
        }

        Map<String, Object> params = getBasicInfo();
        params.put("action", TraceAction.REGIST.ordinal());
        params.put("role", userId);
        request(params);
    }

    public static void onLogout(String userId) {
        if (userId == null) {
            return;
        }

        Map<String, Object> params = getBasicInfo();
        params.put("action", TraceAction.LOGOUT.ordinal());
        params.put("role", userId);
        request(params);
    }

    public static void onPurchase(String userId, String productId, double shares, double cost, boolean isActionSuccess) {
        if (userId == null || productId == null) {
            return;
        }

        Map<String, Object> params = getBasicInfo();
        params.put("action", TraceAction.PURCHASE.ordinal());
        params.put("role", userId);
        params.put("target", productId);
        params.put("shares", shares);
        params.put("cost", cost);
        params.put("trade_status", isActionSuccess ? 1 : 0);
        request(params);
    }

    public static void onRedeem(String userId, String productId, double shares, double cost, boolean isActionSuccess) {
        if (userId == null || productId == null) {
            return;
        }

        Map<String, Object> params = getBasicInfo();
        params.put("action", TraceAction.REDEEM.ordinal());
        params.put("role", userId);
        params.put("target", productId);
        params.put("shares", shares);
        params.put("cost", cost);
        params.put("trade_status", isActionSuccess ? 1 : 0);
        request(params);
    }

    // internal
    private static void request(Map<String, Object> param) {
        request(param, new SimpleEasySubscriber<byte[]>());
    }
    private static void request(Map<String, Object> param, Subscriber subscriber) {
        for (Map.Entry<String, Object> kv : param.entrySet()) {
            if (kv.getValue() == null) {
                kv.setValue("");
            }
        }

        NetworkUtils.post(URL, param).subscribe(subscriber);
    }

    /**
     * @return
     */
    private static Map<String, Object> getBasicInfo() {
        return Transformer.asMap(
                "os",           Constants.OS_ANDROID,
                "osversion",    Build.VERSION.RELEASE,
                "mac",          DeviceInfo.getMacAddress(),
                "imei",         DeviceInfo.getImei(),
                "androidid",    DeviceInfo.getAndroidId(),
                "model",        Build.MANUFACTURER,
                "appversion",   PackageUtils.getVersionName(),
                "buildcode",    String.valueOf(PackageUtils.getVersionCode()),
                "channel",      ManifestUtils.getMeta("UMENG_CHANNEL"),
                "ip",           DeviceInfo.getIpAddress(),
                "site",         AndroidBaseLibrary.getAppId(),
                "lbs",          "",
                "network",      NetworkUtils.isWifiConnected() ? 0 :
                                    NetworkUtils.isMobileConnected() ? 4:
                                    1,
                "osname",       Build.BRAND,
                "timestamp",    System.currentTimeMillis()
        );
    }

    //
    /**
     * 设备硬件摘要, 用于识别一台设备
     * @return
     */
    public static String getRefId() {
        String androidId = DeviceInfo.getAndroidId();
        String imei = DeviceInfo.getImei();
        String macAddr = DeviceInfo.getMacAddress();
        return HashUtils.md5(String.format("%s/%s/%s", TextUtils.isEmpty(androidId) ? "" : androidId, TextUtils.isEmpty(imei) ? "" : imei, androidId, TextUtils.isEmpty(macAddr) ? "" : macAddr));
    }

    //
    static class Meta {
        public int site;
        public String uid;
        public String imei;
        public String androidid;
        public String mac;
        public List<App> apps;

        static class App {
            public String appname;
            public String pkg;
            public String version;
            public int versioncode;
        }
    }
    private static String getMeta(String userId) {
        DebugInfo debugInfo = DebugUtils.dump();

        Meta meta = new Meta();
        meta.site = AndroidBaseLibrary.getAppId();
        meta.uid = userId == null ? "" : userId;
        meta.imei = DeviceInfo.getImei();
        meta.androidid = DeviceInfo.getAndroidId();
        meta.mac = DeviceInfo.getMacAddress();
        meta.apps = new ArrayList<>();
        for (DebugInfo.PkgInfo pkgInfo : debugInfo.pkgInfoList) {
            if ((pkgInfo.flags & ApplicationInfo.FLAG_INSTALLED) != 0 && (pkgInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                Meta.App app = new Meta.App();
                app.appname = pkgInfo.appName;
                app.pkg = pkgInfo.pkgName;
                app.version = pkgInfo.verName;
                app.versioncode = pkgInfo.verCode;

                meta.apps.add(app);
            }
        }

        String metaStr = new Gson().toJson(meta);
        try {
            metaStr = Aes.encrypt(metaStr, "Kw8BkAETg5n4WTTK");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return metaStr;
    }
}
