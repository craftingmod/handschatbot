package com.craftingmod.HandsHooker.model;

/**
 * Created by superuser on 16/2/4.
 */
public class OnlineModel extends CoreUserModel {
    public boolean online;
    public OnlineModel(int account, int character,Boolean on) {
        super(account, character);
        online = on;
    }
    public OnlineModel(UserModel model,Boolean on) {
        super(model.accountID, model.characterID);
        online = on;
    }
}
