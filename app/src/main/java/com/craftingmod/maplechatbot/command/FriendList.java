package com.craftingmod.maplechatbot.command;

import android.support.annotation.Nullable;

import com.anprosit.android.promise.NextTask;
import com.anprosit.android.promise.Promise;
import com.anprosit.android.promise.Task;
import com.craftingmod.maplechatbot.MapleUtil;
import com.craftingmod.maplechatbot.chat.ISender;
import com.craftingmod.maplechatbot.model.ChatModel;
import com.craftingmod.maplechatbot.model.FriendModel;
import com.craftingmod.maplechatbot.model.SimpleUserModel;
import com.craftingmod.maplechatbot.model.UserModel;
import com.google.common.base.Joiner;

import java.util.ArrayList;

/**
 * Created by superuser on 16/1/25.
 */
public class FriendList extends BaseCommand {

    @Override
    protected String[] filter(){
        return new String[]{"friendlist","친구목록"};
    }

    public FriendList(ISender sender) {
        super(sender);
    }

    @Override
    protected void onCommand(ChatModel chat, final UserModel user, String cmdName, @Nullable final ArrayList<String> args) {
        final int start;
        if(args.size() == 2){
            return;
        }else{
            try{
                start = Integer.parseInt(args.get(0));
            }catch (Exception e){
                return;
            }
        }
        Promise.with(this,Boolean.class).then(new Task<Boolean, ArrayList<FriendModel>>() {
            @Override
            public void run(Boolean aBoolean, NextTask<ArrayList<FriendModel>> nextTask) {
                if(args.size() >= 2){
                    MapleUtil.getInstance().getAccountFriendList(user.accountID,nextTask);
                }else{
                    nextTask.run(null);
                }
            }
        }).then(new Task<ArrayList<FriendModel>, Integer>() {
            @Override
            public void run(ArrayList<FriendModel> friendModels, NextTask<Integer> nextTask) {
                if (args.size() == 1) {
                    nextTask.run(user.accountID);
                } else {
                    Boolean find = false;
                    for (int i = 0; i < friendModels.size(); i += 1) {
                        final FriendModel model = friendModels.get(i);
                        if (model.nickname.equalsIgnoreCase(args.get(0)) || model.charName.equalsIgnoreCase(args.get(0))) {
                            nextTask.run(model.accountID);
                            find = true;
                            break;
                        }
                    }
                    if (!find) {
                        sendMessage("유저를 찾을수 없습니다.", user.accountID);
                        nextTask.yield(0, null);
                    }
                }
            }
        }).then(new Task<Integer, ArrayList<FriendModel>>() {
            @Override
            public void run(Integer integer, NextTask<ArrayList<FriendModel>> nextTask) {
                MapleUtil.getInstance().getAccountFriendList(integer,nextTask);
            }
        }).then(new Task<ArrayList<FriendModel>, Void>() {
            @Override
            public void run(ArrayList<FriendModel> friendModels, NextTask<Void> nextTask) {
                ArrayList<String> names = new ArrayList<>();
                FriendModel model = null;

                for (int i = start; i < friendModels.size() && i < start+10; i += 1) {
                    names.add(friendModels.get(i).charName);
                }
                sendMessage("친구 목록: " + Joiner.on(" ").join(names),user.accountID);
                nextTask.yield(0,null);
            }
        }).create().execute(true);
    }
}
