package com.pg.software.controller.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Toast;

import com.xintu.xintuclick.sdk.BizMain;

import java.lang.reflect.Method;

/**
 * Created by Freedom on 2017/10/24.
 */
public class Utils {

    /**
     * 判断返回地址是否为空
     *
     * @param str
     * @return
     */
    public static boolean isNullString(String str) {
        if (TextUtils.isEmpty(str)) {
            return true;
        }
        if (str.equalsIgnoreCase("")) {
            return true;
        }
        return false;
    }

    /**
     * 防止Toast多次弹出
     */
    private static Toast mToast;

    public static void makeToast(Context mContext, String text) {
        if (mToast != null) {
            mToast.setDuration(Toast.LENGTH_LONG);
            mToast.setText(text);
            mToast.show();
        } else {
            mToast = Toast.makeText(mContext, text, Toast.LENGTH_LONG);
            mToast.show();
        }
    }

    /**
     * 获取顶部Activity名称
     *
     * @param context
     * @return
     */
    public static String getTopPackageName(Context context) {
        ActivityManager manager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        if (manager.getRunningTasks(1) == null
                || manager.getRunningTasks(1).size() == 0) {
            return null;
        }
        ActivityManager.RunningTaskInfo info = manager.getRunningTasks(1).get(0);
        return info.topActivity.getPackageName();
    }



    /**
     * 开始搜索
     */
    public static void startScan() {
        LogUtils.i("freedom", "startScan");
        BizMain.getInstance().startScan();
    }

    /**
     * 停止搜索
     */
    public static void stopScan() {
        LogUtils.i("freedom", "stopScan");
        BizMain.getInstance().stopScan();
    }

    /**
     * 取消连接
     */
    public static void cancelConn() {
        BizMain.getInstance().cancelConn();
    }

    /**
     * 断开已连接的ble设备
     */
    public static void stopConn() {
        BizMain.getInstance().stopConn();
        unInitialize();
    }
    /**
     * 在结束使用ble设备后（通常指ble设备断开连接后），需调用此方法，以确保资源被回收
     */
    public static void unInitialize() {
        BizMain.getInstance().unInitialize();
    }

    /**
     * 开始连接ble设备
     * @param text ble地址
     */
    public static void startConn(String text) {
        if(stringIsMac(text)){
            BizMain.getInstance().startConn(text);
        }
    }

    /**
     * 释放资源，防止点击出现多个按键值
     */
    public static void release(){
        BizMain.getInstance().unInitialize();
    }

    /**
     * 判断是否是蓝牙MAC地址
     * @param val 蓝牙地址
     * @return true/false
     */
    private static boolean stringIsMac(String val) {
        String trueMacAddress = "([A-Fa-f0-9]{2}:){5}[A-Fa-f0-9]{2}";
        if (val.matches(trueMacAddress)) {
            return true;
        } else {
            return false;
        }
    }

}
