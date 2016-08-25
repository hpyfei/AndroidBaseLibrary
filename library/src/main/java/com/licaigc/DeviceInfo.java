package com.licaigc;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

/**
 * Created by walfud on 2016/7/7.
 */
public class DeviceInfo {
    public static final String TAG = "DeviceInfo";

    /**
     * @return 返回 "192.168.1.1" 或者 "3FFE:FFFF:0:CD30:0:0:0:0". 获取不到则返回 `null`
     */
    public static String getIpAddress() {
        if (!PermissionUtils.hasPermission("android.permission.INTERNET")) {
            return null;
        }

        try {
            List<NetworkInterface> networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface networkInterface : networkInterfaces) {
                List<InetAddress> inetAddresses = Collections.list(networkInterface.getInetAddresses());
                for (InetAddress inetAddress : inetAddresses) {
                    if (!inetAddress.isLoopbackAddress()) {
                        String hostAddress = inetAddress.getHostAddress();

                        if (!hostAddress.contains(":")) {
                            // Ipv4, just use it
                        } else {
                            // Ipv6
                            // Handles such as 'fe80::7c0e:c6ff:fe8d:1e34%dummy0'
                            int pos = hostAddress.indexOf('%');
                            if (pos != -1) {
                                hostAddress = hostAddress.substring(0, pos);
                            }
                        }
                        return hostAddress;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * @return 失败返回 null
     * TODO: 此方法在 6.0 上会获取不到正确信息. 目前还没有太好方法
     */
    public static String getMacAddress() {
        if (!PermissionUtils.hasPermission("android.permission.ACCESS_WIFI_STATE")) {
            return null;
        }

        WifiManager wifiManager = (WifiManager) AndroidBaseLibrary.getContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            WifiInfo wInfo = wifiManager.getConnectionInfo();
            if (wInfo != null) {
                return wInfo.getMacAddress();
            }
        }

        return null;
    }

    /**
     * @return 失败返回 null
     */
    public static String getImei() {
        if (!PermissionUtils.hasPermission("android.permission.READ_PHONE_STATE")) {
            return null;
        }

        TelephonyManager telephonyManager = (TelephonyManager) AndroidBaseLibrary.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            return telephonyManager.getDeviceId();
        }
        return null;
    }

    public static String getAndroidId() {
        return Settings.Secure.getString(AndroidBaseLibrary.getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}
