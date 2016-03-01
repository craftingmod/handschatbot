package com.craftingmod.maplebot;

import android.util.Log;

import com.craftingmod.maplebot.model.ChatModel;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Created by superuser on 16/2/13.
 */
public class Config {
    public static final String ACCESS_BROADCAST_TOKEN = "67FC62497E85D81BC54EA07AA87DCA6868CD470E95D7281155A8EB532CE60009";
    public static final String[] WORLDS = new String[]{"없음","스카니아","베라","루나","제니스","크로아","유니온","엘리시움","이노시스","레드","오로라"};
    public static final Gson g = new GsonBuilder().create();
    public static final String CONFIG_FILE = "/data/maple/config.json";
    public static final int ACCOUNT_ID = 5733475;
    public static final int WORLD_ID = 3;

    public static final int ID_SERVICE = 10074;


    public static int getWorldID(String world){
        for(int i=0;i<WORLDS.length;i+=1){
            String w = WORLDS[i];
            if(w.equals(world)){
                return i;
            }
        }
        return -1;
    }
    public static final Save read(){
        try {
            final File file = new File(CONFIG_FILE);
            if(file.exists() && file.canRead()){
                return g.fromJson(Files.toString(file, Charsets.UTF_8),Save.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Save();
    }
    public static final void write(Save save){
        String dir = CONFIG_FILE.substring(0,CONFIG_FILE.lastIndexOf("/"));
        try{
            Runtime.getRuntime().exec("su -c mkdir " + CONFIG_FILE.substring(0,CONFIG_FILE.lastIndexOf("/")));
            Runtime.getRuntime().exec("su -c chmod 777 " + dir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            File file = new File(CONFIG_FILE);
            Log.d("Hello",g.toJson(save,Save.class));
            Files.write(g.toJson(save,Save.class),file,Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try{
            Runtime.getRuntime().exec("su -c chown 0 " + CONFIG_FILE);
            Runtime.getRuntime().exec("su -c chmod 777 " + CONFIG_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
