package com.pg.software.controller.constant;

/**
 * Created by Freedom on 2017/10/24.
 */
public interface Constant {

    /**
     * 蓝牙连接对外广播
     */
    String XTBT_SWITCH_BROAD = "com.pg.software.XTBTSWITCH";
    /**
     * 控制蓝牙连接对内广播
     */
    String XTBT_BROAD = "com.pg.software.XTBTCONTROL";
    /**
     * SOS对外广播
     */
    String SOS_BROAD = "com.pg.software.EXPAND_KEY_DOWN";
    /**
     * SOS对外广播
     */
    String VOICE_BROAD = "txz.intent.action.BUTTON";
    /**
     * 拍照/录像广播
     */
    String PHOTO_VIDEO_BROAD = "com.pg.software.NOTIFACATION_MSG_TO_RECODER";


    /**
     * 酷我音乐包名
     */
    String PACKAGE_KW = "cn.kuwo.kwmusiccar";
    /**
     * 记录仪包名
     */
    String PACKAGE_DRIV = "com.mediatek.carcorderdemo";
    /**
     * 蓝牙遥控器连接的action
     */
    String BT_REMOTE_CONTROL_ACTION_CONNECTION = "com.hud.BTRC.CONNECTION";

    /**
     * 蓝牙遥控器断开的action
     */
    String BT_REMOTE_CONTROL_ACTION_DISCONNECT = "com.hud.BTRC.DISCONNECT";

    int REFRESH_MAINVIEW=10086;
}
