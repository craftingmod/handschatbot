package com.craftingmod.maplechatbot;

import com.craftingmod.maplechatbot.model.ChatModel;

/**
 * Created by superuser on 15/12/15.
 */
public class Config {
    public static final int MASTER_TELEGRAM_ID = 24987991;
    public static final String TELEGRAM_BOT_TOKEN = "168972296:AAFd11aFp30yFDPDxWnXGbP8x8mknSQL4UM";
    public static final String ACCESS_BRAODCAST_TOKEN = "67FC62497E85D81BC54EA07AA87DCA6868CD470E95D7281155A8EB532CE60009";
    public static final String BROADCAST_MESSAGE = "com.craftingmod.broadMessage";
    public static final String SEND_MESSAGE = "com.craftingmod.sendMessage";
    public static final String TELEGRAM_MESSAGE = "com.craftingmod.telegramMessage";
    public static final String INVOKE_NATIVE = "com.craftingmod.invokeNativeMsg";
    public static final String GET_NATIVE = "com.craftingmod.getNativeMsg";
    public static final String DATABASE_PATH = "/data/mapleChat/maple.db";
    public static final int CHARACTER_BOT_ID = 88778583;
    public static final int MASTER_ACCOUNT_ID = 5733475;
    public static final int WORLD_BOT_ID = 3;

    //MASTER ID
    public static final ChatModel CHAT_HANDS = new ChatModel(0,0,MASTER_ACCOUNT_ID,MASTER_ACCOUNT_ID,83396524,WORLD_BOT_ID,"");
    public static final int TELEGRAM_EMU_USERID = -5;
    public static final String TELEGRAM_EMU_USERNAME = "채팅Bot";
}
