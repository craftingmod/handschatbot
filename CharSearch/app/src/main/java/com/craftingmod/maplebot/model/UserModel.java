package com.craftingmod.maplebot.model;

import com.google.common.base.Splitter;

import java.util.List;

/**
 * Created by superuser on 15/12/12.
 */
public class UserModel extends SimpleUserModel {

    public long lastTimestamp;


    public String userImage;
    public String job;

    public String jobName;
    public String jobDetail;

    public short level;
    public byte gender;
    public int pop;

    public String worldName;
    //pop: 인기도
    public int totalRank;
    public int worldRank;
    public int jobRank;
    public int popRank;
    public long Exp;
    public UserModel(int account, int character, String username){
        super(account, character, username);
        accountID = account;
        characterID = character;
        userName = username;
    }
    public void parse(String s){
        String json = s.split("dtCharacterInfo=anyType")[1];
        json = json.replaceAll("anyType\\{\\}", "null");
        json = json.substring(json.indexOf("{")+1,json.indexOf("};"));
        List<String> split = Splitter.on(";").trimResults().omitEmptyStrings().splitToList(json);
        for(int i=0;i<split.size();i+=1){
            String exp = split.get(i);
            String key = exp.split("=")[0];
            String value = exp.split("=")[1];
            if(key.equalsIgnoreCase("AccountID")){
                this.accountID = Integer.parseInt(value);
            }
            if(key.equalsIgnoreCase("Gender")){
                this.gender = Byte.parseByte(value, 10);
            }
            if(key.equalsIgnoreCase("PopRank")){
                this.popRank = Integer.parseInt(value);
            }
            if(key.equalsIgnoreCase("JobRank")){
                this.jobRank = Integer.parseInt(value);
            }
            if(key.equalsIgnoreCase("WorldRank")){
                this.worldRank = Integer.parseInt(value);
            }
            if(key.equalsIgnoreCase("EXP")){
                this.Exp = Long.parseLong(value);
            }
            if(key.equalsIgnoreCase("JobDetailName")){
                if(!value.equals("null")){
                    this.jobDetail = value;
                    this.job = value;
                }
            }
            if(key.equalsIgnoreCase("JobName")){
                if(!value.equals("null")){
                    this.jobName = value;
                    this.job = value;
                }
            }
            if(key.equalsIgnoreCase("TotRank")){
                this.totalRank = Integer.parseInt(value);
            }
            if(key.equalsIgnoreCase("Pop")){
                this.pop = Integer.parseInt(value);
            }
            if(key.equalsIgnoreCase("Lev")){
                this.level = Short.parseShort(value);
            }
            if(key.equalsIgnoreCase("CharacterName")){
                if(!value.equals("null")){
                    this.userName = value;
                }
            }
            if(key.equalsIgnoreCase("WorldName")){
                if(!value.equals("null")){
                    this.worldName = value;
                }
            }
            if(key.equalsIgnoreCase("AvatarImgUrl")){
                if(!value.equals("null")){
                    this.userImage = value;
                }
            }
        }
        if(this.job == null){
            this.job = "";
        }
    }
}
