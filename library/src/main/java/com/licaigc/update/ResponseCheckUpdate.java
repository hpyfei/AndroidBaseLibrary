package com.licaigc.update;

import android.graphics.Bitmap;
import android.support.annotation.Keep;

/**
 * Created by walfud on 2016/7/28.
 */
@Keep
public class ResponseCheckUpdate {
    public String message;

    public Data data;

    public transient int result;

    @Keep
    public static class Data {
        public String update;           // "none/full/delta"
        public String version;          // "2.3.4",
        public boolean force;           // false,
        public String md5;              // "c2c89ca22e083c7f3267a09eaa128585",
        public String url;              // "http://www.talicai.com/downloads",
        public String title;
        public String desc;
        public String image;

        // internal
        public transient Bitmap pic;
    }
}
