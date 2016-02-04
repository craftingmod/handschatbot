package com.craftingmod.HandsHooker.hooker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.anprosit.android.promise.NextTask;
import com.anprosit.android.promise.Promise;
import com.anprosit.android.promise.Task;
import com.craftingmod.HandsHooker.BroadUtil;
import com.craftingmod.HandsHooker.Config;
import com.craftingmod.HandsHooker.MapleUtil;
import com.craftingmod.HandsHooker.model.ChatModel;
import com.craftingmod.HandsHooker.model.CoreUserModel;
import com.craftingmod.HandsHooker.model.FriendModel;
import com.craftingmod.HandsHooker.model.GuildModel;
import com.craftingmod.HandsHooker.model.OnlineModel;
import com.craftingmod.HandsHooker.model.UserModel;
import com.google.common.collect.Iterables;
import com.google.common.primitives.Ints;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramBotAdapter;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;

import java.lang.reflect.Type;
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

    protected final Class<?> MapleDBManager;
    protected final Class<?> MapleInfoModel;
    protected final Class<?> SoapProtocol;
    protected final Class<?> NativeClass;
    protected final Class<?> ModelOnline;
    protected final Class<?> chatThread;

    private final Gson g = new GsonBuilder().create();
    private IntentFilter intentF;



    public MessageHooker(XC_LoadPackage.LoadPackageParam pm) {
        super(pm);
        intentF = new IntentFilter();
        intentF.addAction(BroadUtil.RECEIVE_MESSAGE);
        intentF.addAction(BroadUtil.SEND_MESSAGE);
        intentF.addAction(BroadUtil.SEND_TELEGRAM);
        intentF.addAction(BroadUtil.REQUEST_GUILD);
        intentF.addAction(BroadUtil.RECEIVE_GUILD);
        intentF.addAction(BroadUtil.REQUEST_ONLINE);


        MapleDBManager = getMapleClass(".database.MHDatabaseManager");
        MapleInfoModel = getMapleClass(".structdata.MHCharacterInfo");
        SoapProtocol = getMapleClass(".SoapEx");
        NativeClass = XposedHelpers.findClass("com.Nexon.Common.NativeClass", cl);
        ModelOnline = getMapleClass(".structdata.MHOlineInfo");
        chatThread = getMapleClass(".ChatManager.ChatThread");
    }
    /**
     * Broadcast 1
     * name: Config.BROADCAST_MESSAGE
     * data: ChatModel Object (Gson)
     * token: Config.ACCESS_BRAODCAST_TOKEN
     */

    /**
     * Register Broadcast
     */
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

                XposedBridge.log("- Friend Sent model \n" + g.toJson(model));
                context.sendBroadcast(BroadUtil.buildF_Message(g, model));
            }
        });
        XposedBridge.hookAllMethods(chatManager, "OnGuildChatMessage", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Object[] args = param.args;
                String msg = (String) XposedHelpers.callStaticMethod(global,"NativeToString",
                        XposedHelpers.callStaticMethod(NativeClass,"FilterMessage",
                                XposedHelpers.callStaticMethod(global,"StringToByte",
                                        XposedHelpers.callStaticMethod(global,"NativeToString",args[6])
                        )
                )
                );
                ChatModel model = new ChatModel((int) args[2],(long)args[5],(int) args[0],(int) args[3],(int) args[4],(int) args[1],msg);
                Context context = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                XposedBridge.log("- Guild Sent model \n" + g.toJson(model));
                context.sendBroadcast(BroadUtil.buildG_Message(g, model));
            }
        });
        XposedBridge.hookAllMethods(chatManager, "OnGetGuildRequest", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Object[] args = param.args;

                int accountID = (int) args[0];
                int characterID = (int) args[2];

                int guildID = (int) args[4];
                int guildGrade = (int) args[5];
                String guildName = (String) XposedHelpers.callStaticMethod(global,"NativeToString",args[6]);

                GuildModel model = new GuildModel(guildID,guildGrade,guildName);
                model.setUser(accountID, characterID, (int) args[1]);
                Context context = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                context.sendBroadcast(BroadUtil.buildN_sendGuild(g, model));
            }
        });
        //   public void OnGetOnlineUserRequest(int paramInt1, int paramInt2, MHOlineInfo[] paramArrayOfMHOlineInfo)
        XposedBridge.hookAllMethods(chatManager, "OnGetOnlineUserRequest", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Object[] args = param.args;

                ArrayList<OnlineModel> sends = new ArrayList<>();
                Object[] outs = (Object[]) args[2];
                for(int i=0;i<outs.length;i+=1){
                    Object ob = outs[i];
                    OnlineModel model = new OnlineModel(
                            XposedHelpers.getIntField(ob,"aid"),XposedHelpers.getIntField(ob,"cid"),XposedHelpers.getByteField(ob, "bOnline") >= 1);
                    sends.add(model);
                }
                Context context = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                context.sendBroadcast(BroadUtil.buildN_sendOnline(g,sends));
            }
        });

        XposedBridge.hookAllMethods(chatThread, "ProcessMessage", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                if(param.args[0] instanceof android.os.Message){
                    try{
                        android.os.Message msg = (android.os.Message) param.args[0];
                        XposedBridge.log("ProcessMessage: " + g.toJson(msg.obj));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
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
                XposedHelpers.setAdditionalInstanceField(_this, "mReceiver", mReceiver);
                XposedHelpers.setAdditionalInstanceField(_this, "bot", TelegramBotAdapter.build(Config.TELEGRAM_BOT_TOKEN));
                context.registerReceiver(mReceiver, intentF);
                new Handler((Looper) XposedHelpers.callMethod(_this,"getMainLooper")).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // ChatManager.NativeEnterGuildChatRoomRequest(localMHCharacterInfo.mAccountID, localMHCharacterInfo.mWorldID, localMHCharacterInfo.mGuildID, localMHCharacterInfo.mCharacterID);
                        XposedHelpers.callStaticMethod(chatManager,"NativeEnterGuildChatRoomRequest",Config.MASTER_ACCOUNT_ID,Config.WORLD_BOT_ID,Config.GUILD_ID,Config.MASTER_CHARACTER.characterID);
                    }
                }, 10000);
            }
        });
        XposedBridge.hookAllMethods(talkActivity, "onDestroy", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                final Object _this = param.thisObject;
                final Context context = ((Context) _this);
                final ContextHook mReceiver = (ContextHook) XposedHelpers.getAdditionalInstanceField(_this, "mReceiver");
                context.unregisterReceiver(mReceiver);
                mReceiver.exit();
            }
        });
    }
    private class ContextHook extends BroadcastReceiver {

        private Object mainActivity; // MainTabsActivity
        private final Context mContext;
        private final int BOT_UPDATE_TIME = 2000; // millisecond

        private Object mChatManager; // ChatManager
        private TelegramBot bot; // Telegram bot
        private int myAID; // my AccountID
        private HandlerThread messageT; // Thread_Recieve msg
        private HandlerThread updateT; // Thread Update Telegram msg
        private Handler Mhandler; // Handler Recieve msg
        private Handler Thandler; // Handler Update Telegram msg
        private int lastMessageID; // Telegram LastMessage
        private int[] friendAids;

        private Timer timer;

        private HashMap<Integer,UserModel> masters; // master acconunt
        private HashMap<Integer,UserModel> users; // user

        public ContextHook(Object _mainAct){
            mainActivity = _mainAct;
            mContext = (Context) mainActivity;
            users = new HashMap<>();
            masters = new HashMap<>();
            friendAids = new int[]{Config.MASTER_ACCOUNT_ID}; // Avoid error

            messageT = new HandlerThread("MapleHandsThread");
            updateT = new HandlerThread("HandsTelegramListener");
            messageT.start();
            updateT.start();

            Mhandler = new Handler(messageT.getLooper());
            Thandler = new Handler(updateT.getLooper());

            lastMessageID = 0;

            /**
             * Telegram recieve message code
             */
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Thandler.post(new Runnable() {
                        @Override
                        public void run() {
                            List<Update> updates;
                            try {
                                if (lastMessageID == 0) {
                                    updates = bot.getUpdates(null, 20, 1000).updates();
                                } else {
                                    updates = bot.getUpdates(lastMessageID, 20, 1000).updates();
                                }
                                if (updates.size() >= 1) {
                                    onTelegramMessage(updates);
                                }
                            } catch (Exception e) {
                            }
                        }
                    });
                }
            }, 0, BOT_UPDATE_TIME);

            syncFriends();
        }
        private void syncFriends(){
            Mhandler.post(new Runnable() {
                @Override
                public void run() {
                    ArrayList<FriendModel> models = MapleUtil.getAccountFriendList(Config.MASTER_ACCOUNT_ID);
                    for(int i=0;i<models.size();i+=1){
                        if(models.get(i).worldID != Config.WORLD_BOT_ID){
                            models.remove(i);
                            i -= 1;
                        }
                    }
                    int[] mFriends = new int[models.size()+1];
                    for(int i=0;i<models.size();i+=1){
                        mFriends[i] = models.get(i).accountID;
                    }
                    mFriends[models.size()] = Config.MASTER_ACCOUNT_ID;
                    friendAids = mFriends;
                }
            });
        }

        /**
         * Onrecieve Telegram Msg
         * @param updates
         */
        public void onTelegramMessage(List<Update> updates){
            for(int i=0;i<updates.size();i+=1){
                Message message = updates.get(i).message();
                lastMessageID = updates.get(i).updateId() + 1;
                ChatModel cm = Config.CHAT_HANDS;
                cm.FriendAids = friendAids;
                cm.Msg = message.text();
                mContext.sendBroadcast(BroadUtil.buildF_sendMessage(g,cm));
            }
        }

        public void exit(){
            messageT.quit();
            updateT.quit();
            timer.cancel();
        }

        @Override
        public void onReceive(Context context, final Intent intent) {
            if(!intent.hasExtra("token") || !intent.hasExtra("data") || !intent.getStringExtra("token").equals(Config.ACCESS_BRAODCAST_TOKEN)) {
                return;
            }
            /* ********* */
            final String action = intent.getAction();
            bot = (TelegramBot) XposedHelpers.getAdditionalInstanceField(this.mainActivity, "bot");
            mChatManager = XposedHelpers.callStaticMethod(chatManager, "getInstance", new Class[]{Context.class}, this.mainActivity);
            myAID = XposedHelpers.getIntField(mChatManager, "MyAID");

            /**
             * Log message to telegram
             */
            if(action.equalsIgnoreCase(BroadUtil.RECEIVE_MESSAGE)){
                final ChatModel cModel = g.fromJson(intent.getStringExtra("data"),ChatModel.class);
                final Boolean Guild = intent.getIntExtra("type",BroadUtil.TYPE_FRIEND) == BroadUtil.TYPE_GUILD;
                final int sID = cModel.SenderCID;
                Mhandler.post(new Runnable() {
                    @Override
                    public void run() {
                        UserModel uModel = null;
                        String prefix = "#";
                        String uName = "";
                        if(users.containsKey(sID)){
                            uModel = users.get(sID);
                        }else{
                            try{
                                uModel = MapleUtil.getCharacterInfo(cModel.WID,sID);
                            }catch (Exception e){
                                if(masters.containsKey(cModel.SenderAID)){
                                    uModel = masters.get(cModel.SenderAID);
                                }
                            }
                        }
                        if(uModel == null){
                            //Dont have any infomation
                            return;
                        }
                        final int aID = uModel.accountID;
                        if(uModel.userName == null || uModel.userName.length() < 2){
                            prefix = "$";
                            if(masters.containsKey(aID)){
                                uName = masters.get(aID).userName;
                            }else{
                                uName = uModel.accountID + "";
                            }
                        }else{
                            uName = uModel.userName;
                            if(!users.containsKey(sID)){
                                users.put(sID,uModel);
                            }
                            if(masters.containsKey(aID)){
                                if(masters.get(aID).level < uModel.level){
                                    masters.remove(aID);
                                    masters.put(aID,uModel);
                                }
                            }else{
                                masters.put(aID,uModel);
                            }
                        }
                        if(Guild){
                            prefix = prefix + "G_";
                        }
                        bot.sendMessage(Config.MASTER_TELEGRAM_ID,prefix + uName +  " : " + cModel.Msg);
                    }
                });
                return;
            }
            if(action.equalsIgnoreCase(BroadUtil.SEND_TELEGRAM)){
                Mhandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            bot.sendMessage(Config.MASTER_TELEGRAM_ID, intent.getStringExtra("data"));
                        } catch (Exception e) {

                        }
                    }
                });
                return;
            }
            if(action.equalsIgnoreCase(BroadUtil.REQUEST_GUILD)){
                XposedHelpers.callStaticMethod(chatManager, "NativeGetGuildIDRequest",
                        intent.getIntExtra("aid", Config.MASTER_ACCOUNT_ID),
                        intent.getIntExtra("cid", Config.CHAT_HANDS.SenderCID),
                        intent.getIntExtra("wid", Config.WORLD_BOT_ID));
                return;
            }
            if(action.equalsIgnoreCase(BroadUtil.RECEIVE_GUILD)){
                return;
            }
            /**
             * public class MHOlineInfo
             {
             public int aid;
             public byte bOnline;
             public int cid;
             }
             */
            if(action.equalsIgnoreCase(BroadUtil.REQUEST_ONLINE)){
                Type type = new TypeToken<ArrayList<CoreUserModel>>(){}.getType();
                ArrayList<CoreUserModel> searches = g.fromJson(intent.getStringExtra("data"), type);
                ArrayList<Object> params = new ArrayList<>();
                //  NativeGetOnlineUserRequest(int paramInt1, int paramInt2, MHOlineInfo[] paramArrayOfMHOlineInfo);
                for(int i=0;i<searches.size();i+=1){
                    CoreUserModel cModel = searches.get(i);
                    Object model = XposedHelpers.newInstance(ModelOnline, Void.class);
                    XposedHelpers.setObjectField(model,"aid",cModel.accountID);
                    XposedHelpers.setObjectField(model,"cid",cModel.characterID);
                    XposedHelpers.setObjectField(model,"bOnline",0);
                    params.add(model);
                }
                XposedHelpers.callStaticMethod(chatManager, "NativeGetOnlineUserRequest",
                        myAID, Config.WORLD_BOT_ID, Iterables.toArray(params, Object.class));
                return;
            }

            // BroadUtil.SEND_MESSAGE
            if(!action.equalsIgnoreCase(BroadUtil.SEND_MESSAGE)){
                return;
            }

            final Boolean Guild = intent.getIntExtra("type",BroadUtil.TYPE_FRIEND) == BroadUtil.TYPE_GUILD;

            if(myAID == 0){
                return;
            }

            final ChatModel chatmodel = g.fromJson(intent.getStringExtra("data"),ChatModel.class);
            chatmodel.SenderAID = myAID;
            if(!Guild){
                // Friend
                if(chatmodel.SenderCID < 10){
                    chatmodel.SenderCID = Config.CHARACTER_BOT_ID;
                }
                chatmodel.AID = myAID;
                chatmodel.WID = Config.WORLD_BOT_ID;

                final int[] friendaids = chatmodel.FriendAids;
                Mhandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Boolean addMe = true;
                        ArrayList<Integer> sendAIDs = new ArrayList<>();

                        for(int i=0;i<friendaids.length;i+=1){
                            sendAIDs.add(friendaids[i]);
                            if(friendaids[i] == myAID){
                                addMe = false;
                                continue;
                            }
                        }
                        if(addMe){
                            sendAIDs.add(myAID);
                            chatmodel.FriendAids = Ints.toArray(sendAIDs);
                        }
                        XposedHelpers.callStaticMethod(chatManager, "NativeFriendChatMessage",
                                chatmodel.AID, chatmodel.WID, chatmodel.SenderCID,
                                XposedHelpers.callStaticMethod(global, "StringToByte", chatmodel.Msg), chatmodel.FriendAids);
                    }
                });
            }else{
                //Guild
                // ChatManager.NativeGuildChatMessage(paramMessage.AID, paramMessage.WID, paramMessage.SenderCID, (int)paramMessage.Roomkey, G.StringToByte(paramMessage.Msg));
                // {"AID":5733475,"Msg":"우찌됬든","RegisterDate":0,"Roomkey":844558,"SenderAID":5733475,"SenderCID":83396524,"Status":0,"WID":3}
                chatmodel.AID = myAID;
                chatmodel.WID = Config.MASTER_CHARACTER.worldID;
                chatmodel.SenderCID = Config.MASTER_CHARACTER.characterID;
                chatmodel.Roomkey = Config.GUILD_ID;
                chatmodel.SenderAID = myAID;

                XposedHelpers.callStaticMethod(chatManager,"NativeGuildChatMessage",
                        chatmodel.AID,chatmodel.WID,chatmodel.SenderCID,chatmodel.Roomkey,
                        XposedHelpers.callStaticMethod(global,"StringToByte",chatmodel.Msg));

            }
        }
    }
    // int paramInt1, int paramInt2, int paramInt3, int paramInt4, long paramLong, byte[] paramArrayOfByte, byte paramByte
}
