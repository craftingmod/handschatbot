package com.craftingmod.maplechatbot.chat;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Handler;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.craftingmod.maplechatbot.BroadUtil;
import com.craftingmod.maplechatbot.Config;
import com.craftingmod.maplechatbot.MapleUtil;
import com.craftingmod.maplechatbot.R;
import com.craftingmod.maplechatbot.command.BackDel;
import com.craftingmod.maplechatbot.command.BaseCommand;
import com.craftingmod.maplechatbot.command.Block;
import com.craftingmod.maplechatbot.command.BotState;
import com.craftingmod.maplechatbot.command.Cmath;
import com.craftingmod.maplechatbot.command.Coin;
import com.craftingmod.maplechatbot.command.Help;
import com.craftingmod.maplechatbot.command.Info;
import com.craftingmod.maplechatbot.command.Lotto;
import com.craftingmod.maplechatbot.command.Mail;
import com.craftingmod.maplechatbot.command.Tracker;
import com.craftingmod.maplechatbot.command.UserInfo;
import com.craftingmod.maplechatbot.model.ChatModel;
import com.craftingmod.maplechatbot.model.UserModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pengrad.telegrambot.TelegramBot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by superuser on 15/12/12.
 */
public class ChatService extends Service implements ISender {

    private MyReceiver rec;
    private TelegramBot bot;
    private Gson g;
    private HashMap<Integer,UserModel> users;

    private Boolean isInitBot = false;
    private Block blocks;
    private HandlerThread thread;
    private Handler handler;

    private ArrayList<BaseCommand> commands;

    private ArrayList<Integer> allAIDs;
    private int[] allAIDi;

    private UserDB db;

    private int botID;

    @Nullable

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {

    }
    private void onMessage(final ChatModel chat,final UserModel user,final String msg){
        Log.d("Maple", "called onMessage: " + g.toJson(chat) + " / " + g.toJson(user));

        if(chat.SenderCID == Config.CHARACTER_BOT_ID){
            return;
        }
        // "SenderAID":17282110,"SenderCID":75623432
        if(blocks.isBlocked(user.accountID) && msg.startsWith("!")){
            //this.sendMessage("유저입니다",17282110);
            return;
        }
        for(int k=0;k<commands.size();k+=1){
            final int ps = k;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    commands.get(ps).Receive(chat, user, msg);
                }
            });
        }

        //this.sendMessage("테스트",Config.MASTER_ACCOUNT_ID);
        /*
        for(int i=0;i<commands.size();i+=1){
            commands.get(i).Receive(chat,user,msg);
        }
        */
    }
    public void onReceive(Intent intent) {
        final ChatModel ch =  g.fromJson(intent.getStringExtra("data"), ChatModel.class);
        Log.d("Maple", "messaged received: " + ch.SenderCID + " : " + ch.Msg);
        final int cID = ch.SenderCID;

        handler.post(new Runnable() {
            @Override
            public void run() {
                if(users.containsKey(cID) && users.get(cID).lastTimestamp >= (System.currentTimeMillis()/1000)-7200){
                    onMessage(ch,users.get(cID),ch.Msg);
                }else{
                    UserModel user = MapleUtil.getInstance().getCharacterInfo(ch.WID, ch.SenderCID);
                    user.lastTimestamp = System.currentTimeMillis()/1000;
                    if(users.containsKey(cID)){
                        users.remove(cID);
                    }
                    users.put(cID,user);
                    onMessage(ch,user,ch.Msg);
                }
            }
        });
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        g =  new GsonBuilder().create();
        users = new HashMap<>();
        db = new UserDB(this);
        thread = new HandlerThread("ChatServiceT");
        thread.start();
        handler = new Handler(thread.getLooper());

        final int BOT_UPDATE_TIME = 2000; // millisecond
        final int AUTO_SAVE_TIME = 60*10; // sec

        commands = new ArrayList<>();
       // commands.add(new Info(this));
        commands.add(new Lotto(this));
        commands.add(new Mail(this));
        commands.add(new Tracker(this));
        commands.add(new BotState(this, this));
        commands.add(new UserInfo(this));
        commands.add(new Coin(this));
        commands.add(new Cmath(this));
        commands.add(new Help(this));
        commands.add(new Info(this));
        commands.add(new BackDel(this));
        blocks = new Block(this);
        commands.add(blocks);

        /**
         * Add Receiver
         */
        rec = new MyReceiver(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadUtil.RECEIVE_MESSAGE);
        registerReceiver(rec, intentFilter);

        /**
         * Auto save function
         */
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                save();
            }
        },0,1000*AUTO_SAVE_TIME);

        /**
         * Foreground notification
         */
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic)
                        .setContentTitle("Chatbot Service.")
                        .setContentText("It is running!");
        startForeground(5371, mBuilder.build());
        /**
         * Update all aids for hands
         */

        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        try{
            unregisterReceiver(rec);
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }
    }
    public void save(){
        for(int i=0;i<commands.size();i+=1){
            commands.get(i).onSave();
        }
    }
    private void syncAllID(){
        if(allAIDi == null || allAIDi.length != allAIDs.size()){
            allAIDi = new int[allAIDs.size()];
            for (int i=0; i < allAIDs.size(); i+=1){
                allAIDi[i] = allAIDs.get(i);
            }
        }
    }
    public void sendMessageAll(String msg){
        syncAllID();
        NativeSendMessage(new ChatModel(msg, allAIDi));
    }

    @Override
    public UserDB getDB() {
        return db;
    }

    @Override
    public Context getContext() {
        return this;
    }

    public void sendMessage(String msg,int sender){
        NativeSendMessage(new ChatModel(msg,new int[]{sender}));
    }
    private void NativeSendMessage(ChatModel model){
        this.sendBroadcast(BroadUtil.buildF_sendMessage(g,model));
    }

    public void toggleSlient(){
        for(int i=0;i<commands.size();i+=1){
            commands.get(i).slient = !commands.get(i).slient;
        }
    }

    private class MyReceiver extends BroadcastReceiver {

        private ChatService service;
        public MyReceiver(ChatService sv){
            service = sv;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            service.onReceive(intent);
        }

    }

}
