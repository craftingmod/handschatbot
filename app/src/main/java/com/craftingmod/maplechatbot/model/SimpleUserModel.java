package com.craftingmod.maplechatbot.model;

/**
 * Created by superuser on 16/1/25.
 */
public class SimpleUserModel {

    public int accountID;
    public int characterID;
    public String userName;
    public int worldID;
    public String registerDate;

    public SimpleUserModel(int account,int character,String username){
        accountID = account;
        characterID = character;
        userName = username;
    }
}
