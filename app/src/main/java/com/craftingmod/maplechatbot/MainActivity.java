package com.craftingmod.maplechatbot;

import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.anprosit.android.promise.Callback;
import com.anprosit.android.promise.NextTask;
import com.anprosit.android.promise.Promise;
import com.anprosit.android.promise.Task;
import com.craftingmod.maplechatbot.chat.CharacterFinder;
import com.craftingmod.maplechatbot.chat.ChatService;
import com.craftingmod.maplechatbot.chat.SyncService;
import com.craftingmod.maplechatbot.model.FriendModel;
import com.craftingmod.maplechatbot.model.SimpleUserModel;
import com.craftingmod.maplechatbot.model.UserModel;
import com.google.common.base.Splitter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import android.os.Handler;

import org.apache.commons.codec.binary.Base64;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

public class MainActivity extends AppCompatActivity {

    private LinearLayout mainLayout;
    private TextView info;
    private Gson g;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mainLayout = (LinearLayout) findViewById(R.id.layout);
        info = (TextView) findViewById(R.id.info);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button syncBt = (Button) findViewById(R.id.syncB);
        syncBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // syncDB();
            }
        });
        Button testBt = (Button) findViewById(R.id.testB);
        testBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testDB();
            }
        });
        g = new GsonBuilder().create();

        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */
        execService();

        Log.d("WBUS", System.currentTimeMillis() + "");
        final SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        /*
        new Thread(new Runnable() {
            @Override
            public void run() {
                SoapObject so = new SoapObject("http://api.maplestory.nexon.com/soap/", "GetAccountFriendList");
                //so.addProperty("WorldID",3);
                //so.addProperty("CharacterID", 85599666);
                so.addProperty("AccountID", 23468404);
                SoapSerializationEnvelope sObj = new SoapSerializationEnvelope(120);
                sObj.setOutputSoapObject(so);
                sObj.dotNet = true;
                HttpTransportSE mo = new HttpTransportSE("http://api.maplestory.nexon.com/soap/MobileApp.asmx",10000);
                mo.debug = false;
                try {
                    mo.call("http://api.maplestory.nexon.com/soap/GetAccountFriendList", sObj);
                    SoapObject sob = (SoapObject) sObj.getResponse();

                    String encoded = new String(Base64.encodeBase64(sob.toString().getBytes()));
                    pref.edit().putString("test",encoded).apply();
                    List<String> split = Splitter.on("AccountFriendList=anyType").trimResults().omitEmptyStrings().splitToList(sob.toString());
                    for(int i=0;i<split.size();i+=1){
                        Log.d("WBUS-HI", split.get(i));
                    }

                   // SoapObject ob2 = (SoapObject) ((SoapObject) ((SoapObject) sob.getProperty("diffgram")).getProperty("NewDataSet")).getProperty("dtCharacterInfo");
                   // Log.d("WBUS", ob2.getPropertyAsString("AccountID"));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        */
        MapleUtil.getInstance(this);
        Promise.with(this,Boolean.class).then(new Task<Boolean, ArrayList<FriendModel>>() {
            @Override
            public void run(Boolean aBoolean, NextTask<ArrayList<FriendModel>> nextTask) {
                MapleUtil.getInstance().getAccountFriendList(21517032,nextTask);
            }
        }).then(new Task<ArrayList<FriendModel>, Void>() {
            @Override
            public void run(ArrayList<FriendModel> friendModels, NextTask<Void> nextTask) {
                for(int i=0;i<friendModels.size();i+=1){
                    Log.d("WBUS",g.toJson(friendModels.get(i)));
                }
                nextTask.yield(0,null);
            }
        }).create().execute(true);
        Promise.with(this, Boolean.class).then(new Task<Boolean, UserModel>() {
            @Override
            public void run(Boolean aBoolean, NextTask<UserModel> nextTask) {
                MapleUtil.getInstance().getCharacterInfo(3, Config.CHAT_HANDS.SenderCID, nextTask);
            }
        }).then(new Task<UserModel, Void>() {
            @Override
            public void run(UserModel userModel, NextTask<Void> nextTask) {
                Log.d("WBUS",g.toJson(userModel));
                nextTask.yield(0,null);
            }
        }).create().execute(true);
        Promise.with(this,Boolean.class).then(new Task<Boolean, Boolean>() {
            @Override
            public void run(Boolean aBoolean, NextTask<Boolean> nextTask) {
                MapleUtil.getInstance().isGameLogin(Config.MASTER_ACCOUNT_ID, nextTask);
            }
        }).then(new Task<Boolean, Void>() {
            @Override
            public void run(Boolean aBoolean, NextTask<Void> nextTask) {
                Log.d("WBUS",aBoolean?"true":"false");
                nextTask.yield(0,null);
            }
        }).create().execute(true);
        Promise.with(this,Boolean.class).then(new Task<Boolean, ArrayList<SimpleUserModel>>() {
            @Override
            public void run(Boolean aBoolean, NextTask<ArrayList<SimpleUserModel>> nextTask) {
                MapleUtil.getInstance().getCharacterList(21517032,3,nextTask);
            }
        }).then(new Task<ArrayList<SimpleUserModel>, Void>() {
            @Override
            public void run(ArrayList<SimpleUserModel> simpleUserModels, NextTask<Void> nextTask) {
                Log.d("WBUS",g.toJson(simpleUserModels));
                nextTask.yield(0,null);
            }
        }).create().execute(true);

    }
    private void syncDB(){
        Shell.SU.run(new String[]{"rm -rf /data/mapleChat",
                "mkdir /data/mapleChat",
                "cp -R /data/data/com.Nexon.MonsterLifeChat/databases/maplehands.db /data/mapleChat/maple.db",
                "chmod 775 /data/mapleChat/maple.db"});
        Snackbar.make(mainLayout, "Synced.", Snackbar.LENGTH_SHORT).show();
    }
    private void testDB(){
        Promise.with(this,Integer.class)
                .then(new Task<Integer, HashMap<Integer,String>>() {
                    @Override
                    public void run(Integer integer, NextTask<HashMap<Integer,String>> task) {
                        Log.d("WBUS", "Hello!");

                        task.run(CharacterFinder.getSearch(94141462,83364669));
                        //cl.handle(msg);
                    }
                })
                .then(CharacterFinder.getInstance())
                .setCallback(new Callback<ArrayList<UserModel>>() {
                    @Override
                    public void onSuccess(ArrayList<UserModel> userModels) {
                        if (userModels.size() != 1) {
                            info.setText("DB Error.");
                        } else {
                            info.setText(new GsonBuilder().create().toJson(userModels.get(0)));
                        }
                    }

                    @Override
                    public void onFailure(Bundle bundle, Exception e) {
                        if (e != null) {
                            e.printStackTrace();
                        }
                    }
                })
                .create().execute(0);
    }
    private void execService() {
        //Intent intent = new Intent(this, ChatService.class);
        Intent intent = new Intent(this, ChatService.class);
        String packageName = this.getPackageName();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= 23) {
            if (pm.isIgnoringBatteryOptimizations(packageName)){
                intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            }else {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
            }
        }
        if(isServiceRunning(ChatService.class)){
            Snackbar.make(mainLayout, "Service is already running", Snackbar.LENGTH_SHORT).show();
        }else{
            startService(intent);
            Snackbar.make(mainLayout, "Service started", Snackbar.LENGTH_SHORT).show();
        }
    }
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName()) && service.started) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
