package com.craftingmod.maplebot.heavy.modules;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.util.Log;

import com.anprosit.android.promise.Callback;
import com.anprosit.android.promise.NextTask;
import com.anprosit.android.promise.Promise;
import com.anprosit.android.promise.Task;
import com.craftingmod.maplebot.BroadUtil;
import com.craftingmod.maplebot.Config;
import com.craftingmod.maplebot.MapleUtil;
import com.craftingmod.maplebot.communicate.BaseSocket;
import com.craftingmod.maplebot.communicate.ResultObject;
import com.craftingmod.maplebot.heavy.IServiceHeavy;
import com.craftingmod.maplebot.heavy.core.SQLFinder;
import com.craftingmod.maplebot.heavy.core.UserDB;
import com.craftingmod.maplebot.model.CModel;
import com.craftingmod.maplebot.model.CoreUserModel;
import com.craftingmod.maplebot.model.FriendModel;
import com.craftingmod.maplebot.model.OnlineModel;
import com.craftingmod.maplebot.model.SimpleUserModel;
import com.craftingmod.maplebot.model.UserModel;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;

import android.os.Handler;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by superuser on 16/2/16.
 */
public class Online extends HeavyModule {
    private BaseSocket api;
    private Timer timer;
    private ArrayList<UserModel> connects;

    private HandlerThread threadCheck;
    private Handler handlerCheck;


    public Online(IServiceHeavy mInterface) {
        super(mInterface);
        api = BaseSocket.buildClient(context,"onlineinfo",2000,180*1000);
        connects = new ArrayList<>();
        threadCheck = new HandlerThread("CheckOnlineThread");
        threadCheck.start();
        handlerCheck = new Handler(threadCheck.getLooper());

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                init_Online();
            }
        },0,30*1000);

    }
    protected void init_Online(){
        handlerCheck.post(new Runnable() {
            @Override
            public void run() {
                ArrayList<FriendModel> myFriend = MapleUtil.getAccountFriendList(Config.MASTER_ACCOUNT_ID);
                ArrayList<FriendModel> search = new ArrayList<>();
                for(int i=0;i<myFriend.size();i+=1){
                    final FriendModel fr = myFriend.get(i);
                    if(fr.worldID == Config.WORLD_BOT_ID){
                        Boolean onlineA = MapleUtil.isGameLogin(fr.accountID);
                        if(onlineA){
                            search.add(fr);
                        }
                    }
                }
                final ArrayList<CoreUserModel> send = new ArrayList<>();
                for(FriendModel model : search){
                    final HashMap<String,String> str = new HashMap<>();
                    str.put("aid",model.accountID + "");
                    Cursor sql = service.getDB().rawSQL("SELECT * from 'masterCharacter' WHERE accountID='" + model.accountID +"'");
                    sql.moveToFirst();
                    Log.d("접속자",sql.getString(2));
                    ArrayList<UserModel> partU = service.getDB().searchUser(str);
                    for(UserModel partLoop : partU){
                        send.add(new CoreUserModel(partLoop.accountID,partLoop.characterID));
                    }
                }
                handlerCheck.post(new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<CoreUserModel> onlines = new ArrayList<>();
                        loop_Online(send,onlines,0,handlerCheck);
                    }
                });
            }
        });
    }
    protected void loop_Online(final ArrayList<CoreUserModel> send,final ArrayList<CoreUserModel> onlines,final int num,final Handler handle){
        Promise.with(this,Integer.class).then(new Task<Integer, ResultObject>() {
            @Override
            public void run(Integer it, NextTask<ResultObject> nextTask) {
                final Type type = new TypeToken<ArrayList<CoreUserModel>>(){}.getType();
                ArrayList<CoreUserModel> input = new ArrayList<>();
                final int max = Math.min(send.size(),(it+1)*15);
                for(int i=it*15;i<max;i+=1){
                    input.add(send.get(i));
                }
                Log.d("SendModel",g.toJson(input,type));
                api.request("*",g.toJson(input,type),nextTask);
            }
        }).then(new Task<ResultObject, Boolean>() {
            @Override
            public void run(final ResultObject resultObject, final NextTask<Boolean> nextTask) {
                handle.post(new Runnable() {
                    @Override
                    public void run() {
                        if(resultObject.data != null){
                            Log.d("Result",resultObject.data);
                            Type type = new TypeToken<ArrayList<OnlineModel>>(){}.getType();
                            ArrayList<OnlineModel> models = g.fromJson(resultObject.data,type);
                            for(OnlineModel model : models){
                                if(model.online && model.characterID > 0){
                                    onlines.add(new CoreUserModel(model.accountID,model.characterID));
                                }
                            }
                            nextTask.run(models.size() < 15);
                        }else{
                            nextTask.yield(0,null);
                        }
                    }
                });
            }
        }).setCallback(new Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean bl) {
                if(Math.min(send.size(),(num+1)*15) >= send.size() || bl){
                    handle.post(new Runnable() {
                        @Override
                        public void run() {
                            result_Online(onlines);
                        }
                    });
                }else{
                    handle.post(new Runnable() {
                        @Override
                        public void run() {
                            loop_Online(send,onlines, num+1,handle);
                        }
                    });
                }
            }
            @Override
            public void onFailure(Bundle bundle, Exception e) {}
        }).create().execute(num);
    }
    protected void result_Online(ArrayList<CoreUserModel> users){
        Log.d("Online","Finished.");
        ArrayList<UserModel> online = new ArrayList<>();
        for(CoreUserModel user : users){
            ArrayList<UserModel> lp = service.getDB().searchUser(SQLFinder.getSearch(user.accountID,user.characterID));
            if(lp.size() == 1){
                Log.d("Online",lp.get(0).userName);
                online.add(lp.get(0));
            }
        }
        ArrayList<String> newP = new ArrayList<>();
        ArrayList<String> rmP = new ArrayList<>();
        for(UserModel user : online){
            // user : 지금 접한사람
            if(!connects.contains(user)){
                newP.add(user.userName);
            }
        }
        for(UserModel user : connects){
            // user : 접했던 사람
            if(!online.contains(user)){
                rmP.add(user.userName);
            }
        }
        if(connects.size() == 0){
            sendMessage(new CModel(Config.bot,true),"지금 접한사람 목록 - " + Joiner.on(" ").join(newP));
        }else{
            if(rmP.size() >= 1){
                sendMessage(new CModel(Config.bot,true),Joiner.on(",").join(rmP) + "님이 접속을 종료하였습니다.");
            }
            if(newP.size() >= 1){
                sendMessage(new CModel(Config.bot,true),Joiner.on(",").join(newP) + "님이 접하셨습니다.");
            }
        }
        connects = online;
    }

    @Override
    protected void handleCommand(CModel chat, ArrayList<String> splits) {
        if(splits.size() == 1 && chat.accountID == Config.MASTER_ACCOUNT_ID){
            ArrayList<String> names = new ArrayList<>();
            for(UserModel model : connects){
                names.add(model.userName);
            }
            sendMessage(chat,"현재 접한사람: " + Joiner.on(" ").join(names));
        }
    }

    @Override
    protected void handleCommand(CModel chat, ArrayList<String> splits, UserModel say, @Nullable UserModel target) {

    }

    @Override
    protected ArrayList<String> filter() {
        return Lists.newArrayList(new String[]{"online"});
    }

    @Override
    protected String[] help() {
        return new String[]{"!online : 현재 접속중인 유저들을 출력합니다. (만든 사람만 사용가능)"};
    }

    @Override
    protected String name() {
        return "CheckOnline";
    }
}
