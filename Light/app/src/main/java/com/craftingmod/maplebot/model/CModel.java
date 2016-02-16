package com.craftingmod.maplebot.model;

/**
 * Created by superuser on 16/2/15.
 */
public class CModel extends CoreUserModel {
    public String text;
    public boolean friend = true;
    public int worldID;
    public CModel(ChatModel model,boolean f){
        super(model.SenderAID,model.SenderCID);
        text = model.Msg;
        friend = f;
        worldID = model.WID;
    }
}
