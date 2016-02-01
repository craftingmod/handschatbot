package com.craftingmod.maplechatbot.chat;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.res.TypedArrayUtils;
import android.util.Log;

import com.anprosit.android.promise.Callback;
import com.anprosit.android.promise.NextTask;
import com.anprosit.android.promise.Promise;
import com.anprosit.android.promise.Task;
import com.craftingmod.maplechatbot.Config;
import com.craftingmod.maplechatbot.MapleUtil;
import com.craftingmod.maplechatbot.R;
import com.craftingmod.maplechatbot.command.BaseCommand;
import com.craftingmod.maplechatbot.command.Block;
import com.craftingmod.maplechatbot.command.BotState;
import com.craftingmod.maplechatbot.command.Cmath;
import com.craftingmod.maplechatbot.command.Coin;
import com.craftingmod.maplechatbot.command.FriendList;
import com.craftingmod.maplechatbot.command.Help;
import com.craftingmod.maplechatbot.command.Info;
import com.craftingmod.maplechatbot.command.Lotto;
import com.craftingmod.maplechatbot.command.Mail;
import com.craftingmod.maplechatbot.command.Tracker;
import com.craftingmod.maplechatbot.command.UserInfo;
import com.craftingmod.maplechatbot.model.ChatModel;
import com.craftingmod.maplechatbot.model.FriendModel;
import com.craftingmod.maplechatbot.model.UserModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramBotAdapter;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.robv.android.xposed.XposedBridge;

/**
 * Created by superuser on 15/12/12.
 */
public class ChatService extends Service implements ISender {

    private MyReceiver rec;
    private TelegramBot bot;
    private Gson g;
    private HashMap<String,UserModel> users;

    private Boolean isInitBot = false;
    private Block blocks;

    private ArrayList<BaseCommand> commands;

    private ArrayList<Integer> allAIDs;
    private int[] allAIDi;

    private int botID;

