package com.pg.software.controller.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Freedom on 2017/10/24.
 */
public class BTBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent startService=new Intent(context,BTService.class);
        startService.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startService(startService);
    }
}
