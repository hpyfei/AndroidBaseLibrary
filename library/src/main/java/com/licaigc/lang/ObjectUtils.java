package com.licaigc.lang;

/**
 * Created by walfud on 2015/12/18.
 */
public class ObjectUtils {

    public static final String TAG = "ObjectUtils";

    /**
     * sdk 19 后, 可以考虑使用 {@link java.util.Objects#equals(Object, Object)}
     * @param x
     * @param y
     * @return
     */
    public static boolean isEqual(Object x, Object y) {
        if (x != null) {
            return x.equals(y);
        }

        if (y != null) {
            return y.equals(x);
        }

        return true;
    }
}
