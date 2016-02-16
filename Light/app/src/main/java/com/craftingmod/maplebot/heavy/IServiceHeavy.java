package com.craftingmod.maplebot.heavy;

import android.content.Context;

import com.craftingmod.maplebot.heavy.core.UserDB;
import com.craftingmod.maplebot.heavy.modules.HeavyModule;
import com.craftingmod.maplebot.model.CModel;
import com.craftingmod.maplebot.model.ChatModel;
import com.craftingmod.maplebot.model.UserModel;
import android.os.Handler;

import java.util.ArrayList;

/**
 * Created by superuser on 16/2/15.
 */
public interface IServiceHeavy {
    Context getContext();
    void sendMessage(CModel chat, String text);
    void request(HeavyModule module,CModel chat, String username);
    UserModel getUser(int worldID, int characterID);
    UserDB getDB();
    void putUser(UserModel model);
    void writeUser(UserModel model);
}
