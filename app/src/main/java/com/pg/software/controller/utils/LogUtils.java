package com.pg.software.controller.utils;

import android.util.Log;

/**
 * Created by Freedom on 2017/10/24.
 */
public class LogUtils {
    private static final boolean open = true;

    public static void e(String tag, String msg) {
        if (open) {
            Log.e(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (open) {
            Log.i(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (open) {
            Log.d(tag, msg);
        }
    }

}
