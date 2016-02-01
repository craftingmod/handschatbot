package com.craftingmod.maplechatbot.methods;

import com.craftingmod.maplechatbot.model.MethodModel;

/**
 * Created by superuser on 16/1/25.
 */
public class SelectCInfo implements MethodModel {

    public static final String classname = "selectCharacterInfo";
    protected String JSON;

    public SelectCInfo(String jData){
        JSON = jData;
    }

    @Override
    public String getMethodTag() {
        return "selectCharacterInfo";
    }

    @Override
    public String getJSONData() {
        return JSON;
    }
}
