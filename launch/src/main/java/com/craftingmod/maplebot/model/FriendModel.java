package com.craftingmod.maplebot.model;

import com.craftingmod.maplebot.Config;
import com.google.common.base.Splitter;

import java.util.HashMap;
import java.util.List;

/**
 * Created by superuser on 16/1/25.
 */
public class FriendModel {
    public int accountID;
    public int worldID;
    public String worldName;
    public String charName;
    public String nickname;
    private HashMap<String,Integer> worldIDs;
    public String image;
    public FriendModel(int friendAID, String characterName, String customName){
        this();
        accountID = friendAID;
        charName = characterName;
        nickname = customName;
        worldID = -1;
    }
    public FriendModel(){
        worldIDs = new HashMap<>();
        for(int i=1;i<Config.WORLDS.length;i+=1){
            worldIDs.put(Config.WORLDS[i],i);
        }
    }
    public FriendModel(String parseableString){
        this();
        parse(parseableString);
    }
    public void parse(String json){
        json = json.replaceAll("anyType\\{\\}","null");
        json = json.substring(json.indexOf("{") + 1, json.indexOf("};"));
        List<String> split = Splitter.on(";").trimResults().omitEmptyStrings().splitToList(json);
        for(int i=0;i<split.size();i+=1){
            String exp = split.get(i);
            String key = exp.split("=")[0];
            String value = exp.split("=")[1];
            if(value.equalsIgnoreCase("null")){
                value = "없음";
            }
            if(key.equalsIgnoreCase("FriendAID")){
                this.accountID = Integer.parseInt(value);
                continue;
            }
            if(key.equalsIgnoreCase("WorldName")){
                this.worldName = value;
                if(worldIDs.containsKey(value)){
                    this.worldID = worldIDs.get(value);
                }
                continue;
            }
            if(key.equalsIgnoreCase("CharacterName")){
                this.charName = value;
                continue;
            }
            if(key.equalsIgnoreCase("AvatarImgUrl")){
                this.image = value;
                continue;
            }
            if(key.equalsIgnoreCase("NickName")){
                this.nickname = value;
            }
        }
    }
}
