package com.craftingmod.maplechatbot.command;

import android.app.Service;
import android.content.Context;
import android.support.annotation.Nullable;

import com.craftingmod.maplechatbot.Config;
import com.craftingmod.maplechatbot.chat.ChatService;
import com.craftingmod.maplechatbot.chat.ISender;
import com.craftingmod.maplechatbot.model.ChatModel;
import com.craftingmod.maplechatbot.model.UserModel;

import java.util.ArrayList;

/**
 * Created by superuser on 15/12/13.
 */
public class BotState extends BaseCommand {

    private ChatService sv;
    private long startTime;
    public BotState(ISender sd,ChatService svc) {
        super(sd);
        sv = svc;
        startTime = System.currentTimeMillis();
    }
    @Override
    protected String[] filter(){
        return new String[]{"uptime","version","shutdown","me","save"};
    }

    @Override
    protected void onCommand(ChatModel chat,UserModel user, String cmdName, @Nullable ArrayList<String> args) {
        if(user.accountID == Config.MASTER_ACCOUNT_ID){
            if(cmdName.equals("shutdown")){
                sendMessage("Shutting down..",user.accountID);
                sv.stopForeground(true);
                sv.stopSelf();
                sv.onDestroy();
            }
            if(cmdName.equals("save")){
                sendMessage("saving...",user.accountID);
                sv.save();
            }
        }
        if(cmdName.equals("version")){
            sendMessage("1.1 (20160113)", user.accountID);
        }
        if(cmdName.equals("uptime")){
            sendMessage(this.getDeltaTime(startTime),user.accountID);
        }
        if(cmdName.equals("me")){
            sendMessage(user.userName + ": AID " + chat.SenderAID + " CID: " + user.characterID,user.accountID);
        }
    }
}
