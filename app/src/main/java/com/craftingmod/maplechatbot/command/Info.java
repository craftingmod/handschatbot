package com.craftingmod.maplechatbot.command;

import android.support.annotation.Nullable;

import com.anprosit.android.promise.NextTask;
import com.anprosit.android.promise.Promise;
import com.anprosit.android.promise.Task;
import com.craftingmod.maplechatbot.MapleUtil;
import com.craftingmod.maplechatbot.chat.ISender;
import com.craftingmod.maplechatbot.chat.SQLFinder;
import com.craftingmod.maplechatbot.model.ChatModel;
import com.craftingmod.maplechatbot.model.FriendModel;
import com.craftingmod.maplechatbot.model.UserModel;
import com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by superuser on 16/1/31.
 */
public class Info extends BaseCommand {
    public Info(ISender sender) {
        super(sender);
    }
    @Override
    protected String[] filter(){
        return new String[]{"info"};
    }

    @Override
    protected void onCommand(ChatModel chat, final UserModel user, String cmdName, @Nullable final ArrayList<String> args) {
        final String type = args.get(0);
        if(args.size() >= 2){
            try{
                Integer.parseInt(args.get(1));
            }catch (Exception e){
                return;
            }
        }
        if(type.equalsIgnoreCase("uid")){
            Promise.with(this, Boolean.class).then(new Task<Boolean, UserModel>() {
                @Override
                public void run(Boolean aBoolean, NextTask<UserModel> nextTask) {
                    int cid;
                    if(args.size() >= 2){
                        cid = Integer.parseInt(args.get(1));
                    }else{
                        cid = user.characterID;
                    }
                    MapleUtil.getInstance().getCharacterInfo(user.worldID, cid, nextTask);
                }
            }).then(new Task<UserModel, Void>() {
                @Override
                public void run(UserModel model, NextTask<Void> nextTask) {
                    sendMessage("accountID: " + model.accountID + " userName: " + model.userName, user.accountID);
                    nextTask.yield(0,null);
                }
            }).create().execute(true);
        }
        if(type.equalsIgnoreCase("friendlist")){
            final int aid;
            if(args.size() >= 2){
                aid = Integer.parseInt(args.get(1));
            }else{
                aid = user.accountID;
            }
            Promise.with(this,Boolean.class).then(new Task<Boolean, ArrayList<FriendModel>>() {
                @Override
                public void run(Boolean aBoolean, NextTask<ArrayList<FriendModel>> nextTask) {
                    MapleUtil.getInstance().getAccountFriendList(Integer.parseInt(args.get(1)), nextTask);
                }
            }).then(new Task<ArrayList<FriendModel>, Void>() {
                @Override
                public void run(ArrayList<FriendModel> friendModels, NextTask<Void> nextTask) {
                    ArrayList<String> list = new ArrayList<>();
                    for (int i = 0; i < friendModels.size(); i += 1) {
                        if (friendModels.get(i).worldID == user.worldID) {
                            list.add(friendModels.get(i).nickname);
                        }
                    }
                    String result =  Joiner.on(",").skipNulls().join(list);
                    sendMessage("AID " + (aid + "") + "의 친구목록: " + result, user.accountID);
                    nextTask.yield(0, null);
                }
            }).create().execute(true);
        }
        if(type.equalsIgnoreCase("online")){
            final String uname;
            if(args.size() >= 2){
                uname = args.get(1);
            }else{
                uname = user.userName;
            }
            Promise.with(this,Boolean.class).then(new Task<Boolean, HashMap<String,String>>() {
                @Override
                public void run(Boolean aBoolean, NextTask<HashMap<String, String>> nextTask) {
                    HashMap<String,String> map = new HashMap<String, String>();
                    map.put("name",uname);
                    nextTask.run(map);
                }
            }).then(new SQLFinder()).then(new Task<ArrayList<UserModel>, Boolean>() {
                @Override
                public void run(ArrayList<UserModel> models, NextTask<Boolean> nextTask) {
                    if (models.size() < 1) {
                        sendMessage("유저를 찾을 수 없습니다.", user.accountID);
                        nextTask.yield(0, null);
                        return;
                    }
                    MapleUtil.getInstance().isGameLogin(models.get(0).accountID, nextTask);
                }
            }).then(new Task<Boolean, Void>() {
                @Override
                public void run(Boolean on, NextTask<Void> nextTask) {
                    if (on) {
                        sendMessage("온라인.",user.accountID);
                    }else{
                        sendMessage("오프라인.",user.accountID);
                    }
                    nextTask.yield(0, null);
                }
            }).create().execute(true);
        }
    }
}
