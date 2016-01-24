package com.craftingmod.maplechatbot;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
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
import com.craftingmod.maplechatbot.model.UserModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import android.os.Handler;

import java.util.ArrayList;
import java.util.HashMap;

import eu.chainfire.libsuperuser.Shell;

public class MainActivity extends AppCompatActivity {

    private LinearLayout mainLayout;
    private TextView info;

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
                syncDB();
            }
        });
        Button testBt = (Button) findViewById(R.id.testB);
        testBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testDB();
            }
        });

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
    private void execService(){
        Intent intent = new Intent(this, ChatService.class);
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
