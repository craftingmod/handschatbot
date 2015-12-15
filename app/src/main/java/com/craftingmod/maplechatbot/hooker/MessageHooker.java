package com.craftingmod.maplechatbot.hooker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.craftingmod.maplechatbot.Config;
import com.craftingmod.maplechatbot.MyApplication;
import com.craftingmod.maplechatbot.model.ChatModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramBotAdapter;

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

    public static final String SEND_MSG = "com.craftingmod.SEND_MSG";
    public static final String INIT_INFO = "com.craftingmod.INIT_HANDS";
    public static final String REQUIRE_INFO = "com.craftingmod.request.REQUIRE_INFO";

    public MessageHooker(XC_LoadPackage.LoadPackageParam pm) {
        super(pm);
        g = new GsonBuilder().create();
    }

    @Override
    public void handlePackage() {
        XposedHelpers.findAndHookConstructor(chatManager, Context.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Context context = ((Context) param.args[0]);
                //        .registerReceiver();
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                XposedHelpers.setAdditionalInstanceField(param.thisObject,"bot",
                        TelegramBotAdapter.build(Config.TELEGRAM_BOT_TOKEN));
            }
        });
    }
    private class ContextHook extends BroadcastReceiver {

        private String action;
        private MethodHookParam param;
        private BroadcastReceiver _this;

        public ContextHook(MethodHookParam pm){
            param = pm;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            action = intent.getAction();
            final Object cm_this = this.param.thisObject;
            int myAID = XposedHelpers.getIntField(cm_this,"MyAID");
            if(intent.getStringExtra("token") != null && intent.getStringExtra("token").equals(Config.ACCESS_BRAODCAST_TOKEN)) {
                TelegramBot bot = (TelegramBot) XposedHelpers.getAdditionalInstanceField(cm_this,"bot");
                ChatModel model = g.fromJson(intent.getStringExtra("data"),ChatModel.class);
                XposedHelpers.
            }
            if(this.action.equalsIgnoreCase(SEND_MSG)) {
                if(myAID == 0 || myAID != intent.getIntExtra("accountID",0)){
                    _bot.sendMessage(Config.MASTER_TELEGRAM_ID,"ChatMessage: myAID is invaild - " + myAID + " / " + intent.getIntExtra("aid",0));
                } else if (intent.hasExtra("worldID")){
                }
            }else if(this.action.equalsIgnoreCase(INIT_INFO)) {
                XposedHelpers.setIntField(cm_this,"myCID",intent.getIntExtra("cid",0));
            }
        }
    }
    private class hookContext extends XC_MethodHook {
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
    }
    // int paramInt1, int paramInt2, int paramInt3, int paramInt4, long paramLong, byte[] paramArrayOfByte, byte paramByte

}
