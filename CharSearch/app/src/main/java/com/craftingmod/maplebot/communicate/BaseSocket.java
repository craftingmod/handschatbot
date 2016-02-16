package com.craftingmod.maplebot.communicate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.anprosit.android.promise.NextTask;
import com.craftingmod.maplebot.CharSearch;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by superuser on 16/2/14.
 */
public abstract class BaseSocket extends BroadcastReceiver {
    public static final ResultObject TIMEOUT = new ResultObject(CharSearch.TIMEOUT);
    public static final ResultObject IDLE = new ResultObject(CharSearch.IDLE);
    public static final ResultObject BUSY = new ResultObject(CharSearch.BUSY);

    public final String STATUS = "maplebot.communication." + getName() + ".status";
    public final String RECEIVE = "maplebot.communication." + getName() + ".send";
    public final String REQUEST = "maplebot.communication." + getName() + ".request";

    private final IntentFilter filter;

    private Timer timer;
    private HashMap<Integer,NextTask<ResultObject>> status;
    private HashMultimap<String,NextTask<ResultObject>> requests;
    private HashMap<Integer,StatusTimeout> timeoutS;
    private HashMultimap<String,StatusTimeout> timeoutR;
    private Context context;

    private String name = getName();


    public static BaseSocket buildClient(final Context context,final String name){
        BaseSocket base = new BaseSocket(context) {
            @Override
            protected String getName() {
                return name;
            }
            @Override
            protected Intent onStatus(Intent base, int id) {
                return null;
            }
            @Override
            protected void onRequest(String input, String id) {
            }
        };
        base.registerBroadcast(context);
        return base;
    }

    public BaseSocket(Context con){
        timer = new Timer();
        status = new HashMap<>();
        requests = HashMultimap.create();
        timeoutS = new HashMap<>();
        timeoutR = HashMultimap.create();
        filter = new IntentFilter();
        filter.addAction(STATUS);
        filter.addAction(REQUEST);
        filter.addAction(RECEIVE);
        context = con;
    }
    public void status(NextTask<ResultObject> task){
        Intent intent = new Intent(STATUS);
        int ID;
        do {
            ID = (int) Math.floor(Math.random() * Integer.MAX_VALUE);
        }while(status.containsKey(ID));
        status.put(ID,task);
        intent.putExtra("id",ID);
        final StatusTimeout timeout = new StatusTimeout(ID,task,timeoutS,status);
        timeoutS.put(ID,timeout);
        timer.schedule(timeout,1500);
        Log.d("BaseSocket","SendBroadcast");
        context.sendBroadcast(intent);
    }
    public void request(String id,String data,NextTask<ResultObject> task){
        Intent intent = new Intent(REQUEST);
        requests.put(id,task);
        intent.putExtra("id",id);
        intent.putExtra("data",data);
        final StatusTimeout timeout = new StatusTimeout(id,task,timeoutR,requests);
        timeoutR.put(id,timeout);
        timer.schedule(timeout,15000);
        context.sendBroadcast(intent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(!intent.hasExtra("id")){
            return;
        }
        if(action.equalsIgnoreCase(STATUS) && !intent.hasExtra("status")){
            Intent build = new Intent(STATUS);
            build.putExtra("id",intent.getIntExtra("id",-1));
            build = onStatus(build,intent.getIntExtra("id",-1));
            if(build != null){
                context.sendBroadcast(build);
            }
            return;
        }
        if(action.equalsIgnoreCase(STATUS) && intent.hasExtra("status")){
            int statusCode = intent.getIntExtra("status",CharSearch.TIMEOUT);
            int ID = intent.getIntExtra("id",-1);
            if(status.containsKey(ID)){
                final NextTask<ResultObject> task = status.get(ID);
                status.remove(ID);
                timeoutS.get(ID).cancel();
                timeoutS.remove(ID);
                task.run(new ResultObject(statusCode));
            }
            return;
        }
        if(action.equalsIgnoreCase(REQUEST)){
            Log.d("Extract",intent.toString());
            onRequest(intent.getStringExtra("data"),intent.getStringExtra("id"));
            return;
        }
        if(action.equalsIgnoreCase(RECEIVE)){
            int statusCode = intent.hasExtra("data") ? CharSearch.OK : CharSearch.NULL;
            String ID = intent.getStringExtra("id");
            ResultObject result = new ResultObject(statusCode);
            if(intent.hasExtra("data")){
                result.data = intent.getStringExtra("data");
            }
            if(requests.containsKey(ID)){
                for(TimerTask timerT : timeoutR.get(ID)){
                    timerT.cancel();
                }
                ArrayList<NextTask<ResultObject>> tasks = Lists.newArrayList(requests.get(ID));
                timeoutR.removeAll(ID);
                requests.removeAll(ID);
                for(NextTask<ResultObject> task : tasks){
                    task.run(result);
                }
                Log.d("RemoveAll","Removed.");
            }
            return;
        }
    }
    protected abstract String getName();
    protected abstract Intent onStatus(Intent base,int id);
    protected abstract void onRequest(String input,String id);

    public void registerBroadcast(Context c){
        if(context == null){
            context = c;
        }
        context.registerReceiver(this,filter);
    }
    public void onDestroy(){
        if(context != null){
            context.unregisterReceiver(this);
        }
    }

    private class StatusTimeout extends TimerTask {
        private final int _id;
        private final String _sid;
        private final NextTask<ResultObject> _task;
        private final HashMap<Integer,NextTask<ResultObject>> _obj1;
        private final HashMap<Integer,StatusTimeout> _obj2;
        private final HashMultimap<String,NextTask<ResultObject>> _obj3;
        private final HashMultimap<String,StatusTimeout> _obj4;

        public StatusTimeout(int __id, NextTask<ResultObject> __task,HashMap<Integer,StatusTimeout> aTm,HashMap<Integer,NextTask<ResultObject>> rst){
            _id = __id;
            _sid = null;
            _task = __task;
            _obj1 = rst;
            _obj2 = aTm;
            _obj3 = null;
            _obj4 = null;
        }
        public StatusTimeout(String __sid, NextTask<ResultObject> __task,HashMultimap<String,StatusTimeout> aTm,HashMultimap<String,NextTask<ResultObject>> rst){
            _sid = __sid;
            _id = -1;
            _task = __task;
            _obj1 = null;
            _obj2 = null;
            _obj3 = rst;
            _obj4 = aTm;
        }
        @Override
        public void run() {
            if(_sid == null){
                _obj1.remove(_id);
                _obj2.remove(_id);
            }else{
                if(_obj3 != null && _obj4 != null){
                    for(TimerTask objj : _obj4.get(_sid)){
                        if(objj != this){
                            objj.cancel();
                        }
                    }
                    _obj3.removeAll(_sid);
                    _obj4.removeAll(_sid);
                }
            }
            _task.run(TIMEOUT);
            this.cancel();
        }
    }
}
