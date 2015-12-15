package com.craftingmod.maplechatbot;

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
import android.widget.TextView;

import com.anprosit.android.promise.NextTask;
import com.anprosit.android.promise.Promise;
import com.anprosit.android.promise.Task;
import com.craftingmod.maplechatbot.chat.CharacterFinder;
import com.craftingmod.maplechatbot.chat.CharacterLoader;
import com.craftingmod.maplechatbot.chat.ChatService;
import com.craftingmod.maplechatbot.model.UserModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        final TextView tv = (TextView) findViewById(R.id.info);

        Intent intent = new Intent(this, ChatService.class);
        startService(intent);
        Log.d("WBUS", System.currentTimeMillis() + "");
        Promise.with(this,Integer.class)
                .then(new Task<Integer, UserModel>() {
                    @Override
                    public void run(Integer integer, NextTask<UserModel> task) {
                        Log.d("WBUS", "Hello!");
                        CharacterLoader cl = new CharacterLoader(task);
                        cl.execute(new UserModel(94141462,83364669,null));
                        //cl.handle(msg);
                    }
                }).thenOnMainThread(new Task<UserModel, Boolean>() {
            @Override
            public void run(UserModel userModel, NextTask<Boolean> nextTask) {
                tv.setText(new GsonBuilder().create().toJson(userModel));
            }
        }).create().execute(0);
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
