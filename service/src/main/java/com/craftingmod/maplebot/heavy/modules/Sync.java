package com.craftingmod.maplebot.heavy.modules;

import android.content.ComponentName;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.craftingmod.maplebot.Config;
import com.craftingmod.maplebot.heavy.IServiceHeavy;
import com.craftingmod.maplebot.model.CModel;
import com.craftingmod.maplebot.model.UserModel;
import com.google.common.collect.Lists;

import java.util.ArrayList;

/**
 * Created by superuser on 16/2/17.
 */
public class Sync extends HeavyModule {
    public Sync(IServiceHeavy mInterface) {
        super(mInterface);
    }

    @Override
    protected void handleCommand(CModel chat, ArrayList<String> splits) {
        if(chat.accountID == Config.MASTER_ACCOUNT_ID){
            Intent i = new Intent();
            i.setComponent(new ComponentName("com.craftingmod.maplebot.launcher","com.craftingmod.maplebot.launcher.SyncService"));
            context.startService(i);
        }
    }

    @Override
    protected void handleCommand(CModel chat, ArrayList<String> splits, UserModel say, @Nullable UserModel target) {

    }

    @Override
    protected ArrayList<String> filter() {
        return Lists.newArrayList(new String[]{"sync"});
    }

    @Override
    protected String[] help() {
        return new String[]{"<Private command>"};
    }

    @Override
    protected String name() {
        return "syncUtil";
    }
}
