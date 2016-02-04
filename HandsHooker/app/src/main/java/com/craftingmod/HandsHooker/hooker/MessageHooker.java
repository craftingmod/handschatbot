package com.craftingmod.HandsHooker.hooker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import com.anprosit.android.promise.NextTask;
import com.anprosit.android.promise.Promise;
import com.anprosit.android.promise.Task;
import com.craftingmod.HandsHooker.Config;
import com.craftingmod.HandsHooker.MapleUtil;
import com.craftingmod.HandsHooker.model.ChatModel;
import com.craftingmod.HandsHooker.model.FriendModel;
import com.craftingmod.HandsHooker.model.UserModel;
import com.google.common.primitives.Ints;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramBotAdapter;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.robv.android.xposed.XC_MethodHook;
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
                i.putExtra("token", Config.ACCESS_BRAODCAST_TOKEN);
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
                XposedHelpers.setAdditionalInstanceField(_this, "msgReceiver", mReceiver);
                XposedHelpers.setAdditionalInstanceField(_this, "bot", TelegramBotAdapter.build(Config.TELEGRAM_BOT_TOKEN));
                context.registerReceiver(mReceiver, intentF);
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
                mReceiver.exit();
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
        private final int BOT_UPDATE_TIME = 2000; // millisecond

        private Object mChatManager; // ChatManager
        private TelegramBot bot;
        private int myAID;
        private HandlerThread thread;
        private HandlerThread updateT;
        private Handler handler;
        private Handler Thandler;
        private int lastMessageID;
        private int[] friendAids;

        private Timer timer;

        private HashMap<Integer,UserModel> users;

        public ContextHook(Object _mainAct){
            mainActivity = _mainAct;
            users = new HashMap<>();

            thread = new HandlerThread("MapleHandsThread");
            updateT = new HandlerThread("HandsTelegramListener");
            thread.start();
            updateT.start();
            handler = new Handler(thread.getLooper());
            Thandler = new Handler(updateT.getLooper());
            friendAids = new int[]{Config.MASTER_ACCOUNT_ID};

            lastMessageID = 0;
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Thandler.post(new Runnable() {
                        @Override
                        public void run() {
                            List<Update> updates;
                            try{
                                if (lastMessageID == 0) {
                                    updates = bot.getUpdates(null, 20, 1000).updates();
                                } else {
                                    updates = bot.getUpdates(lastMessageID, 20, 1000).updates();
                                }
                                if (updates.size() >= 1) {
                                    onTelegramMessage(updates);
                                }
                            }catch (Exception e){
                            }
                        }
                    });
                }
            },0,2000);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    ArrayList<FriendModel> models = MapleUtil.getInstance().getAccountFriendList(Config.MASTER_ACCOUNT_ID);
                    for(int i=0;i<models.size();i+=1){
                        if(models.get(i).worldID != Config.WORLD_BOT_ID){
                            models.remove(i);
                            i -= 1;
                        }
                    }
                    friendAids = new int[models.size()+1];
                    for(int i=0;i<models.size();i+=1){
                        friendAids[i] = models.get(i).accountID;
                    }
                    friendAids[models.size()] = Config.MASTER_ACCOUNT_ID;
                }
            });
        }

        public void onTelegramMessage(List<Update> updates){
            for(int i=0;i<updates.size();i+=1){
                Message message = updates.get(i).message();
                lastMessageID = updates.get(i).updateId() + 1;
                ChatModel cm = Config.CHAT_HANDS;
                cm.FriendAids = friendAids;
                cm.Msg = message.text();
                NativeSendMessage(cm);
            }
        }
        private void NativeSendMessage(ChatModel model){
            Intent intent = new Intent(Config.SEND_MESSAGE);
            intent.putExtra("data",g.toJson(model));
            intent.putExtra("token",Config.ACCESS_BRAODCAST_TOKEN);
            onReceive((Context)mainActivity,intent);
        }

        public void exit(){
            thread.quit();
            updateT.quit();
            timer.cancel();
        }

        @Override
        public void onReceive(Context context, final Intent intent) {
            final String action = intent.getAction();
            bot = (TelegramBot) XposedHelpers.getAdditionalInstanceField(this.mainActivity, "bot");
            if(!intent.hasExtra("token") || !intent.hasExtra("data") || !intent.getStringExtra("token").equals(Config.ACCESS_BRAODCAST_TOKEN)) {
                return;
            }
            if(action.equalsIgnoreCase(Config.BROADCAST_MESSAGE)){
                final ChatModel model = g.fromJson(intent.getStringExtra("data"),ChatModel.class);
                new Handler((Looper) XposedHelpers.callMethod(mainActivity, "getMainLooper")).post(new Runnable() {
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
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            String uName = userModel.userName;
                                            if (uName.length() <= 1) {
                                                uName = model.SenderAID + "";
                                            }
                                            bot.sendMessage(Config.MASTER_TELEGRAM_ID, "#" + uName + " : " + model.Msg);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                nextTask.yield(0,null);
                            }
                        }).create().execute(true);
                    }
                });
                return;
            }
            if(action.equalsIgnoreCase(Config.TELEGRAM_MESSAGE)){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            bot.sendMessage(Config.MASTER_TELEGRAM_ID, intent.getStringExtra("data"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                return;
            }

            mChatManager = XposedHelpers.callStaticMethod(chatManager, "getInstance", new Class[]{Context.class}, this.mainActivity);
            myAID = XposedHelpers.getIntField(mChatManager, "MyAID");
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
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                bot.sendMessage(Config.MASTER_TELEGRAM_ID, chatmodel.Msg);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
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
                    chatmodel.AID, chatmodel.WID, chatmodel.SenderCID,
                    XposedHelpers.callStaticMethod(global, "StringToByte", chatmodel.Msg), chatmodel.FriendAids);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    final ChatModel cm = chatmodel;
                    try{
                        if(myAID == 0) {
                            bot.sendMessage(Config.MASTER_TELEGRAM_ID,"ChatMessage: myAID is invaild - " + myAID);
                        }else{
                            //bot.sendMessage(Config.MASTER_TELEGRAM_ID,"Called NFCM - " + cm.AID + "," + cm.SenderCID + "," + g.toJson(cm.FriendAids));
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
        }
    }
    // int paramInt1, int paramInt2, int paramInt3, int paramInt4, long paramLong, byte[] paramArrayOfByte, byte paramByte
}
