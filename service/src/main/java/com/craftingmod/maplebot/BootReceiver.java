package com.craftingmod.maplebot;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

/**
 * Created by superuser on 16/3/1.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent();
        i.setComponent(new ComponentName("com.craftingmod.maplebot.launcher","com.craftingmod.maplebot.launcher.LaunchService"));
        context.startService(i);
    }
}
