package com.craftingmod.maplechatbot.command;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import org.apache.commons.codec.binary.Base64;
import android.util.Log;

import com.craftingmod.maplechatbot.Config;
import com.craftingmod.maplechatbot.chat.ISender;
import com.craftingmod.maplechatbot.chat.UserDB;
import com.craftingmod.maplechatbot.model.ChatModel;
import com.craftingmod.maplechatbot.model.UserModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.Console;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by superuser on 15/12/13.
 */
public abstract class BaseCommand {
    protected int i;
    protected Context context;
    protected ISender api;
    protected UserDB db;
    protected SharedPreferences sp;
    protected Gson g;
    public boolean slient = false;

    public BaseCommand(ISender sender){
        context = sender.getContext();
        api = sender;
        sp = context.getSharedPreferences("data",Context.MODE_PRIVATE);
        g = new GsonBuilder().create();
        db = sender.getDB();
    }
    protected String getDataStr(String key){
        String data = sp.getString(key, "null");
        if(data == "null"){
            return null;
        }else{
            return  new String(Base64.decodeBase64(data.getBytes()));
        }
    }
    protected void saveData(String key,String json){
        SharedPreferences.Editor edit = sp.edit();

        Log.d("MapleChatbot", "Save key: " + key + " / Data: " + json);
        String encoded = new String(Base64.encodeBase64(json.getBytes()));
        edit.putString(key,encoded);
        edit.apply();
    }
    public void Receive(final ChatModel chat,final UserModel user,final String fullString){
        new Handler(context.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(fullString.startsWith("!")){
                    String[] str = fullString.split("\\s+");
                    String cmd = str[0].substring(1);
                    String[] filters = filter();
                    for(i=0;i<filters.length;i+=1){
                        if(filters[i].equalsIgnoreCase("\\*") || filters[i].equalsIgnoreCase(cmd)){
                            ArrayList<String> args = new ArrayList<>();
                            if(str.length >= 2) {
                                for(i=1;i<str.length;i+=1){
                                    args.add(str[i]);
                                }
                            }
                            onCommand(chat,user,str[0].substring(1),args);
                            break;
                        }
                    }
                }else{
                    if(user.characterID != Config.TELEGRAM_EMU_USERID){
                        onText(chat,user,fullString);
                    }
                }
                if(user.characterID != Config.TELEGRAM_EMU_USERID){
                    onEvent(chat, user, fullString);
                }
            }
        });
    }
    public void callTelegram(String cmd){
        ChatModel emuChat = new ChatModel(0,0,Config.TELEGRAM_EMU_USERID,Config.TELEGRAM_EMU_USERID,Config.TELEGRAM_EMU_USERID,Config.WORLD_BOT_ID,"!" + cmd);
        UserModel emuUser = new UserModel(Config.TELEGRAM_EMU_USERID,Config.TELEGRAM_EMU_USERID,Config.TELEGRAM_EMU_USERNAME);
        this.Receive(emuChat,emuUser,"!" + cmd);
    }
    public void sendMessage(String msg,int sender){
        api.sendMessage(msg, sender);
       // NativeSendMessage(new ChatModel(msg, new int[]{sender}));
    }
    public void sendMessageAll(String msg){
        api.sendMessageAll(msg);
        // NativeSendMessage(new ChatModel(msg, new int[]{sender}));
    }

    protected String grap(ArrayList<String> ar,int start){
        if(start >= 1){
            for(i=0;i<start;i+=1){
                ar.remove(i);
            }
        }
        return TextUtils.join(" ",ar);
    }
    protected String getDeltaTime(long backtime){
        StringBuilder out = new StringBuilder();
        long sec = (System.currentTimeMillis() - backtime)/100;
        int subSec = (int) (sec % 10);
        sec = (long) Math.floor(sec / 10);
        if(sec >= 2592000){
            out.append((int)Math.floor(sec/2592000) + "달 ");
            sec = sec % 2592000;
        }
        if(sec >= 86400){
            out.append((int)Math.floor(sec/86400) + "일 ");
            sec = sec % 86400;
        }
        if(sec >= 3600){
            out.append((int)Math.floor(sec/3600) + "시간 ");
            sec = sec % 3600;
        }
        if(sec >= 60){
            out.append((int)Math.floor(sec/60) + "분 ");
            sec = sec % 60;
        }
        out.append((int)Math.floor(sec) + "." + subSec + "초");
        return out.toString();
    }

    protected void onText(ChatModel chat,UserModel user,String msg){}
    protected void onEvent(ChatModel chat,UserModel user,String msg){}
    protected abstract String[] filter();
    protected abstract void onCommand(ChatModel chat,UserModel user,String cmdName,@Nullable ArrayList<String> args);
    public void onSave(){}
}
