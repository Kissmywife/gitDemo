package com.pg.software.controller.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Freedom on 2018/4/20.
 */

public class PreUtil {

    private static PreUtil mPreUtil;
    private SharedPreferences mShared;
    private SharedPreferences.Editor mEdit;

    public static PreUtil init(Context mContext) {
        if (mPreUtil == null) {
            mPreUtil = new PreUtil(mContext);
        }
        return mPreUtil;
    }

    private PreUtil(Context mContext) {
        mShared = PreferenceManager.getDefaultSharedPreferences(mContext);
        mEdit = mShared.edit();
    }

    public void putBoolean(String key, boolean value) {
        mEdit.putBoolean(key, value).commit();
    }

    public void putString(String key, String value) {
        mEdit.putString(key, value).commit();
    }

    public void puInt(String key, int value) {
        mEdit.putInt(key, value).commit();
    }

    public boolean getBoolean(String key) {
        return mShared.getBoolean(key, false);
    }

    public String getString(String key) {
        return mShared.getString(key, "");
    }

    public int getInt(String key) {
        return mShared.getInt(key, 0);
    }
}