    @Nullable

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {

    }
    /*
    private String getCommandResult(String message){
        if(message.equalsIgnoreCase("!version")){
            return "0.1.0.alpha";
        }else if(message.equalsIgnoreCase("!help")){
            return "그런거없어요.";
        }else if(message.equalsIgnoreCase("!주사위")) {
            return (int)(1 + Math.floor(Math.random() * 6)) + " !";
        }else{
            return null;
        }
    }
    private String getMail(String uname,String message){
        if(mails.containsKey(uname)) {
            String ms = mails.get(uname);
            mails.remove(uname);
            return ms;
        }
        if(message.startsWith("!mail")){
            if(message.split(" ").length <= 2){
                return uname + " >> 커맨드 사용법: !mail <보낼 대상> <할말>";
            }else{
                String[] splited = message.split(" ");
                String saying = message.substring(6 + splited[1].length() + 1);
                mails.put(splited[1], uname + " >>  " + saying);
                return uname + " >> 메일 저장 완료!";
            }
        }
        return null;
    }
    private String getLastseen(String message){
        if(message.startsWith("!최근") || message.startsWith("!seen")){
            String uname = message.split(" ")[1];
            if(connects.containsKey(uname)) {
                StringBuilder lastseen = new StringBuilder();
                Long ms = connects.get(uname);
                lastseen.append(uname + "님은 ");
                long sec = (System.nanoTime() - ms)/1000000000;
                if(sec >= 86400){
                    lastseen.append((int)Math.floor(sec/86400) + "일 ");
                    sec = sec % 86400;
                }
                if(sec >= 3600){
                    lastseen.append((int)Math.floor(sec/3600) + "시간 ");
                    sec = sec % 3600;
                }
                if(sec >= 60){
                    lastseen.append((int)Math.floor(sec/60) + "분 ");
                    sec = sec % 60;
                }
                lastseen.append((int)Math.floor(sec) + "초");
                lastseen.append("에 마지막으로 말했어요");
                return lastseen.toString();
            }else{
                return "방문 기록이 없습니다!";
            }
        }
        return null;
    }
    */
    private void sendTelegram(String msg){
        Intent intent = new Intent(Config.TELEGRAM_MESSAGE);
        intent.putExtra("token", Config.ACCESS_BRAODCAST_TOKEN);
        intent.putExtra("data", msg);
        sendBroadcast(intent);
    }
    private void onMessage(final ChatModel chat,final UserModel user,final String msg){
        Log.d("Maple", "called onMessage: " + g.toJson(chat) + " / " + g.toJson(user));

        new Handler().post(new Runnable() {
            @Override
            public void run() {
               // sendTelegram("#" + user.userName + " : " + msg);
            }
        });

        if(chat.SenderCID == Config.CHARACTER_BOT_ID){
            return;
        }
        // "SenderAID":17282110,"SenderCID":75623432
        if(blocks.isBlocked(user.accountID) && msg.startsWith("!")){
            //this.sendMessage("유저입니다",17282110);
            return;
        }
        for(int k=0;k<commands.size();k+=1){
            commands.get(k).Receive(chat, user, msg);
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
        final String uaID = ch.SenderAID + "/" + ch.SenderCID;
        if(users.containsKey(uaID) && users.get(uaID).lastTimestamp >= (System.currentTimeMillis()/1000)-7200){
            onMessage(ch,users.get(uaID),ch.Msg);
        }else{
            Promise.with(this,Boolean.class).then(new Task<Boolean, HashMap<Integer,String>>() {
                @Override
                public void run(Boolean aBoolean, NextTask<HashMap<Integer, String>> nextTask) {
                    nextTask.run(CharacterFinder.getSearch(ch.SenderAID,ch.SenderCID));
                }
            }).then(CharacterFinder.getInstance()).then(new Task<ArrayList<UserModel>, Void>() {
                @Override
                public void run(ArrayList<UserModel> userModels, NextTask<Void> nextTask) {
                    if(userModels.size() != 1){
                        XposedBridge.log("Error: No User found.");
                    }else{
                        UserModel model = userModels.get(0);
                        model.lastTimestamp = System.currentTimeMillis()/1000;
                        users.put(uaID,model);
                        onMessage(ch,model,ch.Msg);
                    }
                }
            }).setCallback(new Callback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                }

                @Override
                public void onFailure(Bundle bundle, Exception e) {
                    if (e != null) e.printStackTrace();
                }
            }).create().execute(true);
        }
    }
    private int lastMessageID = 0;
    public void onTelegramMessage(List<Update> updates){
        for(int i=0;i<updates.size();i+=1){
            Message message = updates.get(i).message();
            if(!message.text().startsWith("/")){
                sendMessageSpeak(message.text());
                Log.d("MapleReceived",message.text());
            }else{
                for(int j=0;j<commands.size();j+=1){
                    commands.get(j).callTelegram(message.text().substring(1));
                }
            }
            lastMessageID = updates.get(i).updateId() + 1;
        }
    }

    /**
     * update speakers
     */
    public void updateSpeak(){
        allAIDs = new ArrayList<>();
        Promise.with(this, Boolean.class).then(new Task<Boolean, ArrayList<FriendModel>>() {
            @Override
            public void run(Boolean aBoolean, NextTask<ArrayList<FriendModel>> nextTask) {
                MapleUtil.getInstance().getAccountFriendList(Config.MASTER_ACCOUNT_ID,nextTask);
            }
        }).then(new Task<ArrayList<FriendModel>, Void>() {
            @Override
            public void run(ArrayList<FriendModel> friendModels, NextTask<Void> nextTask) {
                ArrayList<Integer> list = new ArrayList<>();
                int data;
                for(int i=0;i<friendModels.size();i+=1){
                    data = friendModels.get(i).accountID;
                    if(!list.contains(data)){
                        list.add(data);
                    }
                }
                allAIDs = list;
                nextTask.run(null);
            }
        }).setCallback(new Callback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }

            @Override
            public void onFailure(Bundle bundle, Exception e) {
                if (e != null) {
                    e.printStackTrace();
                }
            }
        }).create().execute(true);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        g =  new GsonBuilder().create();
        users = new HashMap<>();

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
        commands.add(new FriendList(this));
        blocks = new Block(this);
        commands.add(blocks);

        /**
         * Add Receiver
         */
        rec = new MyReceiver(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Config.BROADCAST_MESSAGE);
        registerReceiver(rec, intentFilter);

        /**
         * Add telegram receiver
         */
        bot = TelegramBotAdapter.build(Config.TELEGRAM_BOT_TOKEN);
        final Handler handler = new Handler();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... params) {
                                List<Update> updates;
                                try {
                                    if (lastMessageID == 0) {
                                        updates = bot.getUpdates(null, 20, 1000).updates();
                                    } else {
                                        updates = bot.getUpdates(lastMessageID, 20, 1000).updates();
                                    }
                                    if (updates.size() >= 1) {
                                        onTelegramMessage(updates);
                                    }
                                } catch (Exception e2) {
                                    e2.printStackTrace();
                                }
                                return null;
                            }
                        }.execute();
                    }
                });
            }
        }, 0, BOT_UPDATE_TIME);

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
        updateSpeak();

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
    public void sendMessageSpeak(String msg){
        syncAllID();
        ChatModel cm = Config.CHAT_HANDS;
        cm.FriendAids = allAIDi;
        cm.Msg = msg;
        NativeSendMessage(cm);
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
    public Context getContext() {
        return this;
    }

    public void sendMessage(String msg,int sender){
        NativeSendMessage(new ChatModel(msg,new int[]{sender}));
    }
    private void NativeSendMessage(ChatModel model){
        Intent intent = new Intent(Config.SEND_MESSAGE);
        intent.putExtra("data",g.toJson(model));
        intent.putExtra("token",Config.ACCESS_BRAODCAST_TOKEN);
        this.sendBroadcast(intent);
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
