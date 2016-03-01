package com.craftingmod.maplebot.heavy.modules;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.util.Log;

import com.craftingmod.maplebot.MapleUtil;
import com.craftingmod.maplebot.heavy.IServiceHeavy;
import com.craftingmod.maplebot.model.CModel;
import com.craftingmod.maplebot.model.ChatModel;
import com.craftingmod.maplebot.model.CoreUserModel;
import com.craftingmod.maplebot.model.SimpleUserModel;
import com.craftingmod.maplebot.model.UserModel;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

/**
 * Created by superuser on 16/2/15.
 */
public abstract class HeavyModule {
    private HandlerThread thread;
    protected Handler handler;
    protected Gson g;

    protected Context context;
    protected IServiceHeavy service;
    public HeavyModule(IServiceHeavy mInterface){
        service = mInterface;
        context = service.getContext();
        if(name() == null){
            thread = new HandlerThread("handler" + (long)(Math.random()*Long.MAX_VALUE));
        }else{
            thread = new HandlerThread("handler" + name().toLowerCase());
        }
        thread.start();
        g = new GsonBuilder().create();
        handler = new Handler(thread.getLooper());
    }

    /**
     * invoke when any message
     * Main Thread / In handler
     */
    public void handleMessage(CModel model){}

    /**
     * invoke like light command
     * Main Thread / In handler
     * @param chat
     */
    public void handleCommand(CModel chat){
        ArrayList<String> split = split(chat.text);
        handleCommand(chat,split);
    }

    /**
     * Handle command in main thread
     * Use soft!
     * MainThread / Handler
     * @param chat
     * @param splits
     */
    protected abstract void handleCommand(CModel chat,ArrayList<String> splits);

    /**
     * Handle command in sub thread
     * use network etc..
     * SubThread / Handler
     * @param chat
     * @param splits
     * @param say
     * @param target
     */
    protected abstract void handleCommand(CModel chat,ArrayList<String> splits,UserModel say,@Nullable UserModel target);

    /**
     * Handle command
     * Sub Thread / In Handler
     * @param chat
     * @param target
     */

    public void handleCommand(final CModel chat,final @Nullable SimpleUserModel target){
        handler.post(new Runnable() {
            @Override
            public void run() {
                try{
                    UserModel sender = service.getUser(chat.worldID,chat.characterID);
                    ArrayList<String> split = split(chat.text);
                    if(sender == null){
                        sender = MapleUtil.getCharacterInfo(chat.worldID,chat.characterID);
                        if(sender != null){
                            service.putUser(sender);
                        }
                    }
                    if(target == null){
                        handleCommand(chat,split,sender,null);
                        return;
                    }else{
                        UserModel search = service.getUser(target.worldID,target.characterID);
                        if(search == null){
                            search = MapleUtil.getCharacterInfo(target.worldID,target.characterID);
                            Log.d("Result",g.toJson(search));
                            if(search != null){
                                service.putUser(search);
                                service.writeUser(search);
                            }
                        }
                        handleCommand(chat,split,sender,search);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }
    protected void sendMessage(CModel chat,String msg){
        service.sendMessage(chat, msg);
    }
    public void handleTimeout(CModel chat){
        sendMessage(chat,"응답 없음!");
    }
    /**
     * Request to heavy modification
     * @param chat
     * @param username
     */
    protected void requestWithUser(CModel chat,String username){
        service.request(this,chat,username);
    }
    protected ArrayList<String> split(String msg){
        return Lists.newArrayList(Splitter.on(" ").omitEmptyStrings().trimResults().split(msg));
    }

    /**
     * invoke when know handle Message
     * Main Thread / No handler
     * @param model
     * @return
     */
    public boolean shouldMessage(CModel model){
        return false;
    }

    public boolean shouldCommand(CModel model){
        ArrayList<String> filter = filter();
        String msg = model.text;
        ArrayList<String> splits = split(msg);
        if(msg.startsWith("!")){
            String cmd = splits.get(0);
            boolean contain = false;
            for(String white : filter){
                if(cmd.equalsIgnoreCase("!" + white)){
                    contain = true;
                }
            }
            return contain;
        }else{
            return false;
        }
    }
    protected abstract ArrayList<String> filter();
    protected abstract String[] help();
    public String[] getHelp(){
        return help();
    }
    public String getName(){
        return name();
    }
    protected abstract String name();
}
