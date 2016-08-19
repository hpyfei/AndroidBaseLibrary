package com.licaigc.trace;

import android.os.Build;

import com.licaigc.Constants;
import com.licaigc.DeviceInfo;
import com.licaigc.ManifestUtils;
import com.licaigc.PackageUtils;
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
    public static void startupInfo() {
        // TODO: 这里只上传基本信息么?
        Map<String, String> params = getBasicInfo();
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
                "site", String.valueOf(Constants.APP_ID)
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
