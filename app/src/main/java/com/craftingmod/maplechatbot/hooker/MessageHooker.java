package com.craftingmod.maplechatbot.hooker;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.craftingmod.maplechatbot.Config;
import com.craftingmod.maplechatbot.MyApplication;
import com.craftingmod.maplechatbot.model.ChatModel;
import com.google.common.primitives.Ints;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramBotAdapter;

import java.sql.Time;
import java.util.ArrayList;
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

    private final Gson g = new GsonBuilder().create();
    private IntentFilter intentF;

    public MessageHooker(XC_LoadPackage.LoadPackageParam pm) {
        super(pm);
        intentF = new IntentFilter();
        intentF.addAction(Config.SEND_MESSAGE);
        intentF.addAction(Config.TELEGRAM_MESSAGE);
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
                XposedHelpers.setAdditionalInstanceField(_this,"msgReceiver",mReceiver);
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
                context.unregisterReceiver(mReceiver);
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
    private class ContextHook extends BroadcastReceiver {

        private Object mainActivity; // MainTabsActivity

        private Object mChatManager; // ChatManager
        private TelegramBot bot;
        private int myAID;

        public ContextHook(Object _mainAct){
            mainActivity = _mainAct;
        }

        @Override
        public void onReceive(Context context, final Intent intent) {
            final String action = intent.getAction();
            bot = (TelegramBot) XposedHelpers.getAdditionalInstanceField(this.mainActivity,"bot");
            if(!intent.hasExtra("token") || !intent.hasExtra("data") || !intent.getStringExtra("token").equals(Config.ACCESS_BRAODCAST_TOKEN)) {
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
