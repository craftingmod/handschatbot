package com.craftingmod.maplebot.heavy;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;

import com.anprosit.android.promise.NextTask;
import com.anprosit.android.promise.Promise;
import com.anprosit.android.promise.Task;
import com.craftingmod.maplebot.BroadUtil;
import com.craftingmod.maplebot.communicate.CharSearch;
import com.craftingmod.maplebot.Config;
import com.craftingmod.maplebot.R;
import com.craftingmod.maplebot.communicate.BaseSocket;
import com.craftingmod.maplebot.communicate.ResultObject;
import com.craftingmod.maplebot.heavy.core.CustomDB;
import com.craftingmod.maplebot.heavy.core.SQLFinder;
import com.craftingmod.maplebot.heavy.core.UserDB;
import com.craftingmod.maplebot.heavy.modules.BackDel;
import com.craftingmod.maplebot.heavy.modules.HeavyModule;
import com.craftingmod.maplebot.heavy.modules.Online;
import com.craftingmod.maplebot.heavy.modules.UInfo;
import com.craftingmod.maplebot.light.modules.CoreModule;
import com.craftingmod.maplebot.model.CModel;
import com.craftingmod.maplebot.model.ChatModel;
import com.craftingmod.maplebot.model.SimpleUserModel;
import com.craftingmod.maplebot.model.UserModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by superuser on 16/2/15.
 */
public class HeavyService extends Service implements IServiceHeavy {

    private Gson g;
    private IReceiver sReceiver;
    private ArrayList<HeavyModule> modules;
    private HandlerThread threadUser;
    private Handler userHandler;
    private Handler mainHandler;

    private HashMap<Long,UserModel> users;

    private HeavyService _this;
    private CustomDB dbCache;
    private UserDB dbUser;
    private BaseSocket api;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        _this = this;
        threadUser = new HandlerThread("UserFinder");
        threadUser.start();
        sReceiver = new IReceiver(this);
        sReceiver.onCreate();

