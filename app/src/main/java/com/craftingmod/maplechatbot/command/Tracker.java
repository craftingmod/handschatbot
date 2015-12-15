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
import com.craftingmod.maplechatbot.model.UserModel;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by superuser on 15/12/13.
 */
public class Tracker extends BaseCommand {

    private HashMap<String,Long> connects;

    public Tracker(Context ct) {
        super(ct);
        connects = new HashMap<>();
    }

    @Override
    protected void onCommand(final UserModel um, String cmdName, @Nullable ArrayList<String> args) {
        if(cmdName.equals("seen") || cmdName.equals("최근")){
            if(args.size() >= 1){
                final String user = args.get(0);
                /*
                Promise.with(this, Boolean.class)
                        .then(new Task<Boolean, ArrayList<UserModel>>() {
                            @Override
                            public void run(Boolean aBoolean, NextTask<ArrayList<UserModel>> task) {
                                new CharacterFinder(task).execute("name=\'" + user + "\'");
                            }
                        }).then(new Task<ArrayList<UserModel>, Object>() {
                    @Override
                    public void run(ArrayList<UserModel> userModels, NextTask<Object> nextTask) {
                        Log.d("Maple",userModels.get(0).accountID + "");
                        StringBuilder sb = new StringBuilder();
                        sb.append(user).append("님은 ");
                        if(connects.containsKey(userModels.get(0).accountID)){
                            sb.append(getDeltaTime(connects.get(0)))
                                    .append("전에 친구챗을 했어요.");
                            sendMessage(sb.toString());
                        }else{
                            sb.append("최근 접속한 기록이 없어요. (!uptime 치시면 나오는 시간만큼)");
                            sendMessage(sb.toString());
                        }
                    }
                }).setCallback(new Callback<Object>() {
                    @Override
                    public void onSuccess(Object o) {

                    }

                    @Override
                    public void onFailure(Bundle bundle, Exception e) {
                        sendMessage("유저 조회를 실패했어요.");
                    }
                }).create().execute(true);
                */
                StringBuilder sb = new StringBuilder();
                sb.append(user).append("님은 ");
                if(connects.containsKey(user)){
                    sb.append(getDeltaTime(connects.get(user)))
                            .append("전에 친구챗을 했어요.");
                    sendMessage(sb.toString());
                }else{
                    sb.append("최근 접속한 기록이 없어요. (!uptime 치시면 나오는 시간만큼)");
                    sendMessage(sb.toString());
                }
            }
        }
    }
    @Override
    public void onText(UserModel user,String msg){
        Log.d("Maple",(System.nanoTime()/1000000000)%3600+"");
        Log.d("Maple",user.userName + "");
        connects.put(user.userName,System.nanoTime());
    }

    @Override
    protected void onExit() {

    }
}
