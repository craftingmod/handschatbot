package com.craftingmod.maplechatbot.hooker;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.anprosit.android.promise.NextTask;
import com.anprosit.android.promise.Promise;
import com.anprosit.android.promise.Task;
import com.craftingmod.maplechatbot.Config;
import com.craftingmod.maplechatbot.MapleUtil;
import com.craftingmod.maplechatbot.MyApplication;
import com.craftingmod.maplechatbot.methods.GetCharacterInfo;
import com.craftingmod.maplechatbot.methods.GetFriendList;
import com.craftingmod.maplechatbot.methods.SelectCInfo;
import com.craftingmod.maplechatbot.model.ChatModel;
import com.craftingmod.maplechatbot.model.UserModel;
import com.google.common.primitives.Ints;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramBotAdapter;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by superuser on 15/12/12.
 */
public class MessageHooker extends BaseMapleHooker {

    protected Class<?> MapleDBManager;
    protected Class<?> MapleInfoModel;
    protected Class<?> SoapProtocol;

    private final Gson g = new GsonBuilder().create();
    private IntentFilter intentF;
    private IntentFilter intentNativeMSG;


    public MessageHooker(XC_LoadPackage.LoadPackageParam pm) {
        super(pm);
        intentF = new IntentFilter();
        intentF.addAction(Config.SEND_MESSAGE);
        intentF.addAction(Config.BROADCAST_MESSAGE);
        intentF.addAction(Config.TELEGRAM_MESSAGE);

        intentNativeMSG = new IntentFilter();
        intentNativeMSG.addAction(Config.INVOKE_NATIVE);

        MapleDBManager = getMapleClass(".database.MHDatabaseManager");
        MapleInfoModel = getMapleClass(".structdata.MHCharacterInfo");
        SoapProtocol = getMapleClass(".SoapEx");
    }

