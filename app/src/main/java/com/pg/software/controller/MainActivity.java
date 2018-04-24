package com.pg.software.controller;

import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.pg.software.controller.Service.BTService;
import com.pg.software.controller.constant.Constant;
import com.pg.software.controller.constant.MyApp;
import com.pg.software.controller.utils.LogUtils;
import com.pg.software.controller.utils.PICWindUtil;
import com.pg.software.controller.utils.PreUtil;
import com.pg.software.controller.utils.Utils;
import com.xintu.xintuclick.sdk.BizMain;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, Constant {

    private static final String TAG = "freedom";
    private Context mContext;
    private TextView textContext, textName;

    private boolean isDeviceConnect = false;
    private MyHandler mHandler;
    private BTService.MyBinder mBTService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandler = new MyHandler(this);
        mContext = MyApp.getContext();
        initView();

        if (!BTService.isStartAlive) {
            mContext.startService(new Intent(mContext, BTService.class));
        }
        bindBTService();
        BTService.setCallBack(mCallBack);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unbindService(conn);
        BTService.removeCallBack();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && PICWindUtil.getInstance(mContext).isWindShow()) {
            PICWindUtil.getInstance(mContext).showView(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private class MyHandler extends Handler {
        WeakReference<MainActivity> mWeak;

        private MyHandler(MainActivity mActivity) {
            mWeak = new WeakReference<>(mActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity mMain = mWeak.get();
            if (mMain == null)
                return;
            switch (msg.what) {
                case REFRESH_MAINVIEW:
                    if (mBTService != null) {
                        boolean isConnect = mBTService.isDeviceConnect();
                        String deviceAddress = mBTService.getBTAddress();
                        if (isConnect) {
                            textContext.setText(getString(R.string.connected));
                            textName.setText(deviceAddress);
                        } else {
                            textContext.setText(getString(R.string.searchBT));
                            textName.setText(getString(R.string.unconnect));
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void initView() {
        textContext = findViewById(R.id.textContext);
        textContext.setOnClickListener(this);
        textName = findViewById(R.id.textName);
        findViewById(R.id.mImageView).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.textContext:
                if (mBTService != null && !mBTService.isBTOpen()) {
                    //蓝牙未打开，提示打开蓝牙
                    Intent enableBtIntent = new Intent(
                            BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    mContext.startActivity(enableBtIntent);
                } else {
                    if (mBTService != null && !mBTService.isSearchBT()) {
                        if (isDeviceConnect) {
                            LogUtils.e(TAG, "stopConn");
                            Utils.stopConn();
                        } else {
                            LogUtils.e(TAG, "startScan");
                            Utils.startScan();
                            textContext.setText(mContext.getString(R.string.searchbt));
                        }
                    }
                }
                break;
            case R.id.mImageView:
                PICWindUtil.getInstance(mContext).showView(true);
                break;
            default:
                break;
        }
    }

    private void bindBTService() {
        Intent intent = new Intent(mContext, BTService.class);
        bindService(intent, conn, BIND_AUTO_CREATE);
    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBTService = (BTService.MyBinder) service;
            mHandler.sendEmptyMessage(REFRESH_MAINVIEW);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBTService = null;
        }
    };


    private BTService.XTBTCallBack mCallBack = new BTService.XTBTCallBack() {

        @Override
        public void isDeviceConnected(boolean isConnect, String deviceAddress) {
            mHandler.sendEmptyMessage(REFRESH_MAINVIEW);
        }

        @Override
        public void refreshStatus(boolean isBtOpen, boolean isDeviceConnect) {
            if (!isBtOpen) {
                Utils.makeToast(mContext, mContext.getString(R.string.btnoOPen));
            } else {
                if (!isDeviceConnect) {
                    Utils.makeToast(mContext, mContext.getString(R.string.btdisconnect));
                }
            }
        }
    };
}
