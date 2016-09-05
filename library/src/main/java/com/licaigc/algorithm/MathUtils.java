package com.licaigc.algorithm;

/**
 * Created by walfud on 2015/11/18.
 */
public class MathUtils {

    public static final String TAG = "MathUtils";

    public static int min(int... values) {
        int minValue = 0;
        for (int i : values) {
            if (i < minValue) {
                minValue = i;
            }
        }

        return minValue;
    }

    public static double min(double... values) {
        double minValue = 0;
        for (double d : values) {
            if (d < minValue) {
                minValue = d;
            }
        }

        return minValue;
    }

    /**
     * Make return value in [min, max]
     * @param min
     * @param t
     * @param max
     * @param <T>
     * @return
     */
    public static <T extends Comparable<T>> T between(T min, T t, T max) {
        if (t.compareTo(min) < 0) {
            return min;
        }
        if (t.compareTo(max) > 0) {
            return max;
        }

        return t;
    }
}
