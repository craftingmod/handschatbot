package com.craftingmod.maplechatbot.command;

import android.app.Service;
import android.content.Context;
import android.support.annotation.Nullable;

import com.craftingmod.maplechatbot.chat.ChatService;
import com.craftingmod.maplechatbot.model.UserModel;

import java.util.ArrayList;

/**
 * Created by superuser on 15/12/13.
 */
public class BotState extends BaseCommand {

    private ChatService sv;
    public BotState(Context ct,ChatService svc) {
        super(ct);
        sv = svc;
    }

    @Override
    public void onText(UserModel user, String msg) {

    }

    @Override
    protected void onCommand(UserModel user, String cmdName, @Nullable ArrayList<String> args) {
        if(user.accountID == 5733475){
            if(cmdName.equals("shutdown")){
                sendMessage("Shutting down..");
                sv.stopForeground(true);
                sv.stopSelf();
                sv.onDestroy();
            }
            if(cmdName.equals("toggleOut")){
                sendMessage("Toggled Output");
                sv.toggleSlient();
            }
        }
    }

    @Override
    protected void onExit() {

    }
}
