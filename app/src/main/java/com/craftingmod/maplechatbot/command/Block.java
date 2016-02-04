package com.craftingmod.maplechatbot.command;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.anprosit.android.promise.Callback;
import com.anprosit.android.promise.NextTask;
import com.anprosit.android.promise.Promise;
import com.anprosit.android.promise.Task;
import com.craftingmod.maplechatbot.Config;
import com.craftingmod.maplechatbot.chat.CharacterFinder;
import com.craftingmod.maplechatbot.chat.ISender;
import com.craftingmod.maplechatbot.chat.SQLFinder;
import com.craftingmod.maplechatbot.model.ChatModel;
import com.craftingmod.maplechatbot.model.MailModel;
import com.craftingmod.maplechatbot.model.UserModel;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.codec.binary.Base64;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by superuser on 16/1/18.
 */
public class Block extends BaseCommand {

    private ArrayList<Integer> blockList;
    private Type typeI = new TypeToken<ArrayList<Integer>>(){}.getType();

    public Block(ISender sd) {
        super(sd);
        String data = this.getDataStr("blocks");
        blockList = new ArrayList<>();
        if(data != null){
            blockList = g.fromJson(data,typeI);
        }
    }
    public Boolean isBlocked(int aid){
        for(int i=0;i<blockList.size();i+=1){
            if(blockList.get(i) == aid){
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onCommand(final ChatModel chat, final UserModel user,final String cmdName, @Nullable ArrayList<String> args) {
        if(args.size() == 1 && user.accountID == Config.MASTER_ACCOUNT_ID){
            Promise.with(this,String.class)
                    .then(new Task<String, ArrayList<UserModel>>() {
                        @Override
                        public void run(String s, NextTask<ArrayList<UserModel>> nextTask) {
                            nextTask.run(db.searchUser(SQLFinder.getSearch(s)));
                        }
                    }).then(new Task<ArrayList<UserModel>, Integer>() {
                @Override
                public void run(ArrayList<UserModel> userModels, NextTask<Integer> nextTask) {
                    final int aid = userModels.get(0).accountID;
                    if (cmdName.equalsIgnoreCase("unblock")) {
                        blockList.remove(aid);
                    } else {
                        blockList.add(aid);
                    }
                    nextTask.run(aid);
                }
            }).setCallback(new Callback<Integer>() {
                @Override
                public void onSuccess(Integer pam) {
                    sendMessage("AID: " + pam + " 성공", chat.SenderAID);
                }

                @Override
                public void onFailure(Bundle bundle, Exception e) {
                    if(e != null){
                        e.printStackTrace();
                    }
                    sendMessage("캐릭터 이름 조회에 실패하였습니다.", chat.SenderAID);
                }
            }).create().execute(args.get(0));
        }
    }

    @Override
    public void onSave() {
        this.saveData("blocks", g.toJson(blockList,typeI));
    }

    @Override
    protected String[] filter(){
        return new String[]{"block","out","unblock"};
    }
}
