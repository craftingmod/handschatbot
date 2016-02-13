package com.craftingmod.maplebot.light.modules;

/**
 * Created by superuser on 16/2/13.
 */
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.craftingmod.maplebot.BroadUtil;
import com.craftingmod.maplebot.light.IService;
import com.craftingmod.maplebot.model.ChatModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

public abstract class CoreModule extends Handler {
    protected IService service;
    protected Context context;
    protected Gson g;
    public CoreModule(IService sender){
        super(sender.getLooper());
        service = sender;
        context = service.getContext();
        g = new GsonBuilder().create();
    }
    protected void sendFMessage(ChatModel user,String message){
        service.sendFMessage(user,message);
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        ChatModel chat = (ChatModel) msg.obj;
        String[] messages = chat.Msg.trim().split("\\s+");
        ArrayList<String> args = new ArrayList<>();
        for(int i=1;i<messages.length;i+=1){
            args.add(messages[i]);
        }
        if(msg.arg1 == BroadUtil.TYPE_FRIEND){
            sendFMessage(chat,this.onCommand(chat,messages[0],args));
        }else if(msg.arg1 == BroadUtil.TYPE_GUILD){
            // ?
        }else{
            Log.e("??","??!");
        }
    }
    public boolean shouldExec(String text){
        String[] filters = filter();
        String[] messages = text.trim().split("\\s+");
        for(String fil : filters){
            if(fil.equalsIgnoreCase(messages[0])){
                return true;
            }
        }
        return false;
    }
    protected String grap(ArrayList<String> ar,int start){
        if(start >= 1){
            for(int i=0;i<start;i+=1){
                ar.remove(i);
            }
        }
        return TextUtils.join(" ", ar);
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

    protected abstract String onCommand(ChatModel chat,String cmd,ArrayList<String> args);
    public abstract String getName();
    protected abstract String[] filter();
}
