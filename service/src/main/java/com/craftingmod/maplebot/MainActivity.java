package com.craftingmod.maplebot;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.craftingmod.maplebot.BroadUtil;
import com.craftingmod.maplebot.R;
import com.craftingmod.maplebot.heavy.HeavyService;
import com.craftingmod.maplebot.model.CoreUserModel;
import com.craftingmod.maplebot.model.FriendModel;
import com.craftingmod.maplebot.model.OnlineModel;
import com.craftingmod.maplebot.model.SimpleUserModel;
import com.craftingmod.maplebot.model.UserModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {

    private Gson g = new GsonBuilder().create();

    private BroadcastReceiver r;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button start = (Button) findViewById(R.id.btn);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setComponent(new ComponentName("com.craftingmod.maplebot.launcher","com.craftingmod.maplebot.launcher.LaunchService"));
                //startService(i);
            }
        });
        findViewById(R.id.sync).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setComponent(new ComponentName("com.craftingmod.maplebot.launcher","com.craftingmod.maplebot.launcher.SyncService"));
                //startService(i);
            }
        });
        ArrayList<CoreUserModel> models = new ArrayList<>();
        models.add(new CoreUserModel(99007258,91263605));
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<String> mong = new ArrayList<String>();

                for(int i=1;i<13;i+=1){
                    ArrayList<SimpleUserModel> ar = MapleUtil.getCharacterList(11082628,i);
                    for(int k=0;k<ar.size();k+=1){
                        mong.add(ar.get(k).userName);
                    }
                }
                Log.d("Get",g.toJson(mong));
                //ArrayList<SimpleUserModel> ar = MapleUtil.getCharacterList(99007258,3);
                //Log.d("Get",g.toJson(ar));
                //ArrayList<FriendModel> arF = MapleUtil.getAccountFriendList(99007258);
                //Log.d("Get",g.toJson(arF));
            }
        }).start();
        r = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Type type = new TypeToken<ArrayList<OnlineModel>>(){}.getType();
                ArrayList<OnlineModel> models = g.fromJson(intent.getStringExtra("data"),type);
                Log.d("Get",g.toJson(models));
            }
        };
        //this.registerReceiver(r,new IntentFilter("broadcast.maple.recOnline"));
        //sendBroadcast(BroadUtil.buildN_reqOnline(g,models));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
       // this.unregisterReceiver(r);
    }
}
