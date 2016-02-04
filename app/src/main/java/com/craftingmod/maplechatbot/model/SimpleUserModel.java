package com.craftingmod.maplechatbot.model;

/**
 * Created by superuser on 16/1/25.
 */
public class SimpleUserModel extends CoreUserModel {

    public String userName;
    public int worldID;
    public String registerDate;

    public SimpleUserModel(int account, int character, String username){
        super(account, character);
        userName = username;
    }
    public SimpleUserModel(int account, int character, int worldID2){
        super(account, character);
        worldID = worldID2;
    }
}
