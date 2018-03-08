package com.pg.software.controller.utils;

/**
 * Created by Freedom on 2017/10/24.
 */
public class Constant {

    /**
     * 蓝牙连接对外广播
     */
    public static final String XTBT_SWITCH_BROAD = "com.pg.software.XTBTSWITCH";
    /**
     * 控制蓝牙连接对内广播
     */
    public static final String XTBT_BROAD = "com.pg.software.XTBTCONTROL";
    /**
     * SOS对外广播
     */
    public static final String SOS_BROAD = "com.pg.software.EXPAND_KEY_DOWN";
    /**
     * SOS对外广播
     */
    public static final String VOICE_BROAD = "txz.intent.action.BUTTON";
    /**
     * 拍照/录像广播
     */
    public static final String PHOTO_VIDEO_BROAD = "com.pg.software.NOTIFACATION_MSG_TO_RECODER";
    /**
     * 连接状态
     * INITIALIZATION初始化    SCANSUCCESS搜索成功
     * SCANFAIL搜索失败    CONNECTED连接成功    CONNEECTFAIL连接失败    DISCONNECTED取消连接
     */
    public static final int INITIALIZATION = 0;
    public static final int SCANSUCCESS = 1;
    public static final int SCANFAIL = 2;
    public static final int CONNECTED = 3;
    public static final int CONNEECTFAIL = 4;
    public static final int DISCONNECTED = 5;

    /**
     * 包名
     */
    public static final String PACKAGE_KW ="cn.kuwo.kwmusiccar";
    public static final String PACKAGE_DRIV ="com.mediatek.carcorderdemo";


}
