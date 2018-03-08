package com.pg.software.controller.utils;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.pg.software.controller.R;

/**
 * 显示弹框，
 * Created by Freedom on 2017/10/27.
 */
public class PICWindUtil {
    private Context mContext;
    private static PICWindUtil mPICWindUtil;
    private WindowManager mManager;
    private WindowManager.LayoutParams mParams;
    private View mView;
    private static boolean isWindShow;

    public boolean isWindShow() {
        return isWindShow;
    }

    private PICWindUtil(Context mContext) {
        this.mContext = mContext;
        initView();
    }

    public static PICWindUtil getInstance(Context mContext) {
        if (mPICWindUtil == null) {
            mPICWindUtil = new PICWindUtil(mContext);
        }
        return mPICWindUtil;
    }

    private void initView() {
        mManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mParams = new WindowManager.LayoutParams();
        mParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        mParams.format = PixelFormat.TRANSPARENT;
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        mParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        mParams.x = 0;
        mParams.y = 0;
        mParams.gravity = Gravity.CENTER;
        mView = LayoutInflater.from(mContext).inflate(R.layout.imageview, null);

        mView.findViewById(R.id.llLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showView(false);
            }
        });
    }

    public void showView(boolean isShow) {
        if (mView == null) {
            throw new IllegalArgumentException("there have no view");
        }
        if (isShow) {
            isWindShow = true;
            mManager.addView(mView, mParams);
        } else {
            isWindShow = false;
            mManager.removeView(mView);
        }
    }


}
