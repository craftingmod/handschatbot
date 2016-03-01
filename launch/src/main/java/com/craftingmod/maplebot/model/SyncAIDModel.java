package com.craftingmod.maplebot.model;

/**
 * Created by superuser on 16/2/1.
 */
public class SyncAIDModel {
    public int bridge = 1;
    public int aID;
    public SyncAIDModel(int aid){
        aID = aid;
    }
    public SyncAIDModel(int aid, int Bridge){
        aID = aid;
        bridge = Bridge;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof SyncAIDModel){
            SyncAIDModel model = (SyncAIDModel) o;
            if(aID == model.aID){
                return true;
            }
        }
        return false;
    }
}
