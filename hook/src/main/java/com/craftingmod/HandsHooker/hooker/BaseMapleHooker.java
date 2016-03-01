package com.craftingmod.HandsHooker.hooker;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * Created by superuser on 15/12/12.
 */
public abstract class BaseMapleHooker extends XC_MethodHook {
    protected final String MapleRoot = "com.Nexon.MonsterLifeChat";

    protected LoadPackageParam param;
    protected ClassLoader cl;
    protected final Class<?> chatModel;
    protected final Class<?> FragTalk;
    protected final Class<?> global;
    protected final Class<?> messageAdapter;
    protected final Class<?> chatManager;
    protected final Class<?> talkroom;
    protected final Class<?> talkActivity;
    protected final Class<?> splash;


    public BaseMapleHooker(LoadPackageParam pm){
        param = pm;
        cl = param.classLoader;
        chatModel = getMapleClass(".ChatMessageModel");
        FragTalk = getMapleClass(".maplechat.talk.FragmentMapleTalkMessages");
        global = getMapleClass(".G");
        messageAdapter = getMapleClass(".maplechat.talk.AdapterMapleTalkMessages");
        chatManager = getMapleClass(".ChatManager");
        talkroom = getMapleClass(".maplechat.talk.ActivityMHTalkRoom");
        talkActivity = getMapleClass(".MainTabsActivity");
        splash = getMapleClass(".MapleHandsSplash");
    }
    protected Class<?> getMapleClass(String clname){
        return XposedHelpers.findClass(MapleRoot + clname, cl);
    }
    public abstract void handlePackage();
}