    @Override
    public void handlePackage() {

        /*
        Receive Friends message
         */
        XposedBridge.hookAllMethods(chatManager, "OnFriendChatMessage", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Object[] args = param.args;
                String msg = (String) XposedHelpers.callStaticMethod(global, "NativeToString", args[5]);
                ChatModel model = new ChatModel((int) args[0], (long) args[4], (int) args[0], (int) args[2], (int) args[3], (int) args[1], msg);
                Context context = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");

                XposedBridge.log("- Sent model \n" + g.toJson(model));

                Intent i = new Intent(Config.BROADCAST_MESSAGE);
                i.putExtra("data", g.toJson(model));
                i.putExtra("token",Config.ACCESS_BRAODCAST_TOKEN);
                context.sendBroadcast(i);
            }
        });

        /*
        Register custom braodcastReceiver
         */
        XposedHelpers.findAndHookMethod(talkActivity, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                final Object _this = param.thisObject;
                final Context context = ((Context) _this);
                final ContextHook mReceiver = new ContextHook(_this);
                final ContextMethod mNativeBroker = new ContextMethod(_this);
                XposedHelpers.setAdditionalInstanceField(_this,"msgReceiver",mReceiver);
                XposedHelpers.setAdditionalInstanceField(_this,"nativeCmdReceiver",mNativeBroker);
                XposedHelpers.setAdditionalInstanceField(_this, "bot", TelegramBotAdapter.build(Config.TELEGRAM_BOT_TOKEN));
                context.registerReceiver(mReceiver,intentF);
            }
        });
        XposedBridge.hookAllMethods(talkActivity, "onDestroy", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                final Object _this = param.thisObject;
                final Context context = ((Context) _this);
                final ContextHook mReceiver = (ContextHook) XposedHelpers.getAdditionalInstanceField(_this, "msgReceiver");
                final ContextMethod mNativeCMDReceiver = (ContextMethod) XposedHelpers.getAdditionalInstanceField(_this, "nativeCmdReceiver");
                context.unregisterReceiver(mReceiver);
                context.unregisterReceiver(mNativeCMDReceiver);
            }
        });
        /*
        XposedHelpers.findAndHookConstructor(chatManager, Context.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Context context = ((Context) param.args[0]);
                final ContextHook hooker = new ContextHook(param);
                XposedHelpers.setAdditionalInstanceField(param.thisObject, "bot", TelegramBotAdapter.build(Config.TELEGRAM_BOT_TOKEN));
                XposedHelpers.setAdditionalInstanceField(param.thisObject, "hookedReceiver", hooker);
                context.registerReceiver(hooker,intentF);
                if(param.args[0] instanceof Activity){
                    XposedHelpers.findAndHookMethod(param.args[0].getClass(),"onDestroy",)
                }
                //        .registerReceiver();
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                XposedHelpers.setAdditionalInstanceField(param.thisObject,"bot",
                        TelegramBotAdapter.build(Config.TELEGRAM_BOT_TOKEN));
            }
        });
        */
    }
    private class ContextMethod extends BroadcastReceiver {

        private Object mainActivity; // MainTabsActivity

        private Object mChatManager; // ChatManager
        private Handler handler;

        public ContextMethod(Object _mainAct){
            mainActivity = _mainAct;
        }

        @Override
        public void onReceive(final Context context, final Intent intent) {
            if(!intent.hasExtra("type")){
                return;
            }
            String type = intent.getStringExtra("type");
            handler = new Handler(context.getMainLooper());
            if(type.equalsIgnoreCase(GetFriendList.classname)){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        List list = (List) XposedHelpers.callStaticMethod(SoapProtocol, "GetAccountFriendList", intent.getIntExtra("aid", Config.MASTER_ACCOUNT_ID));
                        String toSend = g.toJson(list);
                        Intent intent = new Intent(Config.GET_NATIVE);
                        intent.putExtra("method", GetFriendList.classname);
                        intent.putExtra("data", toSend);
                        context.sendBroadcast(intent);
                    }
                });
            }
            if(type.equalsIgnoreCase(GetCharacterInfo.classname)){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Object info = XposedHelpers.callStaticMethod(SoapProtocol,"GetCharacterInfo");
                    }
                });
            }
        }
    }
    private class ContextHook extends BroadcastReceiver {

        private Object mainActivity; // MainTabsActivity

        private Object mChatManager; // ChatManager
        private TelegramBot bot;
        private int myAID;

        private HashMap<Integer,UserModel> users;

        public ContextHook(Object _mainAct){
            mainActivity = _mainAct;
            users = new HashMap<>();
        }

        @Override
        public void onReceive(Context context, final Intent intent) {
            final String action = intent.getAction();
            bot = (TelegramBot) XposedHelpers.getAdditionalInstanceField(this.mainActivity,"bot");
            if(!intent.hasExtra("token") || !intent.hasExtra("data") || !intent.getStringExtra("token").equals(Config.ACCESS_BRAODCAST_TOKEN)) {
                return;
            }
            if(action.equalsIgnoreCase(Config.BROADCAST_MESSAGE)){
                final ChatModel model = g.fromJson(intent.getStringExtra("data"),ChatModel.class);
                new Handler((Looper)XposedHelpers.callMethod(mainActivity,"getMainLooper")).post(new Runnable() {
                    @Override
                    public void run() {
                        Promise.with(this,Boolean.class).then(new Task<Boolean, UserModel>() {
                            @Override
                            public void run(Boolean aBoolean, NextTask<UserModel> nextTask) {
                                if(users.containsKey(model.SenderCID)){
                                    nextTask.run(users.get(model.SenderCID));
                                }else{
                                    MapleUtil.getInstance().getCharacterInfo(model.WID,model.SenderCID,nextTask);
                                }
                            }
                        }).then(new Task<UserModel, Void>() {
                            @Override
                            public void run(final UserModel userModel, NextTask<Void> nextTask) {
                                if(!users.containsKey(userModel.characterID)){
                                    users.put(userModel.characterID,userModel);
                                }
                                new AsyncTask<String, Void, Void>() {
                                    @Override
                                    protected Void doInBackground(String... params) {
                                        try{
                                            String uName = userModel.userName;
                                            if(uName.length() <= 1){
                                                uName = model.SenderAID + "";
                                            }
                                            bot.sendMessage(Config.MASTER_TELEGRAM_ID,"#" + uName + " : " + model.Msg);
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                        return null;
                                    }
                                }.execute();
                                nextTask.yield(0,null);
                            }
                        }).create().execute(true);
                    }
                });
                return;
            }
            if(action.equalsIgnoreCase(Config.TELEGRAM_MESSAGE)){
                new AsyncTask<String, Void, Void>() {
                    @Override
                    protected Void doInBackground(String... params) {
                        try{
                            bot.sendMessage(Config.MASTER_TELEGRAM_ID,intent.getStringExtra("data"));
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        return null;
                    }
                }.execute();
                return;
            }

            mChatManager = XposedHelpers.callStaticMethod(chatManager,"getInstance",new Class[]{Context.class},this.mainActivity);
            myAID = XposedHelpers.getIntField(mChatManager,"MyAID");
            if(myAID == 0){
                return;
            }

            final ChatModel chatmodel = g.fromJson(intent.getStringExtra("data"),ChatModel.class);
            chatmodel.SenderAID = myAID;
            if(chatmodel.SenderCID < 10){
                chatmodel.SenderCID = Config.CHARACTER_BOT_ID;
            }
            chatmodel.AID = myAID;
            chatmodel.WID = Config.WORLD_BOT_ID;

            final int[] friendaids = chatmodel.FriendAids;
            for(int j=0;j<friendaids.length;j+=1){
                if(friendaids[j] == Config.TELEGRAM_EMU_USERID){
                    new AsyncTask<Void,Void,Void>(){
                        @Override
                        protected Void doInBackground(Void... params) {
                            try{
                                bot.sendMessage(Config.MASTER_TELEGRAM_ID,chatmodel.Msg);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            return null;
                        }
                    }.execute();
                    return;
                }
            }

            Boolean addMe = true;
            ArrayList<Integer> sendAIDs = new ArrayList<>();
            for(int i=0;i<friendaids.length;i+=1){
                sendAIDs.add(friendaids[i]);
                if(friendaids[i] == myAID){
                    addMe = false;
                    break;
                }
            }
            if(addMe){
                sendAIDs.add(myAID);
                chatmodel.FriendAids = Ints.toArray(sendAIDs);
            }
            XposedHelpers.callStaticMethod(chatManager, "NativeFriendChatMessage",
                    chatmodel.AID,chatmodel.WID,chatmodel.SenderCID,
                    XposedHelpers.callStaticMethod(global,"StringToByte",chatmodel.Msg),chatmodel.FriendAids);
            new AsyncTask<ChatModel,Void,Void>(){
                @Override
                protected Void doInBackground(ChatModel... params) {
                    final ChatModel cm = params[0];
                    try{
                        if(myAID == 0) {
                            bot.sendMessage(Config.MASTER_TELEGRAM_ID,"ChatMessage: myAID is invaild - " + myAID);
                            return null;
                        }else{
                            //bot.sendMessage(Config.MASTER_TELEGRAM_ID,"Called NFCM - " + cm.AID + "," + cm.SenderCID + "," + g.toJson(cm.FriendAids));
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute(chatmodel);
        }
    }
    // int paramInt1, int paramInt2, int paramInt3, int paramInt4, long paramLong, byte[] paramArrayOfByte, byte paramByte
}
