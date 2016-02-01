package com.craftingmod.maplechatbot.command;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.anprosit.android.promise.Callback;
import com.anprosit.android.promise.NextTask;
import com.anprosit.android.promise.Promise;
import com.anprosit.android.promise.Task;
import com.craftingmod.maplechatbot.MapleUtil;
import com.craftingmod.maplechatbot.chat.CharacterFinder;
import com.craftingmod.maplechatbot.chat.ISender;
import com.craftingmod.maplechatbot.model.ChatModel;
import com.craftingmod.maplechatbot.model.SimpleUserModel;
import com.craftingmod.maplechatbot.model.UserModel;
import com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by superuser on 16/1/21.
 */
public class UserInfo extends BaseCommand {
    public UserInfo(ISender sd) {
        super(sd);
    }

    @Override
    protected String[] filter(){
        return new String[]{"user","image"};
    }

    @Override
    protected void onCommand(ChatModel chat, final UserModel user, final String cmdName, @Nullable ArrayList<String> args) {
        if(args.size() != 1){
            this.sendMessage("사용법: !"+cmdName+" <유저이름>",user.accountID);
            return;
        }
        final String name = args.get(0);
        Promise.with(this,String.class)
                .then(new Task<String, HashMap<Integer,String>>() {
                    @Override
                    public void run(String s, NextTask<HashMap<Integer, String>> nextTask) {
                        Log.d("Maple", s);
                        nextTask.run(CharacterFinder.getSearch(s));
                    }
                }).then(CharacterFinder.getInstance()).then(new Task<ArrayList<UserModel>, ArrayList<Integer>>() {
            @Override
            public void run(ArrayList<UserModel> userModels, NextTask<ArrayList<Integer>> nextTask) {
                if (userModels.size() != 1) {
                    sendMessage(name + "를 찾을수 없습니다.", user.accountID);
                } else {
                    if (cmdName.equals("image")) {
                        sendMessage(name + " : " + userModels.get(0).userImage, user.accountID);
                        nextTask.yield(0, null);
                    } else {
                        ArrayList<Integer> pam = new ArrayList<Integer>();
                        pam.add(userModels.get(0).accountID);
                        pam.add(userModels.get(0).worldID);
                        nextTask.run(pam);
                    }
                }
            }
        }).then(new Task<ArrayList<Integer>, ArrayList<SimpleUserModel>>() {
            @Override
            public void run(ArrayList<Integer> itg, NextTask<ArrayList<SimpleUserModel>> nextTask) {
                Log.d("Maple",itg.get(0) + "/" + itg.get(1));
                new MapleUtil().getCharacterList(itg.get(0), itg.get(1), nextTask);
            }
        }).then(new Task<ArrayList<SimpleUserModel>, Void>() {
            @Override
            public void run(ArrayList<SimpleUserModel> sModels, NextTask<Void> nextTask) {
                Log.d("Maple","Hello!!!!");
                ArrayList<String> names = new ArrayList<>();
                SimpleUserModel model = null;
                for (int i = 0; i < sModels.size(); i += 1) {
                    if (!sModels.get(i).userName.equalsIgnoreCase(name)) {
                        names.add(sModels.get(i).userName);
                    } else {
                        model = sModels.get(i);
                    }
                }
                sendMessage("accountID: " + model.accountID + " characterID: " + model.characterID + " / " + Joiner.on(" ").join(names), user.accountID);
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
        }).create().execute(name);
                /*
                .then(CharacterFinder.getInstance()).then(new Task<ArrayList<UserModel>, Void>() {
            @Override
            public void run(ArrayList<UserModel> userModels, NextTask<Void> nextTask) {
                ArrayList<String> names = new ArrayList<>();
                UserModel model = null;
                for (int i = 0; i < userModels.size(); i += 1) {
                    if (!userModels.get(i).userName.equalsIgnoreCase(name)) {
                        names.add(userModels.get(i).userName);
                    } else {
                        model = userModels.get(i);
                    }
                }
                sendMessage("accountID: " + userModels.get(0).accountID + " characterID: " + model.characterID + " / Image: " + (model.userImage == null ? "없음" : model.userImage) + " / " + Joiner.on(" ").join(names), user.accountID);
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
        }).create().execute(name);
        */
    }
}
