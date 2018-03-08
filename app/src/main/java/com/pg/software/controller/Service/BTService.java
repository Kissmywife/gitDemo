package com.pg.software.controller.Service;

import android.app.Instrumentation;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.pg.software.controller.R;
import com.pg.software.controller.utils.Constant;
import com.pg.software.controller.utils.LogUtils;
import com.pg.software.controller.utils.Utils;
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
public class BTService extends Service {

    private static final String TAG = "XTBT_BTService";
    private static XTBTCallBack mCallBack;
    private static Context mContext;
    private KWAPI mKwapi = null;
    private AudioManager mAudioManager;
    private Utils mUtils;
    private WifiManager wifiManager;

    private static final int BT_ADDRESS = 10086;

    private boolean isMusicPlaying = false;
    private static int mCurrentVolume = 7;
    private static boolean isFMOpen = false;
    private static boolean isADASOpen = false;

    public static boolean isStartAlive = false;//是否开启Service
    public static boolean isConnect = false;//设备是否连接
    public static boolean isClickBT = false;//手动点击断开
    private static String btAddress = "";

    public static String getBtAddress() {
        return btAddress;
    }

    public static void setBtAddress(String btAddress) {
        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putString("BTAddress", btAddress).commit();
        BTService.btAddress = btAddress;
    }

    public static void setIsClickBT(boolean isClickBT) {
        BTService.isClickBT = isClickBT;
    }

    public static void setCallBack(XTBTCallBack callBack) {
        mCallBack = callBack;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isStartAlive = false;
        mHandler.removeCallbacks(mRunnable);
        Utils.stopScan();
        BizMain.getInstance().unInitialize();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.i(TAG, "Service_onCreate");
        isStartAlive = true;
        mContext = getApplicationContext();
        wifiManager = (WifiManager) mContext
                .getSystemService(Context.WIFI_SERVICE);
        mUtils = Utils.getInstance(mContext, wifiManager);
        mKwapi = KWAPI.createKWAPI(mContext, "auto");
        KWPlayStateListner();
        mAudioManager = (AudioManager) mContext
                .getSystemService(Context.AUDIO_SERVICE);
        int iValue = BizMain.getInstance().initialize(mContext, new MyXintuCallback());
        if (iValue == ErrCode.ERRCODE_OK) {
            if (!BizMain.getInstance().isBluetoothEnabled()) {
                Toast.makeText(mContext, mContext.getString(R.string.btnoOPen), Toast.LENGTH_LONG).show();
            }
            mHandler.postDelayed(mRunnable, 0);
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case BT_ADDRESS:
                    btAddress = PreferenceManager.getDefaultSharedPreferences(mContext).getString("BTAddress", "");
                    if (TextUtils.isEmpty(btAddress)) {
                        Utils.startScan();
                    } else {
                        if (!isClickBT) {
                            Utils.startConn(btAddress);
                        }
                        isClickBT = false;
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if(!isConnect){
                Utils.stopScan();
                Utils.startScan();
            }
            mHandler.postDelayed(mRunnable, 10 * 1000);
        }
    };

    public class MyXintuCallback implements XintuBLECallback {
        @Override
        public void onScanSucc(String strAddr) {
            // 获取地址成功，返回设备的地址
            isConnect = false;
            if (!Utils.isNullString(strAddr)) {
                Log.e(TAG, "scan succ " + strAddr);
                if (mCallBack != null) {
//                    mCallBack.btData(strAddr);
                    Utils.startConn(strAddr);
                    Utils.stopScan();
                    mCallBack.connectionStatus(Constant.SCANSUCCESS);
                    mCallBack.connectionTime();
                }
            }
        }

        @Override
        public void onScanFail() {
            // 获取地址失败
            LogUtils.e(TAG, "onScanFail");
            isConnect = false;
            if (mCallBack != null) {
                mCallBack.connectionTime();
                mCallBack.connectionStatus(Constant.SCANFAIL);
            }
        }

        @Override
        public void onConnected() {
            // 连接设备成功
            LogUtils.e(TAG, "onConnected");
            mHandler.sendEmptyMessage(BT_ADDRESS);
            isConnect = true;
            if (mCallBack != null) {
                mCallBack.connectionStatus(Constant.CONNECTED);
            }
        }

        @Override
        public void onConnectFail() {
            // 连接设备失败
            LogUtils.e(TAG, "onConnectFail");
            mHandler.sendEmptyMessage(BT_ADDRESS);
            isConnect = false;
            if (mCallBack != null) {
                mCallBack.connectionStatus(Constant.CONNEECTFAIL);
            }
        }

        @Override
        public void onDisconnected() {
            //取消连接
            LogUtils.e(TAG, "onDisconnected");
            mHandler.sendEmptyMessage(BT_ADDRESS);
            isConnect = false;
            if (mCallBack != null) {
                mCallBack.connectionStatus(Constant.DISCONNECTED);
            }
        }

        @Override
        public void onNotifyEvent(int iKeyValue) {
            //点击按键
            LogUtils.e(TAG, "onNotifyEvent");
            switch (iKeyValue) {
                case KeyEvent.KEY_UP_PRESSED:
                    LogUtils.e(TAG, "KeyEvent.KEY_UP_PRESSED");
                    playOrPause();
                    break;
                case KeyEvent.KEY_DOWN_PRESSED:
                    LogUtils.e(TAG, "KeyEvent.KEY_DOWN_PRESSED");
                    soundDown();
                    break;
                case KeyEvent.KEY_LEFT_PRESSED:
                    LogUtils.e(TAG, "KeyEvent.KEY_LEFT_PRESSED");
                    lastMusic();
                    break;
                case KeyEvent.KEY_RIGHT_PRESSED:
                    LogUtils.e(TAG, "KeyEvent.KEY_RIGHT_PRESSED");
                    nextMusic();
                    break;
                case KeyEvent.KEY_MIDDLE_PRESSED:
                    LogUtils.e(TAG, "KeyEvent.KEY_MIDDLE_PRESSED");
                    makePhotoAndVideo();
                    break;
                case KeyEvent.KEY_UP_LONG_PRESSED:
                    LogUtils.e(TAG, "KeyEvent.KEY_UP_LONG_PRESSED");
                    switchMusic();
                    break;
                case KeyEvent.KEY_DOWN_LONG_PRESSED:
                    LogUtils.e(TAG, "KeyEvent.KEY_DOWN_LONG_PRESSED");
                    soundUP();
                    break;
                case KeyEvent.KEY_LEFT_LONG_PRESSED:
                    LogUtils.e(TAG, "KeyEvent.KEY_LEFT_LONG_PRESSED");
                    switchWind();
                    break;
                case KeyEvent.KEY_RIGHT_LONG_PRESSED:
                    LogUtils.e(TAG, "KeyEvent.KEY_RIGHT_LONG_PRESSED");
                    switchDriv();
                    break;
                case KeyEvent.KEY_MIDDLE_LONG_PRESSED:
                    LogUtils.e(TAG, "KeyEvent.KEY_MIDDLE_LONG_PRESSED");
                    voiceControl();
                    break;
            }
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

    private void KWPlayStateListner() {
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

    public interface XTBTCallBack {
        void btData(String mBtData);

        void connectionStatus(int type);

        void connectionTime();
    }
}
