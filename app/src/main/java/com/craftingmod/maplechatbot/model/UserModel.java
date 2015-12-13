package com.craftingmod.maplechatbot.model;

import java.net.URL;

/**
 * Created by superuser on 15/12/12.
 */
public class UserModel {

    public long lastTimestamp;

    public int accountID;
    public int characterID;
    public String userName;

    public String userImage;
    public String job;
    public short level;
    public byte gender;
    public int pop;

    public int worldID;
    public String worldName;
    //pop: 인기도
    public int totalRank;
    public int worldRank;
    public int jobRank;
    public int popRank;
    public long Exp;
    public UserModel(int account,int character,String username){
        accountID = account;
        characterID = character;
        userName = username;
    }
}
