package com.licaigc;

import java.util.Map;

/**
 * Created by walfud on 2016/8/19.
 */
public class Constants {
    public static final String TAG = "Constants";

    // 平台
    public static final int OS_UNKNOWN = 0;
    public static final int OS_ANDROID = 1;
    public static final int OS_IOS     = 2;

    // 应用 Id
    public static final int APP_ID_UNKNOWN     = 0;
    public static final int APP_ID_TALICAI     = 1;
    public static final int APP_ID_GUIHUA      = 2;
    public static final int APP_ID_TIMI        = 3;
    public static final int APP_ID_JIJINDOU    = 4;
    public static final int APP_ID;                     // 当前应用 id

    static {
        APP_ID = getAppId();
    }

    // internal
    private static final Map<String, Integer> PKG_ID = Transformer.asMap(
            "com.talicai.talicaiclient", Constants.APP_ID_TALICAI,
            "com.haoguihua.app", Constants.APP_ID_GUIHUA,
            "com.talicai.timiclient", Constants.APP_ID_TIMI,
            "com.talicai.fund", Constants.APP_ID_JIJINDOU
    );

    /**
     * 根据当前包名返回整数 id. 如果该应用不存在于 {@link #PKG_ID} 中, 则返回 0.
     * @return
     */
    private static int getAppId() {
        String pkgName = AndroidBaseLibrary.getContext().getPackageName();
        return PKG_ID.containsKey(pkgName) ? PKG_ID.get(pkgName) : 0;
    }
}
