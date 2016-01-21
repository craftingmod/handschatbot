package com.craftingmod.maplechatbot.command;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.anprosit.android.promise.Callback;
import com.anprosit.android.promise.NextTask;
import com.anprosit.android.promise.Promise;
import com.anprosit.android.promise.Task;
import com.craftingmod.maplechatbot.chat.CharacterFinder;
import com.craftingmod.maplechatbot.chat.ISender;
import com.craftingmod.maplechatbot.model.ChatModel;
import com.craftingmod.maplechatbot.model.MailModel;
import com.craftingmod.maplechatbot.model.UserModel;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by superuser on 15/12/13.
 */
public class Tracker extends BaseCommand {

    private HashMap<Integer,Long> connects;

    public Tracker(ISender sd) {
        super(sd);
        Type type = new TypeToken<HashMap<Integer,Long>>(){}.getType();
        String data = this.getDataStr("recent");
        if(data != null){
            connects = g.fromJson(data,type);
        }else{
            connects = new HashMap<>();
        }
    }
    @Override
    protected String[] filter(){
        return new String[]{"seen","최근"};
    }
    @Override
    protected void onText(ChatModel chat,UserModel user,String msg){
        connects.put(user.accountID, System.currentTimeMillis());
    }
    @Override
    public void onSave(){
        Type type = new TypeToken<HashMap<Integer,Long>>(){}.getType();
        this.saveData("recent",g.toJson(connects,type));
    }

    @Override
    protected void onCommand(ChatModel chat, final UserModel user, String cmdName, @Nullable final ArrayList<String> args) {
        if(args.size() == 0){
            args.add(user.userName);
        }
        final String nick = args.get(0);
        Promise.with(this, Boolean.class).then(new Task<Boolean, HashMap<String,String>>() {
            @Override
            public void run(Boolean aBoolean, NextTask<HashMap<String,String>> task) {
                HashMap<String,String> map = new HashMap<>();
                map.put("name",nick);
                task.run(map);
            }
        }).then(new CharacterFinder())
        .then(new Task<ArrayList<UserModel>, Integer>() {
            @Override
            public void run(ArrayList<UserModel> userModels, NextTask<Integer> nextTask) {
                if (userModels.size() == 0) {
                    nextTask.fail(null, null);
                } else {
                    nextTask.run(userModels.get(0).accountID);
                }
            }
        }).setCallback(new Callback<Integer>() {
            @Override
            public void onSuccess(Integer pam) {
                if (connects.containsKey(pam)) {
                    sendMessage(nick + "님은 " + getDeltaTime(connects.get(pam)) + "전에 말하였어요.", user.accountID);
                } else {
                    this.onFailure(null, null);
                }
            }
            @Override
            public void onFailure(Bundle bundle, Exception e) {
                sendMessage(nick + "님의 기록을 찾을 수 없어요.",user.accountID);
            }
        }).create().execute(true);
    }
}
