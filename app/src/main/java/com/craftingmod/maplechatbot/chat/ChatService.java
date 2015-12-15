package com.craftingmod.maplechatbot.chat;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.anprosit.android.promise.NextTask;
import com.anprosit.android.promise.OnYieldListener;
import com.anprosit.android.promise.Promise;
import com.anprosit.android.promise.Task;
import com.craftingmod.maplechatbot.R;
import com.craftingmod.maplechatbot.command.BaseCommand;
import com.craftingmod.maplechatbot.command.BotState;
import com.craftingmod.maplechatbot.command.Info;
import com.craftingmod.maplechatbot.command.Lotto;
import com.craftingmod.maplechatbot.command.Mail;
import com.craftingmod.maplechatbot.command.Tracker;
import com.craftingmod.maplechatbot.command.oClock;
import com.craftingmod.maplechatbot.hooker.MessageHooker;
import com.craftingmod.maplechatbot.model.ChatModel;
import com.craftingmod.maplechatbot.model.UserModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramBotAdapter;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.response.GetUpdatesResponse;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * Created by superuser on 15/12/12.
 */
public class ChatService extends Service {

    private MyReceiver rec;
    private TelegramBot bot;
    private Gson g;
    private HashMap<String,UserModel> users;
    private HashMap<Integer,Long> lastSaid;

    private Boolean isInitBot = false;
    private HashMap<String,String> mails;

    private ArrayList<BaseCommand> commands;

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
    private int i;
    private void onMessage(final UserModel user,final String msg){

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                bot.sendMessage(24987991,"#" + user.userName + " : " + msg);
                return null;
            }
        }.execute();

        if(msg.startsWith("!")){
            if(lastSaid.containsKey(user.accountID) && (System.nanoTime() - lastSaid.get(user.accountID))/1000000000 < 10){
                return;
            }else{
                lastSaid.put(user.accountID,System.nanoTime());
            }
            for(int i=0;i<commands.size();i+=1){
                commands.get(i).Receive(user,msg);
            }
        }else{
            for(i=0;i<commands.size();i+=1){
                Promise.with(this, null).then(new Task<Object, Object>() {
                    @Override
                    public void run(Object o, NextTask<Object> nextTask) {
                        commands.get(i).onText(user,msg);
                    }
                }).create().execute(null);
            }
        }
    }
    private String uaID;
    public void onReceive(Intent intent) {
        if(intent.getAction().equals("com.craftingmod.INITED_BOT")){

        }else if(intent.hasExtra("data")){
            final ChatModel ch =  g.fromJson(intent.getStringExtra("data"), ChatModel.class);
            Log.d("Maple","messaged received: " + intent.getStringExtra("data"));
            Promise.with(this,Boolean.class).then(new Task<Boolean, UserModel>() {
                @Override
                public void run(Boolean aBoolean, NextTask<UserModel> task) {
                    uaID = ch.SenderAID + "/" + ch.SenderCID;
                    if(users.containsKey(uaID)){
                        if(users.get(uaID).lastTimestamp >= (System.nanoTime()/1000000000)-7200){
                            Log.d("Maple",g.toJson(users.get(uaID)));
                            task.run(users.get(uaID));
                            //return;
                        }else{
                            users.remove(uaID);
                            CharacterLoader cl = new CharacterLoader(task);
                            cl.execute(new UserModel(ch.SenderAID, ch.SenderCID, null));
                        }
                    }else{
                        CharacterLoader cl = new CharacterLoader(task);
                        cl.execute(new UserModel(ch.SenderAID, ch.SenderCID, null));
                    }
                }
            }).then(new Task<UserModel, Boolean>() {
                @Override
                public void run(UserModel userModel, NextTask<Boolean> nextTask) {
                    Log.d("Maple","called onMessage");
                    if(!users.containsKey(uaID)){
                        // 0 is 9..
                        userModel.lastTimestamp = System.nanoTime()/1000000000;
                        users.put(uaID,userModel);
                    }
                    onMessage(userModel,ch.Msg);
                }
            }).create().execute(true);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        g =  new GsonBuilder().create();
        users = new HashMap<>();
        mails = new HashMap<>();

        lastSaid = new HashMap<>();

        commands = new ArrayList<>();
        commands.add(new Info(this));
        commands.add(new Lotto(this));
        commands.add(new Mail(this));
        commands.add(new Tracker(this));
        commands.add(new BotState(this,this));

        bot = TelegramBotAdapter.build("168972296:AAFd11aFp30yFDPDxWnXGbP8x8mknSQL4UM");
        new Timer().scheduleAtFixedRate(new oClock(this), 0, 10000);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic)
                        .setContentTitle("My notification")
                        .setContentText("Hello World!");
        startForeground(5371,mBuilder.build());
        rec = new MyReceiver(this);

        IntentFilter i_f = new IntentFilter();
        i_f.addAction("com.craftingmod.INITED_BOT");
        i_f.addAction("com.craftingmod.MESSAGE_FRIEND");
        registerReceiver(rec, i_f);

        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(rec);
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
