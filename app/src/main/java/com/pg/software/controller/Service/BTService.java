package com.pg.software.controller.Service;

import android.app.Instrumentation;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.pg.software.controller.R;
import com.pg.software.controller.constant.Constant;
import com.pg.software.controller.constant.MyApp;
import com.pg.software.controller.utils.LogUtils;
import com.pg.software.controller.utils.PreUtil;
import com.pg.software.controller.utils.Utils;
import com.pg.software.controller.utils.WifiHotUtils;
import com.xintu.xintuclick.sdk.BizMain;
import com.xintu.xintuclick.sdk.ErrCode;
import com.xintu.xintuclick.sdk.KeyEvent;
import com.xintu.xintuclick.sdk.XintuBLECallback;

import cn.kuwo.autosdk.api.KWAPI;
import cn.kuwo.autosdk.api.OnPlayerStatusListener;
import cn.kuwo.autosdk.api.PlayState;
import cn.kuwo.autosdk.api.PlayerStatus;

/**
 * Created by Freedom on 2017/10/24.
 */
public class BTService extends Service implements Constant {

    private static final String TAG = "freedom";

    private static Context mContext;
    private KWAPI mKwapi = null;
    private AudioManager mAudioManager;
    private WifiHotUtils mUtils;
    private WifiManager wifiManager;

    private boolean isMusicPlaying = false;
    private static int mCurrentVolume = 7;
    private static boolean isFMOpen = false;
    private static boolean isADASOpen = false;

    public static boolean isStartAlive = false;//是否开启Service
    private boolean isDeviceConnect = false;//设备是否连接
    private boolean isSearchBt = false;//是否在搜索蓝牙
    private boolean isBtOpen = false;

    private String btAddress = "";
    private MyXintuCallback mMyCallBack;

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.i(TAG, "Service_onDestroy");
        isStartAlive = false;
        mHandler.removeCallbacks(mRunnable);
        Utils.stopScan();
        BizMain.getInstance().unInitialize();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.i(TAG, "Service_onCreate");
        isStartAlive = true;
        mContext = MyApp.getContext();
        wifiManager = (WifiManager) mContext
                .getSystemService(Context.WIFI_SERVICE);
        mUtils = WifiHotUtils.init(mContext, wifiManager);
        mKwapi = KWAPI.createKWAPI(mContext, "auto");
        registerKWPlayStateListner();
        mAudioManager = (AudioManager) mContext
                .getSystemService(Context.AUDIO_SERVICE);
        mMyCallBack = new MyXintuCallback();

