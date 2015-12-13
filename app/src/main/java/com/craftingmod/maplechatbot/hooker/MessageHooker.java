package com.craftingmod.maplechatbot.hooker;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.craftingmod.maplechatbot.MyApplication;
import com.craftingmod.maplechatbot.model.ChatModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by superuser on 15/12/12.
 */
public class MessageHooker extends BaseMapleHooker {

    private Gson g;

    public MessageHooker(XC_LoadPackage.LoadPackageParam pm) {
        super(pm);
        g = new GsonBuilder().create();
    }

    @Override
    public void handlePackage() {
        XposedBridge.hookAllMethods(chatManager, "OnFriendChatMessage", this);
    }
    // int paramInt1, int paramInt2, int paramInt3, int paramInt4, long paramLong, byte[] paramArrayOfByte, byte paramByte

}
