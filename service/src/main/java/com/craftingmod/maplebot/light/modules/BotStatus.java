package com.craftingmod.maplebot.light.modules;

import com.craftingmod.maplebot.light.IService;
import com.craftingmod.maplebot.model.ChatModel;

import java.util.ArrayList;

/**
 * Created by superuser on 16/2/13.
 */
public class BotStatus extends CoreModule {

    private long startTime;

    public BotStatus(IService sender) {
        super(sender);
        startTime = System.currentTimeMillis();
    }

    @Override
    protected String onCommand(ChatModel chat, String cmd, ArrayList<String> args) {
        String out = "";
        if(cmd.equals("version")){
            out = "2.0Dev (20160213)";
        }
        if(cmd.equals("uptime")){
            out = this.getDeltaTime(startTime);
        }
        return out;
    }

    @Override
    public String getName() {
        return "BotStatus";
    }

    @Override
    public String[] help() {
        return new String[]{"!version : 봇의 버전을 출력합니다.","!uptime : 봇이 작동한 시간을 출력합니다."};
    }

    @Override
    protected String[] filter() {
        return new String[]{"version","uptime"};
    }
}
