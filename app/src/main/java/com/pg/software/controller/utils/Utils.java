package com.pg.software.controller.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.widget.Toast;

import com.xintu.xintuclick.sdk.BizMain;

import java.lang.reflect.Method;

/**
 * Created by Freedom on 2017/10/24.
 */
public class Utils {

    private static final String UPDATE_HOTSPOT_STATE = "intent.action.update.hotspot.state";
    private static final int WIFI_HOT = 10086;
    private Context mContext;
    private static WifiManager mWifiManager;
    private static Utils mUtils;
    private static boolean enabled = false;

    private Utils(Context mContext, WifiManager mWifiManager) {
        this.mContext = mContext;
        this.mWifiManager = mWifiManager;
    }

    public static Utils getInstance(Context mContext, WifiManager mWifiManager) {
        if (mUtils == null) {
            mUtils = new Utils(mContext, mWifiManager);
        }
        return mUtils;
    }


    /**
     * 判断返回地址是否为空
     *
     * @param str
     * @return
     */
    public static boolean isNullString(String str) {
        if (str == null) {
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
     * 判断热点开启状态
     */
    public boolean isWifiApEnabled(WifiManager wifiManager) {
        return getWifiApState(wifiManager) == WIFI_AP_STATE.WIFI_AP_STATE_ENABLED;
    }

    private WIFI_AP_STATE getWifiApState(WifiManager wifiManager) {
        int tmp;
        try {
            Method method = wifiManager.getClass().getMethod("getWifiApState");
            tmp = ((Integer) method.invoke(wifiManager));
            // Fix for Android 4
            if (tmp > 10) {
                tmp = tmp - 10;
            }
            return WIFI_AP_STATE.class.getEnumConstants()[tmp];
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return WIFI_AP_STATE.WIFI_AP_STATE_FAILED;
        }
    }

    /**
     * 开始搜索
     */
    public static void startScan() {
        BizMain.getInstance().startScan();
    }

    /**
     * 停止搜索
     */
    public static void stopScan() {
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

    public enum WIFI_AP_STATE {
        WIFI_AP_STATE_DISABLING, WIFI_AP_STATE_DISABLED, WIFI_AP_STATE_ENABLING, WIFI_AP_STATE_ENABLED, WIFI_AP_STATE_FAILED
    }

    /**
     * wifi热点开关
     *
     * @param enabled true：打开 false：关闭
     * @return true：成功 false：失败
     */
    public boolean setWifiApEnabled(boolean enabled, Context mContext) {
        this.mContext = mContext;
        this.enabled = enabled;

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        ComponentName cn = new ComponentName("com.diggisin.mtk.settings",
                "com.digissin.mtk.hospot.WifiHotSpotActivity");
        intent.setComponent(cn);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mContext.startActivity(intent);

        if (enabled) { // disable WiFi in any case
            // wifi和热点不能同时打开，所以打开热点的时候需要关闭wifi
            mWifiManager.setWifiEnabled(false);
        } else {
            mWifiManager.setWifiEnabled(true);
        }
        mHandler.sendEmptyMessageDelayed(WIFI_HOT, 1000);

        // try {
        // // 热点的配置类
        // WifiConfiguration apConfig = new WifiConfiguration();
        // // 配置热点的名称(可以在名字后面加点随机数什么的)
        //
        // Context otherAppsContext;
        // otherAppsContext = mContext.createPackageContext(
        // "com.pg.software.setting", Context.CONTEXT_IGNORE_SECURITY);
        // SharedPreferences sharedPreferences = otherAppsContext
        // .getSharedPreferences("wifi_set",
        // Context.MODE_MULTI_PROCESS
        // | Context.MODE_WORLD_READABLE);
        // // wifi热点名字与密码
        // String ssid = sharedPreferences.getString("softap_name", "5APAI");
        // String pwd = sharedPreferences.getString("softap_passwd",
        // "12345678");
        // apConfig.SSID = ssid;
        // // 配置热点的密码
        // apConfig.preSharedKey = pwd;
        // // 安全：WPA2_PSK
        // apConfig.allowedKeyManagement
        // .set(WifiConfiguration.KeyMgmt.WPA_PSK);
        // // 通过反射调用设置热点
        // Method method = wifiManager.getClass().getMethod(
        // "setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
        // // 返回热点打开状态
        // return (Boolean) method.invoke(wifiManager, apConfig, enabled);
        // } catch (Exception e) {
        // return false;
        // }

        return enabled;
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case WIFI_HOT:
                    Intent wifiAction = new Intent(UPDATE_HOTSPOT_STATE);
                    if (enabled) {
                        wifiAction.putExtra("hotspot", 1);// 打开WIFIHOT
                        mContext.sendBroadcast(wifiAction);
                    } else {
                        wifiAction.putExtra("hotspot", 0);// 关闭WIFIHOT
                        mContext.sendBroadcast(wifiAction);
                    }
                    break;
                default:
                    break;
            }

        }

        ;
    };



}