        g = new GsonBuilder().create();
        users = new HashMap<>();
        dbCache = new CustomDB(this.getContext());
        dbUser = new UserDB(this.getContext());
        mainHandler = new Handler(this.getMainLooper());
        userHandler = new Handler(threadUser.getLooper());
        api = BaseSocket.buildClient(this.getContext(),"charaName");
        /// Module
        initModules();
        /// end
        startForeground();
        return super.onStartCommand(intent, flags, startId);
    }
    private void initModules(){
        if(modules == null){
            modules = new ArrayList<>();
        }
        HeavyModule[] md = new HeavyModule[]{
                new UInfo(this),new Online(this),new BackDel(this)
        };
        modules.clear();
        String initS = "HeavyService 실행 - ";
        for(HeavyModule module : md){
            initS += module.getName() + ",";
            modules.add(module);
        }
        initS = initS.substring(0,initS.length()-1);

        ChatModel model = Config.bot.clone();
        model.FriendAids = new int[]{Config.MASTER_ACCOUNT_ID};
        model.Msg = initS;
        NativeSendMessage(model);
    }
    @Override
    public void request(final HeavyModule module, final CModel chat, final String username) {
        Promise.with(this,Boolean.class).then(new Task<Boolean, ResultObject>() {
            @Override
            public void run(Boolean aBoolean, NextTask<ResultObject> nextTask) {
                ArrayList<UserModel> models = dbUser.searchUser(SQLFinder.getSearch(username));
                if(models.size() >= 1){
                    ResultObject result = new ResultObject(CharSearch.OK);
                    result.data = g.toJson(models.get(0));
                    nextTask.run(result);
                }else{
                    api.request(username,username,nextTask);
                }
            }
        }).then(new Task<ResultObject, Void>() {
            @Override
            public void run(ResultObject resultObject, NextTask<Void> nextTask) {
                if(resultObject.status == CharSearch.NULL){
                    module.handleCommand(chat,null);
                }else if(resultObject.status == CharSearch.TIMEOUT){
                    module.handleTimeout(chat);
                }else if(resultObject.status == CharSearch.OK){
                    UserModel baseID = g.fromJson(resultObject.data,UserModel.class);
                    SimpleUserModel user = new SimpleUserModel(baseID.accountID,baseID.characterID,username);
                    user.worldID = baseID.worldID;
                    module.handleCommand(chat,user);
                }
            }
        }).create().execute(true);
        /*
        UserModel send = MapleUtil.getCharacterInfo(chat.worldID,chat.characterID);
        ArrayList<UserModel> listTarget = db.searchCache(SQLFinder.getSearch(username));
        //ArrayList<UserModel> listTarget = db.searchUser(SQLFinder.getSearch(username));
        if(listTarget.size() != 1){
            listTarget = db.searchUser(SQLFinder.getSearch(username));
            // 용량없음!
        }
        if(listTarget.size() != 1){

        }
        */
    }

    @Override
    public UserModel getUser(int worldID, int characterID) {
        double key = CustomDB.genKey(worldID, characterID);
        if(users.containsKey(key)){
            UserModel user = users.get(key);
            if(user.lastTimestamp >= System.currentTimeMillis()/1000-7200){
                users.remove(key);
                return null;
            }else{
                return user;
            }
        }
        return null;
    }

    @Override
    public UserDB getDB() {
        return dbUser;
    }

    @Override
    public void putUser(UserModel model) {
        long key = CustomDB.genKey(model.worldID, model.characterID);
        if(users.containsKey(key)){
            users.remove(key);
        }
        model.lastTimestamp = System.currentTimeMillis()/1000;
        users.put(key,model);
    }

    @Override
    public void writeUser(UserModel model) {
        try{
            dbCache.put(new SimpleUserModel(model.accountID,model.characterID,model.worldID,model.userName));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equalsIgnoreCase(BroadUtil.RECEIVE_MESSAGE) && intent.hasExtra("data")){
            Log.d("data:",intent.getStringExtra("data"));
            final ChatModel oldChat = g.fromJson(intent.getStringExtra("data"),ChatModel.class);
            if(oldChat == null){
                return;
            }
            final Boolean Friend = intent.getIntExtra("type",BroadUtil.TYPE_FRIEND) == BroadUtil.TYPE_FRIEND;
            final CModel chat = new CModel(oldChat,Friend);
            if(Friend && chat.text.equalsIgnoreCase("!help")){
                help(chat);
                return;
            }
            for(HeavyModule module : modules){
                if(module.shouldMessage(chat)){
                    module.handleMessage(chat);
                }
            }
            for(HeavyModule module : modules){
                if(module.shouldCommand(chat)){
                    module.handleCommand(chat);
                }
            }
        }
    }
    private TimerTask tTask = null;
    private void help(final CModel chat){
        if(tTask == null){
            final ArrayList<String> messages = new ArrayList<>();
            for(int k=0;k<modules.size();k+=1){
                String[] strArray = modules.get(k).getHelp();
                if(strArray != null){
                    for(String str : strArray){
                        messages.add("> " + str);
                    }
                }
            }
            tTask = new TimerTask() {
                private int i = 0;
                @Override
                public void run() {
                    sendMessage(chat,messages.get(i));
                    if(messages.size()-1 == i){
                        tTask = null;
                        this.cancel();
                    }else{
                        i += 1;
                    }
                }
            };
            new Timer().schedule(tTask,0,1000);
        }else{
            sendMessage(chat,"이미 사용중입니다.");
        }
    }

    @Override
    public Context getContext() {
        return this.getBaseContext();
    }


    public void sendMessage(CModel model,String msg){
        if(model.friend){
            ChatModel cm = Config.bot.clone();
            cm.Msg = msg;
            cm.FriendAids = new int[]{Config.MASTER_ACCOUNT_ID,model.accountID};
            NativeSendMessage(cm);
        }
    }
    private void NativeSendMessage(ChatModel model){
        this.sendBroadcast(BroadUtil.buildF_sendMessage(g,model));
    }

    private void startForeground(){
        /**
         * Foreground notification
         */
        Notification.Builder mBuilder =
                new Notification.Builder(this)
                        .setSmallIcon(R.drawable.ic)
                        .setContentTitle("<Heavy> Chatbot")
                        .setContentText("It is running!");
        startForeground(5891, mBuilder.build());
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        sReceiver.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class IReceiver extends BroadcastReceiver {

        private HeavyService objR;
        private final IntentFilter filter;
        protected IReceiver(HeavyService ob){
            objR = ob;
            filter = new IntentFilter(BroadUtil.RECEIVE_MESSAGE);
        }

        @Override
        public void onReceive(final Context context, final Intent intent) {
            objR.mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    objR.onReceive(context, intent);
                }
            });
        }
        public void onCreate(){
            objR.registerReceiver(this,filter);
        }
        public void onDestroy(){
            try{
                objR.unregisterReceiver(this);
            }catch (IllegalArgumentException e){
                e.printStackTrace();
            }
        }
    }
}
