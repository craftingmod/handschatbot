package com.craftingmod.maplebot.model;

/**
 * Created by superuser on 16/2/4.
 */
public class CoreUserModel {
    public int accountID;
    public int characterID;
    public CoreUserModel(int account, int character){
        accountID = account;
        characterID = character;
    }
}
