package com.licaigc.trace;

import android.os.Build;

import com.licaigc.Constants;
import com.licaigc.DeviceInfo;
import com.licaigc.ManifestUtils;
import com.licaigc.PackageUtils;
import com.licaigc.TraceAction;
import com.licaigc.TransactionStatus;
import com.licaigc.Transformer;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 统计, 打点相关
 * Created by walfud on 2016/8/19.
 */
public class Trace {
    public static final String TAG = "Trace";

    private static final String URL = "http://c.lcgc.pub/r/app";

    // Function
    public static void onActivate() {
        // TODO: 这里只上传基本信息么?
        Map<String, String> params = getBasicInfo();
        request(params);
    }

    public  static  void  onLogin(String userId) {
        if (userId == null) {
            return;
        }

        Map<String, String> params = getBasicInfo();
        params.put("action", TraceAction.LOGIN.toString());
        params.put("role",userId);
        request(params);
    }

    public  static  void  onRegist(String userId) {
        if (userId == null) {
            return;
        }

        Map<String, String> params = getBasicInfo();
        params.put("action", TraceAction.REGIST.toString());
        params.put("role",userId);
        request(params);
    }

    public  static  void  onLogout(String userId) {
        if (userId == null) {
            return;
        }

        Map<String, String> params = getBasicInfo();
        params.put("action", TraceAction.REGIST.toString());
        params.put("role",userId);
        request(params);
    }

    public static  void  onPurchase(String userId,String productId,double shares, double cost,TransactionStatus status) {
        if (userId == null || productId == null) {
            return;
        }

        Map<String, String> params = getBasicInfo();
        params.put("action", TraceAction.PURCHASE.toString());
        params.put("role",userId);
        params.put("target",productId);
        params.put("shares",String.valueOf(shares));
        params.put("cost",String.valueOf(cost));
        params.put("status",String.valueOf(status));

        request(params);
    }


    public static  void  onRedeem(String userId,String productId,double shares, double cost,TransactionStatus status) {
        if (userId == null || productId == null) {
            return;
        }

        Map<String, String> params = getBasicInfo();
        params.put("action", TraceAction.REDEEM.toString());
        params.put("role",userId);
        params.put("target",productId);
        params.put("shares",String.valueOf(shares));
        params.put("cost",String.valueOf(cost));
        params.put("status",String.valueOf(status));
        request(params);
    }

    // internal

    /**
     *
     * @return
     */
    private static Map<String, String> getBasicInfo() {
        // TODO: 新家了几个参数, 应该补充道
        return Transformer.asMap(
                "os", String.valueOf(Constants.OS_ANDROID),
                "osversion", Build.VERSION.RELEASE,
                "mac", DeviceInfo.getMacAddress(),
                "imei", DeviceInfo.getImei(),
                "androidid", DeviceInfo.getAndroidId(),
                "model", Build.MANUFACTURER,
                "appversion", PackageUtils.getVersionName(),
                "buildcode", String.valueOf(PackageUtils.getVersionCode()),
                "channel", ManifestUtils.getMeta("UMENG_CHANNEL"),
                "ip", DeviceInfo.getIpAddress(),
                "site", String.valueOf(Constants.APP_ID),
                "lbs","",
                "network","",
                "osname","",
                "timestamp",""

        );
    }

    private static void request(Map<String, String> params) {
        final String param = Transformer.map2HttpGetParam(params, true, Transformer.MAP2HTTPGETPARAM_SKIP_EMPTY);
        Observable.<Void>just(null)
                .observeOn(Schedulers.io())
                .map(new Func1<Void, Void>() {
                    @Override
                    public Void call(Void aVoid) {
                        HttpURLConnection urlConnection = null;
                        try {
                            urlConnection = (HttpURLConnection) new URL(String.format("%s/?%s", URL, param)).openConnection();
                            urlConnection.setRequestMethod("GET");
                            urlConnection.getResponseCode();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if (urlConnection != null) {
                                urlConnection.disconnect();
                            }
                        }
                        return null;
                    }
                })
                .subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Void aVoid) {

                    }
                });
    }
}
