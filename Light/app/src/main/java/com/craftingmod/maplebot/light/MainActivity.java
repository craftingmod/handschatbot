package com.craftingmod.maplebot.light;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.craftingmod.maplebot.BroadUtil;
import com.craftingmod.maplebot.R;
import com.craftingmod.maplebot.heavy.HeavyService;
import com.craftingmod.maplebot.model.CoreUserModel;
import com.craftingmod.maplebot.model.OnlineModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Gson g = new GsonBuilder().create();

    private BroadcastReceiver r;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startService(new Intent(this,LightService.class));
        startService(new Intent(this,HeavyService.class));

        /*
        ArrayList<CoreUserModel> models = new ArrayList<>();
        models.add(new CoreUserModel(17731671,85724972));
        r = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Type type = new TypeToken<ArrayList<OnlineModel>>(){}.getType();
                ArrayList<OnlineModel> models = g.fromJson(intent.getStringExtra("data"),type);
                Log.d("Get",g.toJson(models));
            }
        };
        this.registerReceiver(r,new IntentFilter("broadcast.maple.recOnline"));
        sendBroadcast(BroadUtil.buildN_reqOnline(g,models));
        */
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
       // this.unregisterReceiver(r);
    }
}
