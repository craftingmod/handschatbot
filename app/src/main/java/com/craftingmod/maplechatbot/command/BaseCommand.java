package com.craftingmod.maplechatbot.command;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.craftingmod.maplechatbot.model.UserModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Console;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by superuser on 15/12/13.
 */
public abstract class BaseCommand {
    protected int i;
    protected Context context;
    protected SharedPreferences sp;
    protected Gson g;
    public boolean slient = false;

    public BaseCommand(Context ct){
        context = ct;
        sp = context.getSharedPreferences("data",Context.MODE_PRIVATE);
        g = new GsonBuilder().create();
    }
    protected HashMap restoreData(String key){
        String data = sp.getString("data", "null");
        if(data == null){
            return new HashMap<>();
        }else{
            return g.fromJson(data,HashMap.class);
        }
    }
    protected void saveData(String key,Object obj){
        SharedPreferences.Editor edit = sp.edit();
        edit.putString(key,g.toJson(obj));
        edit.commit();
    }
    public void Receive(UserModel user,String fullString){
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
                    onCommand(user,str[0].substring(1),args);
                    break;
                }
            }
        }else{
            onText(user,fullString);
        }
    }
    protected static void sendMessage(Context ct,String msg){
        Log.d("Maple", msg);
        if(1 == 1) return;
        Intent intent = new Intent("com.craftingmod.SEND_MSG");
        intent.putExtra("msg","- " + msg);
        ct.sendBroadcast(intent);
    }
    protected void sendMessage(String msg){
        if(slient) return;
        Intent intent = new Intent("com.craftingmod.SEND_MSG");
        intent.putExtra("msg","- " + msg);
        context.sendBroadcast(intent);
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
        long sec = (System.nanoTime() - backtime)/1000000000;
        int subSec = (int) Math.floor(sec % 10);
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

    public abstract void onText(UserModel user,String msg);
    protected String[] filter(){
        return new String[]{"\\*"};
    }
    protected abstract void onCommand(UserModel user,String cmdName,@Nullable ArrayList<String> args);
    protected abstract void onExit();
}
