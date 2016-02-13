package com.craftingmod.maplebot.light;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.craftingmod.maplebot.BroadUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by superuser on 16/2/13.
 */
public class LightService extends Service {

    private Gson g;
    private IReceiver mReceiver;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        g = new GsonBuilder().create();
        mReceiver = new IReceiver(this);
        mReceiver.onCreate();

        startForeground();

        return START_STICKY;
    }

    private void startForeground(){
        /**
         * Foreground notification
         */
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic)
                        .setContentTitle("Chatbot Service.")
                        .setContentText("It is running!");
        startForeground(5572, mBuilder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mReceiver.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onReceive(Context context, Intent intent) {

    }


    private class IReceiver extends BroadcastReceiver {

        private LightService objR;
        private final IntentFilter filter;
        protected IReceiver(LightService ob){
            objR = ob;
            filter = new IntentFilter(BroadUtil.RECEIVE_MESSAGE);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            objR.onReceive(context, intent);
        }
        public void onCreate(){
            objR.registerReceiver(this,filter);
        }
        public void onDestroy(){
            try{
                objR.unregisterReceiver(this);
            }catch (IllegalArgumentException e){
                e.printStackTrace();
            }
        }
    }
}
