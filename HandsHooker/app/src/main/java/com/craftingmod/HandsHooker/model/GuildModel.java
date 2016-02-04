package com.craftingmod.HandsHooker.model;

/**
 * Created by superuser on 16/2/4.
 */
public class GuildModel {
    public int GuildID;
    public int GuildGrade;
    public String GuildName;

    public int accountID;
    public int characterID;
    public int worldID;

    public GuildModel(int guildID,int guildGrade,String guildName){
        GuildID = guildID;
        GuildGrade = guildGrade;
        GuildName = guildName;
    }
    public void setUser(int acid,int chid,int wid){
        accountID = acid;
        characterID = chid;
        worldID = wid;
    }
    public void setUser(UserModel model){
        setUser(model.accountID,model.characterID,model.worldID);
    }
}
