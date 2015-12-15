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

    private receiver rc;

    protected BroadcastReceiver br;

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
            XposedHelpers.findAndHookConstructor(chatManager, Context.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                    IntentFilter inf = new IntentFilter();
                    inf.addAction("com.craftingmod.SEND_MSG");
                    inf.addAction("com.craftingmod.GET_FRD");
                    inf.addAction("com.craftingmod.GET_API");
                    ((Context) param.args[0]).registerReceiver(new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            if (intent.getAction().equalsIgnoreCase("com.craftingmod.GET_FRD")) {
                                XposedHelpers.setAdditionalInstanceField(param.thisObject, "friendAList", intent.getIntArrayExtra("data"));
                            } else if (intent.getAction().equalsIgnoreCase("com.craftingmod.GET_API")) {
                                XposedHelpers.setAdditionalInstanceField(param.thisObject, "firstTime", intent.getLongExtra("date", System.nanoTime()));
                                XposedHelpers.setAdditionalInstanceField(param.thisObject, "baseData", g.fromJson(intent.getStringExtra("modelData"), ChatModel.class));
                            } else {
                                Log.i("WBUS", (XposedHelpers.getAdditionalInstanceField(param.thisObject, "firstTime") == null) ? "true" : "false");
                                if (XposedHelpers.getAdditionalInstanceField(param.thisObject, "firstTime") != null) {
                                    ChatModel model = (ChatModel) XposedHelpers.getAdditionalInstanceField(param.thisObject, "baseData");
                                    long time = model.RegisterDate + (((System.nanoTime() - ((long) XposedHelpers.getAdditionalInstanceField(param.thisObject, "firstTime"))) / 1000000) * 10000);
                                    Object nativeModel = XposedHelpers.newInstance(chatModel
                                            , model.Roomkey, 0L, model.AID, model.SenderAID, model.SenderCID, model.WID, intent.getStringExtra("msg"));
                                    Log.d("WBUS", g.toJson(nativeModel));
                                    //XposedHelpers.setObjectField(nativeModel, "FriendAids", XposedHelpers.getAdditionalInstanceField(param.thisObject, "friendAList"));
                                    XposedHelpers.callMethod(param.thisObject, "sendFriendMessage", nativeModel);
                                }
                            }
                            Log.i("WBUS", "Response OK.");
                        }
                    }, inf);
                    super.beforeHookedMethod(param);
                }
            });
            XposedHelpers.findAndHookMethod(chatManager, "sendFriendMessage", chatModel, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    Log.d("Maple", g.toJson(param.args[0]));
                    XposedHelpers.setObjectField(param.args[0], "FriendAids", new int[]{5733475, 17282110});
                }
            });

            XposedHelpers.findAndHookMethod(talkroom, "onCreate", Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Context context = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                    IntentFilter inf = new IntentFilter();
                    inf.addAction("com.craftingmod.SEND_MSG");
                    rc = new receiver(param);
                    context.registerReceiver(rc, inf);
                }
            });
            XposedHelpers.findAndHookMethod(talkroom, "onDestroy", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    Context context = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                    if(rc != null){
                        context.unregisterReceiver(rc);
                    }
                }
            });
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
            XposedBridge.hookAllMethods(chatManager, "OnFriendChatMessage", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Object[] args = param.args;
                    String msg = (String) XposedHelpers.callStaticMethod(global, "NativeToString", args[5]);
                    ChatModel model = new ChatModel((int)args[0],(long)args[4],(int)args[0],(int)args[2],(int)args[3],(int)args[1],msg);
                    Context context = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                    if(model.Msg.equalsIgnoreCase("updateUser")){
                        Intent snd = new Intent("com.craftingmod.GET_API");
                        snd.putExtra("modelData",g.toJson(model));
                        snd.putExtra("date",System.nanoTime());
                        context.sendBroadcast(snd);
                        /*
                        context.registerReceiver(new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {
                                String msg = intent.getStringExtra("data");
                                if (model_API != null) {
                                    long time = model_API.RegisterDate + (((System.nanoTime() - nano_API) / 1000000) * 10000);
                                    Object nativeModel = XposedHelpers.newInstance(chatModel, model_API.Roomkey, time, model_API.AID, model_API.SenderAID, model_API.SenderCID, model_API.WID, msg);
                                    XposedHelpers.callMethod(chatManager_API, "sendFriendMessage", nativeModel);
                                }
                            }
                        }, new IntentFilter());
                        */
                    }
                    Log.d("MapleBOT",g.toJson(model));
                    Log.d("MapleBOT-Local", System.nanoTime() + "");
                    XposedBridge.log(g.toJson(model));

                    Intent i = new Intent("com.craftingmod.MESSAGE_FRIEND");
                    i.putExtra("data", g.toJson(model));
                    context.sendBroadcast(i);
                }
            });
            new LogHooker(loadPackageParam).handlePackage();
        }
    }
    private class receiver extends BroadcastReceiver {
        private XC_MethodHook.MethodHookParam pm;
        public receiver(XC_MethodHook.MethodHookParam p){
            pm = p;
        }
        @Override
        public void onReceive(Context context, Intent intent) {
            XposedHelpers.callMethod(pm.thisObject, "sendMessage",intent.getStringExtra("msg"));
        }
    }
}
