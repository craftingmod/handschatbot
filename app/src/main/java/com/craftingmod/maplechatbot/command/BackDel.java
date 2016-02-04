package com.craftingmod.maplechatbot.command;

import android.os.HandlerThread;
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
import android.os.Handler;

/**
 * Created by superuser on 16/2/4.
 */
public class BackDel extends BaseCommand {
    private HandlerThread threadS;
    private Handler handler;
    public BackDel(ISender sender) {
        super(sender);
        threadS = new HandlerThread("BackDelSearchService");
        threadS.start();
        handler = new Handler(threadS.getLooper());
    }

    @Override
    protected String[] filter() {
        return new String[]{"뒷삭체크","checkD"};
    }

    @Override
    protected void onCommand(ChatModel chat, final UserModel user, String cmdName, @Nullable final ArrayList<String> args) {
        if(args.size() != 1){
            return;
        }
        Promise.with(this,String.class).then(new Task<String, UserModel>() {
            @Override
            public void run(String s, NextTask<UserModel> nextTask) {
                ArrayList<UserModel> models = db.searchUser(SQLFinder.getSearch(s));
                if(models.size() < 1){
                    nextTask.fail(null,null);
                }else{
                    nextTask.run(models.get(0));
                }
            }
        }).then(new Task<UserModel, ArrayList<FriendModel>>() {
            @Override
            public void run(UserModel userModel, NextTask<ArrayList<FriendModel>> nextTask) {
                MapleUtil.getInstance().getAccountFriendList(userModel.accountID,nextTask);
            }
        }).then(new Task<ArrayList<FriendModel>, Object>() {
            @Override
            public void run(final ArrayList<FriendModel> fModel, NextTask<Object> nextTask) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        final ArrayList<String> sakList = new ArrayList<>();
                        final int aID = db.searchUser(SQLFinder.getSearch(args.get(0))).get(0).accountID;
                        for(int i=0;i<fModel.size();i+=1){
                            Boolean sak = true;
                            ArrayList<FriendModel> sideM = MapleUtil.getAccountFriendList(fModel.get(i).accountID);
                            for(int j=0;j<sideM.size();j+=1){
                                if(sideM.get(j).accountID == aID){
                                    sak = false;
                                    break;
                                }
                            }
                            if (sak) {
                                sakList.add(fModel.get(i).nickname);
                            }
                        }
                        sendMessage("뒷삭 리스트: " + Joiner.on(",").skipNulls().join(sakList),user.accountID);
                    }
                });
            }
        }).create().execute(args.get(0));
    }
}
