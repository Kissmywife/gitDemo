package com.pg.software.controller.constant;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import com.pg.software.controller.utils.CrashHandler;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Freedom on 2018/1/6.
 */

public class MyApp extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();

//        //屏蔽错误弹框
//        CrashHandler handler = CrashHandler.getInstance();
//        Thread.setDefaultUncaughtExceptionHandler(handler);
//        //提交错误到bugly
         mContext = getApplicationContext();
//        // 获取当前包名
//        String packageName = mContext.getPackageName();
//        // 获取当前进程名
//        String processName = getProcessName(android.os.Process.myPid());
//        // 设置是否为上报进程
//        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(mContext);
//        strategy.setUploadProcess(processName == null || processName.equals(packageName));
//        // 初始化Bugly
//        CrashReport.initCrashReport(mContext, "74abb0f8a7", false, strategy);
    }

    public static Context getContext(){
        return mContext;
    }

    /**
     * 获取进程号对应的进程名
     *
     * @param pid 进程号
     * @return 进程名
     */
    private String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }
}
