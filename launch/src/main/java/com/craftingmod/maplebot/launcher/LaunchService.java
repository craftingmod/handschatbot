package com.craftingmod.maplebot.launcher;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.Handler;
import android.support.annotation.Nullable;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by superuser on 16/2/28.
 */
public class LaunchService extends Service implements Runnable {

    private Receiver receiver;
    private Timer timer;
    private TimerTask task;
    public Handler handler;

    private ActivityManager manager;
    private boolean skip;

    private int lastDay;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        receiver = new Receiver(this);
        timer = new Timer();
        task = new Tasker(this);
        skip = false;
        handler = new Handler(this.getMainLooper());

        manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        lastDay = getDay();

        IntentFilter filter = new IntentFilter();
        filter.addAction("broadcast.maple.shutdown");
        registerReceiver(receiver,filter);
        task.run();
        timer.scheduleAtFixedRate(task,0,1000*60*2);

        return super.onStartCommand(intent, flags, startId);
    }
    public int getDay(){
        return Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    public void onReceive(Context context, Intent intent){
        skip = true;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void run() {
        final int day = getDay();
        if(skip){
            skip = false;
        }else{
            if(lastDay != day){
                // update
                lastDay = day;
                Intent i = new Intent();
                i.setComponent(new ComponentName("com.craftingmod.maplebot.launcher","com.craftingmod.maplebot.launcher.SyncService"));
                startService(i);
            }
            // service check
            boolean heavy = false;
            boolean light = false;
            boolean search = false;
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                final String cname = service.service.getClassName();
                if(cname.equalsIgnoreCase("com.craftingmod.maplebot.heavy.HeavyService")){
                    heavy = true;
                }
                if(cname.equalsIgnoreCase("com.craftingmod.maplebot.light.LightService")){
                    light = true;
                }
                if(cname.equalsIgnoreCase("com.craftingmod.maplebot.charsearch.SearchService")){
                    search = true;
                }
            }
            Intent i;
            if(!light){
                i = new Intent();
                i.setComponent(new ComponentName("com.craftingmod.maplebot","com.craftingmod.maplebot.light.LightService"));
                startService(i);
            }
            if(!heavy){
                i = new Intent();
                i.setComponent(new ComponentName("com.craftingmod.maplebot","com.craftingmod.maplebot.heavy.HeavyService"));
                startService(i);
            }
            if(!search){
                i = new Intent();
                i.setComponent(new ComponentName("com.craftingmod.maplebot.charsearch","com.craftingmod.maplebot.charsearch.SearchService"));
                startService(i);
            }
        }
    }

    private class Tasker extends TimerTask {
        private LaunchService _this;

        public Tasker(LaunchService service){
            this._this = service;
        }
        @Override
        public void run() {
            handler.post(_this);
        }
    }
    private class Receiver extends BroadcastReceiver {
        private LaunchService _this;

        public Receiver (LaunchService service){
            this._this = service;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            this._this.onReceive(context,intent);
        }
    }

}
