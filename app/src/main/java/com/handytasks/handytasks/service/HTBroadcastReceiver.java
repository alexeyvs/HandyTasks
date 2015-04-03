package com.handytasks.handytasks.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by avsho_000 on 4/1/2015.
 */
public class HTBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent startServiceIntent = new Intent(context, HTService.class);
        context.startService(startServiceIntent);
    }
}