        int iValue = BizMain.getInstance().initialize(mContext, mMyCallBack);
        if (iValue == ErrCode.ERRCODE_OK) {
            mHandler.post(mRunnable);
        }


    }

    private Handler mHandler = new Handler() {
    };

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            //蓝牙是否打开
            isBtOpen = BizMain.getInstance().isBluetoothEnabled();
            LogUtils.i(TAG, "isBTOpen:" + isBtOpen + "  isDeviceConnect:" + isDeviceConnect + "  isSearchBt:" + isSearchBt);

            if (isBtOpen) {
                if (!isDeviceConnect && !isSearchBt) {
                    Utils.stopScan();
                    Utils.startScan();
                    isSearchBt = true;
                }
            }
            if (mCallBack != null) {
                mCallBack.refreshStatus(isBtOpen, isDeviceConnect);
            }
            mHandler.postDelayed(mRunnable, 10 * 1000);
        }
    };

    public class MyXintuCallback implements XintuBLECallback {
        @Override
        public void onScanSucc(String strAddress) {
            // 获取地址成功，返回设备的地址
            btAddress = strAddress;
            if (!Utils.isNullString(strAddress)) {
                isDeviceConnect = false;
                isSearchBt = false;
                Log.e(TAG, "scan succ " + strAddress);
                Utils.startConn(strAddress);
                Utils.stopScan();
                if (mCallBack != null) {
                    mCallBack.isDeviceConnected(true, "");
                }
            }
        }

        @Override
        public void onScanFail() {
            // 获取地址失败
            LogUtils.e(TAG, "onScanFail");
            isDeviceConnect = false;
            isSearchBt = false;
            Utils.release();
            if (mCallBack != null) {
                mCallBack.isDeviceConnected(false, "");
            }
        }

        @Override
        public void onConnected() {
            // 连接设备成功
            LogUtils.e(TAG, "onConnected");
            isDeviceConnect = true;
            isSearchBt = false;
            Utils.stopScan();
            if (mCallBack != null) {
                mCallBack.isDeviceConnected(true, btAddress);
            }
            mContext.sendBroadcast(new Intent(BT_REMOTE_CONTROL_ACTION_CONNECTION));
        }

        @Override
        public void onConnectFail() {
            // 连接设备失败
            LogUtils.e(TAG, "onConnectFail");
            mContext.sendBroadcast(new Intent(BT_REMOTE_CONTROL_ACTION_DISCONNECT));
            Utils.release();
            isDeviceConnect = false;
            isSearchBt = false;
            if (mCallBack != null) {
                mCallBack.isDeviceConnected(false, "");
            }
        }

        @Override
        public void onDisconnected() {
            //取消连接
            LogUtils.e(TAG, "onDisconnected");
            mContext.sendBroadcast(new Intent(BT_REMOTE_CONTROL_ACTION_DISCONNECT));
            Utils.release();
            isSearchBt = false;
            isDeviceConnect = false;
            if (mCallBack != null) {
                mCallBack.isDeviceConnected(false, "");
            }
        }

        @Override
        public void onNotifyEvent(int iKeyValue) {
            //点击按键
            LogUtils.i(TAG, "onNotifyEvent");
            switch (iKeyValue) {
                case KeyEvent.KEY_UP_PRESSED:
                    LogUtils.i(TAG, "KeyEvent.KEY_UP_PRESSED");
                    simulationKey(88);
                    break;
                case KeyEvent.KEY_UP_LONG_PRESSED:
                    LogUtils.i(TAG, "KeyEvent.KEY_UP_LONG_PRESSED");
                    break;
                case KeyEvent.KEY_DOWN_PRESSED:
                    LogUtils.i(TAG, "KeyEvent.KEY_DOWN_PRESSED");
                    simulationKey(87);
                    break;
                case KeyEvent.KEY_DOWN_LONG_PRESSED:
                    LogUtils.i(TAG, "KeyEvent.KEY_DOWN_LONG_PRESSED");
                    break;
                case KeyEvent.KEY_LEFT_PRESSED:
                    LogUtils.i(TAG, "KeyEvent.KEY_LEFT_PRESSED");
                    simulationKey(90);
                    break;
                case KeyEvent.KEY_LEFT_LONG_PRESSED:
                    LogUtils.i(TAG, "KeyEvent.KEY_LEFT_LONG_PRESSED");
                    break;
                case KeyEvent.KEY_RIGHT_PRESSED:
                    LogUtils.i(TAG, "KeyEvent.KEY_RIGHT_PRESSED");
                    simulationKey(89);
                    break;
                case KeyEvent.KEY_RIGHT_LONG_PRESSED:
                    LogUtils.i(TAG, "KeyEvent.KEY_RIGHT_LONG_PRESSED");
                    break;
                case KeyEvent.KEY_MIDDLE_PRESSED:
                    LogUtils.i(TAG, "KeyEvent.KEY_MIDDLE_PRESSED");
                    simulationKey(66);
                    break;
                case KeyEvent.KEY_MIDDLE_LONG_PRESSED:
                    LogUtils.i(TAG, "KeyEvent.KEY_MIDDLE_LONG_PRESSED");
                    break;
            }
        }
    }

    private void simulationKey(int keyCode) {
        try {
            // 按键操作命令
            String keyCommand = "input keyevent " + keyCode;
            // 调用Runtime模拟按键操作
            Runtime.getRuntime().exec(keyCommand);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void makePhotoAndVideo() {
        Intent intent = new Intent();
        intent.setAction(Constant.PHOTO_VIDEO_BROAD);
        intent.putExtra("msg", "getvideo");
        mContext.sendBroadcast(intent);

        Intent itRec = new Intent(Constant.PHOTO_VIDEO_BROAD);
        itRec.putExtra("msg", "getphoto");
        mContext.sendBroadcast(itRec);
    }

    private void soundUP() {
        setVolume(mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM) + 2);
    }

    private void soundDown() {
        setVolume(mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM) - 2);
    }

    private void sendSOS() {
        mContext.sendBroadcast(new Intent(Constant.SOS_BROAD)
                .putExtra("keycode", 188));
    }

    private void voiceControl() {
        Intent intent = getApplication().getPackageManager().getLaunchIntentForPackage("com.example.speechdemo");
        getApplication().startActivity(intent);
//        Intent itVoice = new Intent(Constant.VOICE_BROAD);
//        itVoice.putExtra("txz.intent.action.KEY_EVENT", 0xff00);
//        mContext.sendBroadcast(itVoice);
    }

    private void lastMusic() {
        mKwapi.setPlayState(mContext, PlayState.STATE_PRE);
    }

    private void nextMusic() {
        mKwapi.setPlayState(mContext, PlayState.STATE_NEXT);
    }

    private void switchADAS() {
        Intent startADAS = new Intent("android_dig_action_adas_enable");
        if (isADASOpen) {
            startADAS.putExtra("isEnable", false);
            isADASOpen = false;
        } else {
            startADAS.putExtra("isEnable", true);
            isADASOpen = true;
        }
        mContext.sendBroadcast(startADAS);
    }

    private void switchMusic() {
        if (Constant.PACKAGE_KW.equals(Utils.getTopPackageName(mContext))) {
            mKwapi.exitAPP(mContext);
        } else {
            mKwapi.startAPP(mContext, true);
        }
    }

    private void playOrPause() {
        if (isMusicPlaying) {
            mKwapi.setPlayState(mContext, PlayState.STATE_PAUSE);
        } else {
            mKwapi.setPlayState(mContext, PlayState.STATE_PLAY);
        }
    }

    private void switchWind() {
        Instrumentation inst = new Instrumentation();
        inst.sendKeyDownUpSync(26);
    }

    private void switchDriv() {
        if (Constant.PACKAGE_DRIV
                .equals(Utils.getTopPackageName(mContext))) {
            Intent mHomeIntent = new Intent(Intent.ACTION_MAIN);
            mHomeIntent.addCategory(Intent.CATEGORY_HOME);
            mHomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            mContext.startActivity(mHomeIntent);
        } else {
            Intent it = mContext
                    .getPackageManager()
                    .getLaunchIntentForPackage(Constant.PACKAGE_DRIV);
            if (it != null) {
                mContext.startActivity(it);
            }
        }
    }

    private void mute() {
        int volume = mAudioManager
                .getStreamVolume(AudioManager.STREAM_SYSTEM);
        if (volume == 0) {
            silent(false);
        } else {
            silent(true);
        }
    }

    private void switchWifiHot() {
        if (mUtils.isWifiApEnabled(wifiManager)) {
            mUtils.setWifiApEnabled(false, mContext);
        } else {
            mUtils.setWifiApEnabled(true, mContext);
        }
    }

    private void setVolume(int vol) {
        AudioManager audioManager = (AudioManager) mContext
                .getSystemService(Context.AUDIO_SERVICE);
        if (vol > audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM)) {
            vol = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
        }
        if (vol < 0) {
            vol = 0;
        }
        if (isFMOpen && vol > 12) {
            vol = 12;
        }
        setStreamVolume(audioManager, vol);
    }

    private void silent(boolean silent) {
        int vol;
        AudioManager audioManager = (AudioManager) mContext
                .getSystemService(Context.AUDIO_SERVICE);
        if (silent) {
            mCurrentVolume = audioManager
                    .getStreamVolume(AudioManager.STREAM_SYSTEM);
            vol = 0;
        } else {
            vol = mCurrentVolume;
        }
        if (vol > audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM)) {
            vol = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
        }
        if (vol < 0) {
            vol = 0;
        }
        if (isFMOpen && vol > 12) {
            vol = 12;
        }
        setStreamVolume(audioManager, vol);
    }

    private void setStreamVolume(AudioManager audioManager, int vol) {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0);
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, vol, 0);
        audioManager.setStreamVolume(AudioManager.STREAM_DTMF, vol, 0);
        audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, vol, 0);
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, vol, 0);
        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, vol, 0);
        audioManager.setStreamVolume(AudioManager.STREAM_RING, vol, 0);
    }

    private void registerKWPlayStateListner() {
        mKwapi.registerPlayerStatusListener(mContext,
                new OnPlayerStatusListener() {
                    @Override
                    public void onPlayerStatus(PlayerStatus arg0) {
                        LogUtils.d(TAG, "statechange:" + arg0);
                        if (arg0 == PlayerStatus.PLAYING) {
                            isMusicPlaying = true;
                        } else if (arg0 == PlayerStatus.PAUSE) {
                            isMusicPlaying = false;
                        }
                    }
                });
    }

    public class MyBinder extends Binder {
        private BTService mService;


        public MyBinder() {
            mService = BTService.this;
        }

        public boolean isDeviceConnect() {
            return isDeviceConnect;
        }

        public boolean isBTOpen() {
            return isBtOpen;
        }

        public String getBTAddress() {
            return btAddress;
        }

        public boolean isSearchBT() {
            return isSearchBt;
        }

    }

    private static XTBTCallBack mCallBack;

    public static void setCallBack(XTBTCallBack callBack) {
        mCallBack = callBack;
    }

    public static void removeCallBack() {
        mCallBack = null;
    }

    public interface XTBTCallBack {

        void isDeviceConnected(boolean isConnect, String deviceAddress);

        void refreshStatus(boolean isBtOpen, boolean isDeviceConnect);

    }
}
