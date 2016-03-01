package com.craftingmod.maplebot;

import com.craftingmod.maplebot.model.ChatModel;

/**
 * Created by superuser on 16/2/13.
 */
public class Config {
    public static final String ACCESS_BROADCAST_TOKEN = "67FC62497E85D81BC54EA07AA87DCA6868CD470E95D7281155A8EB532CE60009";
    public static final int CHARACTER_BOT_ID = 88778583;
    public static final int MASTER_ACCOUNT_ID = 5733475;
    public static final int WORLD_BOT_ID = 3;
    public static final ChatModel bot = new ChatModel(Config.MASTER_ACCOUNT_ID,0,Config.MASTER_ACCOUNT_ID,Config.MASTER_ACCOUNT_ID,Config.CHARACTER_BOT_ID,Config.WORLD_BOT_ID,"");
}
