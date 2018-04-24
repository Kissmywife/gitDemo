package com.pg.software.controller.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Handler;

import java.lang.reflect.Method;

/**
 * Created by Freedom on 2018/4/24.
 */

public class WifiHotUtils {

    private static WifiHotUtils mUtils;
    private Context mContext;
    private WifiManager mManager;
    private static final String UPDATE_HOTSPOT_STATE = "intent.action.update.hotspot.state";
    private static final int WIFI_HOT = 10086;
    private boolean enabled=false;

    public static WifiHotUtils init(Context mContext,WifiManager mManager){
        if(mUtils==null){
            mUtils=new WifiHotUtils(mContext,mManager);
        }
        return mUtils;
    }

    private WifiHotUtils(Context mContext,WifiManager mManager){
        this.mContext=mContext;
        this.mManager=mManager;
    }

    /**
     * 判断热点开启状态
     */
    public boolean isWifiApEnabled(WifiManager wifiManager) {
        return getWifiApState(wifiManager) == WifiHotUtils.WIFI_AP_STATE.WIFI_AP_STATE_ENABLED;
    }


    private WifiHotUtils.WIFI_AP_STATE getWifiApState(WifiManager wifiManager) {
        int tmp;
        try {
            Method method = wifiManager.getClass().getMethod("getWifiApState");
            tmp = ((Integer) method.invoke(wifiManager));
            // Fix for Android 4
            if (tmp > 10) {
                tmp = tmp - 10;
            }
            return WifiHotUtils.WIFI_AP_STATE.class.getEnumConstants()[tmp];
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return WifiHotUtils.WIFI_AP_STATE.WIFI_AP_STATE_FAILED;
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
            mManager.setWifiEnabled(false);
        } else {
            mManager.setWifiEnabled(true);
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
