package com.craftingmod.maplebot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

/**
 * Created by superuser on 16/3/1.
 */
public class WifiReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        if(info != null && info.isAvailable() && info.isConnected()) {
            // Do your work.
            Intent i = context.getPackageManager().getLaunchIntentForPackage("com.Nexon.MonsterLifeChat");
            if(i != null){
                context.startActivity(i);
            }
        }
    }
}
