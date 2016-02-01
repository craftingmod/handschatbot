package com.craftingmod.maplechatbot.methods;

import com.craftingmod.maplechatbot.model.MethodModel;

/**
 * Created by superuser on 16/1/25.
 */
public class GetFriendList implements MethodModel {
    public static final String classname = "GetAccountFriendList";
    protected int accountID;
    protected String JSON;

    public GetFriendList(String jData){
        JSON = jData;
    }

    @Override
    public String getMethodTag() {
        return classname;
    }

    @Override
    public String getJSONData() {
        return JSON;
    }
}
