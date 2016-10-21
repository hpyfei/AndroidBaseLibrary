package com.licaigc.trace;

import android.os.Build;
import android.text.TextUtils;

import com.licaigc.AndroidBaseLibrary;
import com.licaigc.Constants;
import com.licaigc.DeviceInfo;
import com.licaigc.ManifestUtils;
import com.licaigc.PackageUtils;
import com.licaigc.Transformer;
import com.licaigc.algorithm.hash.HashUtils;
import com.licaigc.network.NetworkUtils;
import com.licaigc.rxjava.SimpleEasySubscriber;

import java.util.Map;

import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 统计, 打点相关
 * Created by walfud on 2016/8/19.
 */
public class Track {
    public static final String TAG = "Track";

    private static final String URL = "http://c.guihua.com/v1/app";
//    private static final String URL = "http://192.168.8.21:33000/v1/app";

    // Function
    public static void onActivate() {
        onActivate(null);
    }
    public static void onActivate(String refer) {
        Map<String, String> params = getBasicInfo();
        params.put("action", String.valueOf(TraceAction.ACTIVATE.ordinal()));
        params.put("refer", refer);
        params.put("ref_id", getRefId());

        request(params);
    }

    public static void onLogin(String userId) {
        if (userId == null) {
            return;
        }

        Map<String, String> params = getBasicInfo();
        params.put("action", String.valueOf(TraceAction.LOGIN.ordinal()));
        params.put("role", userId);
        request(params);
    }

    public static void onRegist(String userId) {
        if (userId == null) {
            return;
        }

        Map<String, String> params = getBasicInfo();
        params.put("action", String.valueOf(TraceAction.REGIST.ordinal()));
        params.put("role", userId);
        request(params);
    }

    public static void onLogout(String userId) {
        if (userId == null) {
            return;
        }

        Map<String, String> params = getBasicInfo();
        params.put("action", String.valueOf(TraceAction.LOGOUT.ordinal()));
        params.put("role", userId);
        request(params);
    }

    public static void onPurchase(String userId, String productId, double shares, double cost, boolean isActionSuccess) {
        if (userId == null || productId == null) {
            return;
        }

        Map<String, String> params = getBasicInfo();
        params.put("action", String.valueOf(TraceAction.PURCHASE.ordinal()));
        params.put("role", userId);
        params.put("target", productId);
        params.put("shares", String.valueOf(shares));
        params.put("cost", String.valueOf(cost));
        params.put("trade_status", isActionSuccess ? "1" : "0");
        request(params);
    }

    public static void onRedeem(String userId, String productId, double shares, double cost, boolean isActionSuccess) {
        if (userId == null || productId == null) {
            return;
        }

        Map<String, String> params = getBasicInfo();
        params.put("action", String.valueOf(TraceAction.REDEEM.ordinal()));
        params.put("role", userId);
        params.put("target", productId);
        params.put("shares", String.valueOf(shares));
        params.put("cost", String.valueOf(cost));
        params.put("trade_status", isActionSuccess ? "1" : "0");
        request(params);
    }

    // internal

    /**
     * @return
     */
    private static Map<String, String> getBasicInfo() {
        return Transformer.asMap(
                "os",           String.valueOf(Constants.OS_ANDROID),
                "osversion",    Build.VERSION.RELEASE,
                "mac",          DeviceInfo.getMacAddress(),
                "imei",         DeviceInfo.getImei(),
                "androidid",    DeviceInfo.getAndroidId(),
                "model",        Build.MANUFACTURER,
                "appversion",   PackageUtils.getVersionName(),
                "buildcode",    String.valueOf(PackageUtils.getVersionCode()),
                "channel",      ManifestUtils.getMeta("UMENG_CHANNEL"),
                "ip",           DeviceInfo.getIpAddress(),
                "site",         String.valueOf(AndroidBaseLibrary.getAppId()),
                "lbs",          "",
                "network",      NetworkUtils.isWifiConnected() ? "0" :
                                    NetworkUtils.isMobileConnected() ? "4":
                                    "1",
                "osname",       Build.BRAND,
                "timestamp",    String.valueOf(System.currentTimeMillis())
        );
    }

    private static void request(Map<String, String> params) {
        final String param = Transformer.map2HttpGetParam(params, true, Transformer.MAP2HTTPGETPARAM_KEEP_ALL);
        Observable.<Void>just(null)
                .observeOn(Schedulers.io())
                .map(new Func1<Void, Void>() {
                    @Override
                    public Void call(Void aVoid) {
                        NetworkUtils.get(String.format("%s?%s", URL, param));
                        return null;
                    }
                })
                .subscribe(new SimpleEasySubscriber<Void>());
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
}
