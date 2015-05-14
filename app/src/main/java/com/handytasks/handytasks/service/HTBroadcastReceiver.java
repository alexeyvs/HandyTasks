package com.handytasks.handytasks.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class HTBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent startServiceIntent = new Intent(context, HTService.class);
        context.startService(startServiceIntent);
    }
}