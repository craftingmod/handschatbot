package com.craftingmod.maplebot.communicate;

import java.util.HashMap;

/**
 * Created by superuser on 16/2/14.
 */
public class ResultObject {

    private final HashMap<Integer,String> toStr;
    public int type;
    public int status;
    public String data;
    public ResultObject(int ID){
        status = ID;
        toStr = new HashMap<>();
        toStr.put(CharSearch.TIMEOUT,"TIMEOUT");
        toStr.put(CharSearch.IDLE,"IDLE");
        toStr.put(CharSearch.BUSY,"BUSY");
        toStr.put(CharSearch.NULL,"NULL");
        toStr.put(CharSearch.OK,"OK");
    }
    public String getState(){
        if(toStr.containsKey(status)){
            return toStr.get(status);
        }else{
            return status + "";
        }
    }
}
