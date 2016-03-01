package com.craftingmod.maplebot;

import android.content.Intent;

import com.craftingmod.maplebot.model.ChatModel;
import com.craftingmod.maplebot.model.CoreUserModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by superuser on 16/2/4.
 */
public class BroadUtil {

    public static final String SHUTDOWN = "broadcast.maple.shutdown";
    public static final String RECEIVE_MESSAGE = "broadcast.maple.receive";
    public static final String SEND_MESSAGE = "broadcast.maple.send";
    public static final String SEND_TELEGRAM = "broadcast.telegram.send";
    public static final String REQUEST_GUILD = "broadcast.maple.reqGuild";
    public static final String RECEIVE_GUILD = "broadcast.maple.recGuild";
    public static final String REQUEST_ONLINE = "broadcast.maple.reqOnline";
    public static final String RECEIVE_ONLINE = "broadcast.maple.recOnline";



    public static final int TYPE_FRIEND = 7123;
    public static final int TYPE_GUILD = 7133;
    public static Intent buildF_Message(Gson g,ChatModel model){
        Intent i = new Intent(RECEIVE_MESSAGE);
        i.putExtra("data", g.toJson(model));
        i.putExtra("type", TYPE_FRIEND);
        i.putExtra("token", Config.ACCESS_BROADCAST_TOKEN);
        return i;
    }
    public static Intent buildG_Message(Gson g,ChatModel model){
        Intent i = new Intent(RECEIVE_MESSAGE);
        i.putExtra("data", g.toJson(model));
        i.putExtra("type", TYPE_GUILD);
        i.putExtra("token", Config.ACCESS_BROADCAST_TOKEN);
        return i;
    }
    public static Intent buildF_sendMessage(Gson g,ChatModel model){
        Intent i = new Intent(SEND_MESSAGE);
        i.putExtra("data", g.toJson(model));
        i.putExtra("type", TYPE_FRIEND);
        i.putExtra("token", Config.ACCESS_BROADCAST_TOKEN);
        return i;
    }
    public static Intent buildT_sendMessage(String text){
        Intent i = new Intent(SEND_TELEGRAM);
        i.putExtra("data", text);
        i.putExtra("token", Config.ACCESS_BROADCAST_TOKEN);
        return i;
    }
    public static Intent buildN_reqOnline(Gson g,ArrayList<CoreUserModel> models){
        Type type = new TypeToken<ArrayList<CoreUserModel>>(){}.getType();
        Intent i = new Intent(REQUEST_ONLINE);
        i.putExtra("data", g.toJson(models,type));
        i.putExtra("token", Config.ACCESS_BROADCAST_TOKEN);
        return i;
    }
}
