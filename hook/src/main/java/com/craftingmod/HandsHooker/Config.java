package com.craftingmod.HandsHooker;

import com.craftingmod.HandsHooker.model.ChatModel;
import com.craftingmod.HandsHooker.model.SimpleUserModel;

/**
 * Created by superuser on 16/2/2.
 */
public class Config {
    public static final int MASTER_TELEGRAM_ID = 24987991; // Telegram Chat ID
    public static final String TELEGRAM_BOT_TOKEN = "168972296:AAFd11aFp30yFDPDxWnXGbP8x8mknSQL4UM"; // Telegram bot token
    public static final String ACCESS_BRAODCAST_TOKEN = "67FC62497E85D81BC54EA07AA87DCA6868CD470E95D7281155A8EB532CE60009"; // Access broadcast token
    public static final String INVOKE_NATIVE = "com.craftingmod.invokeNativeMsg";

    public static final int CHARACTER_BOT_ID = 88778583;
    public static final int MASTER_ACCOUNT_ID = 5733475;
    public static final int WORLD_BOT_ID = 3;
    public static final int GUILD_ID = 844558;
    public static final SimpleUserModel MASTER_CHARACTER = new SimpleUserModel(5733475,83396524,3);


    //MASTER ID
    public static final ChatModel CHAT_HANDS = new ChatModel(0,0,MASTER_ACCOUNT_ID,MASTER_ACCOUNT_ID,83396524,WORLD_BOT_ID,"");
    public static final int TELEGRAM_EMU_USERID = -5;
}
