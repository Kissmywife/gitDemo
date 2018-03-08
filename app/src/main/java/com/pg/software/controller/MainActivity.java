package com.pg.software.controller;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.pg.software.controller.Service.BTService;
import com.pg.software.controller.utils.BTAdapter;
import com.pg.software.controller.utils.Constant;
import com.pg.software.controller.utils.LogUtils;
import com.pg.software.controller.utils.PICWindUtil;
import com.pg.software.controller.utils.Utils;
import com.xintu.xintuclick.sdk.BizMain;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "XTBT_MainActivity";
    private Context mContext = MainActivity.this;
    private TextView textContext, textName;
    private ImageView mImageView;
    private ListView searchDevice;
    private ArrayList<String> mData = new ArrayList<>();
    private BTAdapter mAdapter;
    private String connectAddress;

    private static final int REFUSH_DATA = 10086;
    private static final int REFUSH_CONNECT = 10087;
    private static final int MAKE_TOAST = 10088;

    private static int connectNUM = 0;
    private static boolean isSearchBT = false;
    private static boolean isBTConnect = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        if (!BTService.isStartAlive) {
            mContext.startService(new Intent(mContext, BTService.class));
        }
        BTService.setCallBack(mCallBack);
        mHandler.post(mRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        connectNUM = 0;
        Utils.stopScan();
        isSearchBT = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mRunnable);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && PICWindUtil.getInstance(mContext).isWindShow()) {
            PICWindUtil.getInstance(mContext).showView(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (BTService.isConnect) {
            textName.setText(PreferenceManager.getDefaultSharedPreferences(mContext).getString("BTAddress", ""));
            textContext.setText(mContext.getString(R.string.connected));
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case REFUSH_CONNECT:
                    if ((int) msg.obj == Constant.CONNECTED) {
                        Utils.stopScan();
                        isSearchBT = false;
                        textName.setText(connectAddress);
                        BTService.setBtAddress(connectAddress);
                        textContext.setText(mContext.getString(R.string.connected));
                    } else {
                        if ((int) msg.obj == Constant.CONNEECTFAIL) {
                            Utils.startConn(connectAddress);
                        } else if ((int) msg.obj == Constant.SCANSUCCESS) {

                        } else {
                            connectAddress = mContext.getString(R.string.unconnect);
                            PreferenceManager.getDefaultSharedPreferences(mContext).edit().putString("BTAddress", "").commit();
                            textName.setText(connectAddress);
                            if (!textContext.getText().equals(mContext.getString(R.string.searchbt))) {
                                textContext.setText(mContext.getString(R.string.searchBT));
                            }
                        }
                    }
                    break;
                case REFUSH_DATA:
                    String mBTData = (String) msg.obj;
                    if (mData.size() == 0) {
                        mData.add(mBTData);
                        mAdapter.notifyDataSetChanged();
                    } else {
                        boolean haveBT = false;
                        for (int i = 0; i < mData.size(); i++) {
                            if (mData.get(i).equals(mBTData)) {
                                haveBT = false;
                                break;
                            } else {
                                haveBT = true;
                            }
                        }
                        if (haveBT) {
                            mData.add(mBTData);
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                    break;
                case MAKE_TOAST:
                    Utils.makeToast(mContext, mContext.getString(R.string.searchfail));
                    connectAddress = mContext.getString(R.string.unconnect);
                    PreferenceManager.getDefaultSharedPreferences(mContext).edit().putString("BTAddress", "").commit();
                    textName.setText(connectAddress);
                    if (!textContext.getText().equals(mContext.getString(R.string.searchbt))) ;
                {
                    textContext.setText(mContext.getString(R.string.searchBT));
                }
                break;
                default:
                    break;
            }
        }
    };

    private void initData() {
        mAdapter = new BTAdapter(mContext, mData);
        searchDevice.setAdapter(mAdapter);
    }

    private void initView() {
        textContext = (TextView) findViewById(R.id.textContext);
        textContext.setOnClickListener(this);
        textName = (TextView) findViewById(R.id.textName);
        mImageView = (ImageView) findViewById(R.id.mImageView);
        mImageView.setOnClickListener(this);
        searchDevice = (ListView) findViewById(R.id.searchDevice);
        searchDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                LogUtils.d(TAG, "Address:" + mData.get(i));
                Utils.stopConn();
                connectAddress = mData.get(i);
                textContext.setText(mContext.getString(R.string.connecting));
                Utils.startConn(mData.get(i));
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.textContext:
                if (!BizMain.getInstance().isBluetoothEnabled()) {
                    //蓝牙未打开，提示打开蓝牙
                    Intent enableBtIntent = new Intent(
                            BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    mContext.startActivity(enableBtIntent);
                } else {
                    if (!isSearchBT) {
                        if (isBTConnect) {
                            LogUtils.e(TAG, "stopConn");
                            Utils.stopConn();
                            BTService.setIsClickBT(true);
                        } else {
                            LogUtils.e(TAG, "startScan");
                            Utils.startScan();
                            textContext.setText(mContext.getString(R.string.searchbt));
                            isSearchBT = true;
                        }
                    } else {
                        Utils.makeToast(mContext, mContext.getString(R.string.searching));
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

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            isBTConnect = BTService.isConnect;
            mHandler.postDelayed(mRunnable, 1000);
        }
    };

    private BTService.XTBTCallBack mCallBack = new BTService.XTBTCallBack() {
        @Override
        public void btData(String mBtData) {
            Message msg = mHandler.obtainMessage(REFUSH_DATA);
            msg.obj = mBtData;
            mHandler.sendMessage(msg);
        }

        @Override
        public void connectionStatus(int type) {
            Message msg = mHandler.obtainMessage(REFUSH_CONNECT);
            msg.obj = type;
            mHandler.sendMessage(msg);
        }

        @Override
        public void connectionTime() {
            if (connectNUM <= 20) {
                isSearchBT = true;
                BizMain.getInstance().startScan();
            } else {
                if (mData.size() <= 0) {
                    mHandler.sendEmptyMessage(MAKE_TOAST);
                }
                isSearchBT = false;
                connectNUM = 0;
            }
            connectNUM++;
        }
    };
}
