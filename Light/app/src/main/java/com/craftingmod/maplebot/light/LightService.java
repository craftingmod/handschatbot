package com.craftingmod.maplebot.light;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import com.craftingmod.maplebot.BroadUtil;
import com.craftingmod.maplebot.Config;
import com.craftingmod.maplebot.light.modules.BotStatus;
import com.craftingmod.maplebot.light.modules.Calc;
import com.craftingmod.maplebot.light.modules.Coin;
import com.craftingmod.maplebot.light.modules.CoreModule;
import com.craftingmod.maplebot.light.modules.OX;
import com.craftingmod.maplebot.light.modules.StarEnchant;
import com.craftingmod.maplebot.model.ChatModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

/**
 * Created by superuser on 16/2/13.
 */
public class LightService extends Service implements IService {

    private Gson g;
    private IReceiver mReceiver;
    private ArrayList<CoreModule> modules;
    private ChatModel botModel;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        g = new GsonBuilder().create();
        modules = new ArrayList<>();
        mReceiver = new IReceiver(this);
        mReceiver.onCreate();

        botModel = new ChatModel(Config.MASTER_ACCOUNT_ID,0,Config.MASTER_ACCOUNT_ID,Config.MASTER_ACCOUNT_ID,Config.CHARACTER_BOT_ID,Config.WORLD_BOT_ID,"");
        initModules();
        startForeground();

        return START_STICKY;
    }
    private void initModules(){
        CoreModule[] md = new CoreModule[]{
                new BotStatus(this),new Calc(this),new Coin(this),
                new OX(this),new StarEnchant(this)};
        modules.clear();
        String initS = "LightService 실행 - ";
        for(CoreModule module : md){
            initS += module.getName() + ",";
            modules.add(module);
        }
        initS = initS.substring(0,initS.length()-1);
        ChatModel model = botModel.clone();
        model.FriendAids = new int[]{Config.MASTER_ACCOUNT_ID};
        model.Msg = initS;
        sendFMessage(model);
    }

    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equalsIgnoreCase(BroadUtil.RECEIVE_MESSAGE)){
            Message msg = new Message();
            ChatModel chat = g.fromJson(intent.getStringExtra("data"),ChatModel.class);
            if(chat == null){
                return;
            }
            if(chat.Msg.startsWith("!") && chat.Msg.length() >= 2){
                chat.Msg = chat.Msg.substring(1);
            }else{
                return;
            }
            msg.arg1 = intent.getIntExtra("type", BroadUtil.TYPE_FRIEND);
            msg.obj = chat;
            for(CoreModule module : modules){
                if(module.shouldExec(chat.Msg)){
                    module.sendMessage(msg);
                }
            }
        }
    }


    @Override
    public void sendFMessage(ChatModel model,String text) {
        ChatModel cm = this.botModel.clone();
        cm.Msg = text;
        cm.FriendAids = new int[]{Config.MASTER_ACCOUNT_ID,model.SenderAID};
        sendFMessage(cm);
    }
    public void sendFMessage(ChatModel model) {
        sendBroadcast(BroadUtil.buildF_sendMessage(g,model));
    }

    @Override
    public void sendGMessage(String text) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mReceiver.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public Looper getLooper() {
        return this.getMainLooper();
    }
    @Override
    public Context getContext() {
        return this.getBaseContext();
    }
    private void startForeground(){
        /**
         * Foreground notification
         */
        Notification.Builder mBuilder =
                new Notification.Builder(this)
                        .setSmallIcon(R.drawable.ic)
                        .setContentTitle("Lightweight Chatbot")
                        .setContentText("It is running!");
        startForeground(5572, mBuilder.build());
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
