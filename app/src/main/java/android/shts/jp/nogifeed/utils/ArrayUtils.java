package android.shts.jp.nogifeed.utils;

import java.util.List;

public class ArrayUtils {

    public static void concatenation(List in, List out) {
        for (Object o : in) {
            out.add(o);
        }
    }
}