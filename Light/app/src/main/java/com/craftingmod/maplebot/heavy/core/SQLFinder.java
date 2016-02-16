package com.craftingmod.maplebot.heavy.core;

import android.util.Log;

import com.craftingmod.maplebot.model.UserModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by superuser on 15/12/13.
 */
public class SQLFinder {
    protected final String sql = "SELECT * FROM character_table_info WHERE $search";
    protected final String cmd = "sqlite3 $p \"" + sql + "\"";

    public static UserModel parse(String oneline){
        String[] piece = oneline.split("\\|");

        UserModel uModel;
        uModel = new UserModel(Integer.parseInt(piece[0]),Integer.parseInt(piece[1]),piece[12]);
        uModel.Exp = Long.parseLong(piece[11]);
        uModel.gender = Byte.parseByte(piece[3]);
        uModel.level = Short.parseShort(piece[4]);
        uModel.jobRank = Integer.parseInt(piece[9]);
        uModel.pop = Integer.parseInt(piece[6]);
        uModel.popRank = Integer.parseInt(piece[10]);
        uModel.totalRank = Integer.parseInt(piece[7]);
        uModel.worldID = Integer.parseInt(piece[2]);
        uModel.worldName = piece[14];
        uModel.worldRank = Integer.parseInt(piece[8]);
        if(piece.length >= 20){
            uModel.userImage = piece[19];
        }
        if(piece[16].length() < 1){
            if(piece[15].length() < 1){
                uModel.job = "없음";
            }else{
                uModel.job = piece[15];
            }
        }else{
            uModel.job = piece[16];
        }
        return uModel;
    }
    public static HashMap<String,String> getSearch(int account,int user){
        HashMap<String,String> hmap = new HashMap<>();
        hmap.put("aid",account + "");
        hmap.put("cid",user + "");
        return hmap;
    }
    public static HashMap<String,String> getSearch(String userName){
        HashMap<String,String> hmap = new HashMap<>();
        hmap.put("name",userName);
        return hmap;
    }
}
