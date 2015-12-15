package com.craftingmod.maplechatbot.model;

/**
 * Created by superuser on 15/12/13.
 */
public class MailModel {
    public String sendNick;
    public int receiveAccountID;
    public String message;
    public MailModel(String p1,int p2,String p3){
        sendNick = p1;receiveAccountID = p2;message = p3;
    }
}
