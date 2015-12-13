package com.craftingmod.maplechatbot.hooker;

import android.content.Context;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * Created by superuser on 15/12/12.
 */
public abstract class BaseMapleHooker extends XC_MethodHook {
    protected final String MapleRoot = "com.Nexon.MonsterLifeChat";

    protected LoadPackageParam param;
    protected ClassLoader cl;
    protected Class<?> chatModel;
    protected Class<?> FragTalk;
    protected Class<?> global;
    protected Class<?> messageAdapter;
    protected Class<?> chatManager;


    public BaseMapleHooker(LoadPackageParam pm){
        param = pm;
        cl = param.classLoader;
        initClasses();
    }
    protected void initClasses(){
        chatModel = getMapleClass(".ChatMessageModel");
        FragTalk = getMapleClass(".maplechat.talk.FragmentMapleTalkMessages");
        global = getMapleClass(".G");
        messageAdapter = getMapleClass(".maplechat.talk.AdapterMapleTalkMessages");
        chatManager = getMapleClass(".ChatManager");

    }
    protected Class<?> getMapleClass(String clname){
        return XposedHelpers.findClass(MapleRoot + clname, cl);
    }
    public abstract void handlePackage();
}
