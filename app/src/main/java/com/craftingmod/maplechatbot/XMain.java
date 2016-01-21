package com.craftingmod.maplechatbot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.craftingmod.maplechatbot.chat.ChatService;
import com.craftingmod.maplechatbot.hooker.LogHooker;
import com.craftingmod.maplechatbot.hooker.MessageHooker;
import com.craftingmod.maplechatbot.hooker.SplashHooker;
import com.craftingmod.maplechatbot.model.ChatModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Iterator;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by superuser on 15/12/12.
 */
public class XMain implements IXposedHookLoadPackage {

    private Class<?> chatModel;
    protected Class<?> FragTalk;
    protected Class<?> global;
    protected Class<?> messageAdapter;
    protected Class<?> chatManager;
    protected Class<?> talkroom;

    private Gson g;
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        final String pname = loadPackageParam.packageName;
        final ClassLoader cl = loadPackageParam.classLoader;
        final String mMaple = "com.Nexon.MonsterLifeChat";

        g = new GsonBuilder().create();

        if(pname.equalsIgnoreCase(mMaple)){
            chatModel = XposedHelpers.findClass(mMaple + ".ChatMessageModel",cl);
            FragTalk = XposedHelpers.findClass(mMaple + ".maplechat.talk.FragmentMapleTalkMessages",cl);
            global = XposedHelpers.findClass(mMaple + ".G",cl);
            messageAdapter = XposedHelpers.findClass(mMaple + ".maplechat.talk.AdapterMapleTalkMessages",cl);
            chatManager = XposedHelpers.findClass(mMaple + ".ChatManager", cl);
            talkroom = XposedHelpers.findClass(mMaple + ".maplechat.talk.ActivityMHTalkRoom",cl);

           /*
            XposedHelpers.findAndHookMethod(talkroom, "sendMessage", String.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Context context = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");

                    Intent intent = new Intent("com.craftingmod.GET_FRD");
                    intent.putExtra("data", (int[]) XposedHelpers.getObjectField(param.thisObject, "friendsAids"));
                    context.sendBroadcast(intent);
                }
            });
            */

            new LogHooker(loadPackageParam).handlePackage();
            new MessageHooker(loadPackageParam).handlePackage();
            new SplashHooker(loadPackageParam).handlePackage();
        }
    }
}
