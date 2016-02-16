package com.craftingmod.maplebot.charsearch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.anprosit.android.promise.Callback;
import com.anprosit.android.promise.NextTask;
import com.anprosit.android.promise.OnYieldListener;
import com.anprosit.android.promise.Promise;
import com.anprosit.android.promise.Task;
import com.craftingmod.maplebot.CharSearch;
import com.craftingmod.maplebot.communicate.BaseSocket;
import com.craftingmod.maplebot.communicate.ResultObject;
import com.craftingmod.maplebot.model.UserModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import android.os.Handler;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private Button button;
    private Button runS;
    private TextView textView;
    private EditText input;
    private Gson g;
    private BaseSocket api;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        g = new GsonBuilder().create();
        button = (Button) findViewById(R.id.btn);
        api = BaseSocket.buildClient(this.getBaseContext(),"charaName");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Promise.with(MainActivity.this,null).then(new Task<Object, ResultObject>() {
                    @Override
                    public void run(Object o, NextTask<ResultObject> nextTask) {
                        api.status(nextTask);
                    }
                }).then(new Task<ResultObject, ResultObject>() {
                    @Override
                    public void run(ResultObject result, NextTask<ResultObject> nextTask) {
                        Log.d("Hello",result.getState());
                        String text = input.getText().toString();
                        if(result.status == CharSearch.IDLE){
                            api.request(text,text,nextTask);
                        }else{
                            nextTask.yield(0,null);
                        }
                    }
                }).then(new Task<ResultObject, String>() {
                    @Override
                    public void run(ResultObject resultObject, NextTask<String> nextTask) {
                        Log.d("Result", resultObject.getState());
                        if(resultObject.status == CharSearch.TIMEOUT){
                            nextTask.run("타임아웃");
                        }else if(resultObject.status == CharSearch.NULL){
                            nextTask.run("검색 결과 없음!");
                        }else if(resultObject.status == CharSearch.OK){
                            UserModel model = g.fromJson(resultObject.data,UserModel.class);
                            nextTask.run(model.userName + " : aID " + model.accountID + " cID " + model.characterID);
                        }
                    }
                }).setOnYieldListener(new OnYieldListener() {
                    @Override
                    public void onYield(int i, Bundle bundle) {
                        textView.append("\n타임아웃");
                    }
                }).setCallback(new Callback<String>() {
                    @Override
                    public void onSuccess(String s) {
                        textView.append("\n" + s);
                    }

                    @Override
                    public void onFailure(Bundle bundle, Exception e) {}
                }).create().execute(null);
                /*
                Intent i = new Intent("maplebot.charsearch.request");
                i.putExtra("name",input.getText().toString());
                sendBroadcast(i);
                */
            }
        });
        input = (EditText) findViewById(R.id.input);
        textView = (TextView) findViewById(R.id.textv);
        textView.setMovementMethod(new ScrollingMovementMethod());
        runS = (Button) findViewById(R.id.run);
        runS.setVisibility(View.GONE);
        Promise.with(this,Boolean.class).then(new Task<Boolean, ResultObject>() {
            @Override
            public void run(Boolean o, NextTask<ResultObject> nextTask) {
                Log.d("Hello","Send.");
                api.status(nextTask);
            }
        }).thenOnMainThread(new Task<ResultObject, Void>() {
            @Override
            public void run(ResultObject resultObject, NextTask<Void> nextTask) {
                if(resultObject.status == CharSearch.TIMEOUT){
                    Log.d("Hello","World");
                    runS.setText("검색 활성화");
                    setClickEnable();
                    runS.setVisibility(View.VISIBLE);
                }else{
                    runS.setText("이미 서비스가 실행중입니다");
                    runS.setEnabled(false);
                    runS.setClickable(false);
                    runS.setVisibility(View.VISIBLE);
                }
                nextTask.run(null);
            }
        }).create().execute(false);
        runS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(new Intent(MainActivity.this,SearchService.class));
                v.setVisibility(View.GONE);
                v.setOnClickListener(null);
            }
        });
        findViewById(R.id.clean).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText("");
            }
        });
        //startService(new Intent(this,SearchService.class));
    }
    private void setClickEnable(){
        runS.setClickable(true);
        runS.setEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        api.onDestroy();
    }
}
