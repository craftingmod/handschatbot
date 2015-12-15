package com.craftingmod.maplechatbot.command;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.anprosit.android.promise.NextTask;
import com.anprosit.android.promise.Promise;
import com.anprosit.android.promise.Task;
import com.craftingmod.maplechatbot.chat.CharacterFinder;
import com.craftingmod.maplechatbot.model.UserModel;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by superuser on 15/12/13.
 */
public class Info extends BaseCommand {

    private boolean inited = false;
    private long startTime = System.nanoTime();

    public Info(Context ct) {
        super(ct);
    }
    @Override
    protected void onCommand(UserModel user, String cmdName, @Nullable final ArrayList<String> args) {
        if(cmdName.equals("help")){

        }else
        if(cmdName.equals("version")){
            sendMessage("1.0 (20151213)");
        }else
        if(cmdName.equals("uptime")){
            sendMessage(this.getDeltaTime(startTime));
        }else
        if(cmdName.equals("me")){
            sendMessage(user.userName + " : " + user.level + " " + user.job + ", ID: " + user.characterID);
        }else
        if(cmdName.equals("dinfo")){
            Promise.with(this,null).then(new Task<Object, ArrayList<UserModel>>() {
                @Override
                public void run(Object o, NextTask<ArrayList<UserModel>> nextTask) {
                    new CharacterFinder(nextTask).execute(args.get(0));
                }
            }).then(new Task<ArrayList<UserModel>, Object>() {
                @Override
                public void run(ArrayList<UserModel> userModels, NextTask<Object> nextTask) {
                    sendMessage(g.toJson(userModels.get(0)));
                }
            }).create().execute(null);
        }else
        if(cmdName.equals("info")){
            if(args.size() < 1) return;
            Promise.with(this,null).then(new Task<Object, ArrayList<UserModel>>() {
                @Override
                public void run(Object o, NextTask<ArrayList<UserModel>> nextTask) {
                    new CharacterFinder(nextTask).execute("nick=\'"+ args.get(0) + "\'");
                }
            }).then(new Task<ArrayList<UserModel>, Object>() {
                @Override
                public void run(ArrayList<UserModel> userModels, NextTask<Object> nextTask) {
                    sendMessage(g.toJson(userModels.get(0)));
                }
            }).create().execute(null);
        }

    }
    @Override
    public void onText(UserModel user,String msg){
        if(!inited){
            inited = true;
            startTime = System.nanoTime();
        }
    }
    @Override
    protected void onExit() {

    }
}
