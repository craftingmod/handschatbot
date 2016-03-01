package com.craftingmod.maplebot;

import com.craftingmod.maplebot.model.SimpleUserModel;
import com.craftingmod.maplebot.model.UserModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by superuser on 16/2/17.
 */
public class Save {
    private final String[] WORLDS = new String[]{"없음","스카니아","베라","루나","제니스","크로아","유니온","엘리시움","이노시스","레드","오로라"};

    public int masterAID;
    public int guildID;
    public SimpleUserModel masterChar;
    protected SimpleUserModel botChar_Scania = null;
    protected SimpleUserModel botChar_Bera = null;
    protected SimpleUserModel botChar_Luna = null;
    protected SimpleUserModel botChar_Zenith = null;
    protected SimpleUserModel botChar_Croa = null;
    protected SimpleUserModel botChar_Union = null;
    protected SimpleUserModel botChar_Elysium = null;
    protected SimpleUserModel botChar_Enosis = null;
    protected SimpleUserModel botChar_Red = null;
    protected SimpleUserModel botChar_Aurora = null;

    public Save(){
        masterAID = -1;
        guildID = -1;
        masterChar = null;
    }
    public SimpleUserModel getChar(String world){
        for(int i=0;i<WORLDS.length;i+=1){
            String w = WORLDS[i];
            if(w.equals(world)){
                return getChar(i);
            }
        }
        return null;
    }
    public SimpleUserModel getChar(int wID){
        SimpleUserModel user = null;
        switch (wID){
            case 1:
                user = botChar_Scania;
                break;
            case 2:
                user = botChar_Bera;
                break;
            case 3:
                user = botChar_Luna;
                break;
            case 4:
                user = botChar_Zenith;
                break;
            case 5:
                user = botChar_Croa;
                break;
            case 6:
                user = botChar_Union;
                break;
            case 7:
                user = botChar_Elysium;
                break;
            case 8:
                user = botChar_Enosis;
                break;
            case 9:
                user = botChar_Red;
                break;
            case 10:
                user = botChar_Aurora;
                break;
        }
        return user;
    }
    public void setChar(String world,SimpleUserModel user){
        for(int i=0;i<WORLDS.length;i+=1){
            String w = WORLDS[i];
            if(w.equals(world)){
                setChar(i,user);
                break;
            }
        }
    }
    public void setChar(int wID,SimpleUserModel user){
        switch (wID){
            case 1:
                botChar_Scania = user;
                break;
            case 2:
                botChar_Bera = user;
                break;
            case 3:
                botChar_Luna = user;
                break;
            case 4:
                botChar_Zenith = user;
                break;
            case 5:
                botChar_Croa = user;
                break;
            case 6:
                botChar_Union = user;
                break;
            case 7:
                botChar_Elysium = user;
                break;
            case 8:
                botChar_Enosis = user;
                break;
            case 9:
                botChar_Red = user;
                break;
            case 10:
                botChar_Aurora = user;
                break;
        }
    }
}
