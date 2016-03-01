package com.craftingmod.maplebot.charsearch;

import android.app.Activity;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.craftingmod.maplebot.CharSearch;
import com.craftingmod.maplebot.communicate.BaseSocket;
import com.craftingmod.maplebot.model.UserModel;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import android.os.Handler;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by superuser on 16/2/14.
 */
public class SearchService extends Service {

    private WebView webView;
    private ResourceClient Rclient;

    private Timer timer;

    private Handler handler;

    private Gson g;
    private boolean running;
    private String name;

    private BaseSocket server;

    private ArrayList<String> waits;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        webView = new WebView(this);
        Rclient = new ResourceClient(webView,this);
        waits = new ArrayList<>();
        g = new GsonBuilder().create();
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(Rclient);

        handler = new Handler(this.getMainLooper());
        timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(!running && waits.size() >= 1){
                    SearchService.this.run();
                }
            }
        },0,500);

        server = new BaseSocket(this.getBaseContext()) {
            @Override
            protected String getName() {
                return "charaName";
            }

            @Override
            protected Intent onStatus(Intent base, int id) {
                base.putExtra("status",(running)?CharSearch.BUSY:CharSearch.IDLE);
                return base;
            }

            @Override
            protected void onRequest(String input, String id) {
                Intent intent = new Intent(server.RECEIVE);
                String str = input;
                if(!str.equalsIgnoreCase("")){
                    for(String wait : waits){
                        if(wait.equalsIgnoreCase(str)){
                            return;
                        }
                    }
                    waits.add(str);
                    run();
                }
            }
        };
        server.registerBroadcast(this.getBaseContext());

        running = false;

        startForeground();
        //receiver.onCreate();
        return super.onStartCommand(intent, flags, startId);
    }
    private void startForeground(){
        /**
         * Foreground notification
         */
        Notification.Builder mBuilder =
                new Notification.Builder(this)
                        .setSmallIcon(R.drawable.ic)
                        .setContentTitle("<CharFinder> Service")
                        .setContentText("It is running!");
        startForeground(5782, mBuilder.build());
    }
    private void run(){
        if(waits.size() >= 1 && !running){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    running = true;
                    name = waits.get(0);
                    waits.remove(0);
                    Rclient.request(name);
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        server.onDestroy();
    }
    public void onReceiveID(UserModel model){
        Intent out = new Intent(server.RECEIVE);
        out.putExtra("id",name);
        if(model != null){
            out.putExtra("data",g.toJson(model));
        }
        sendBroadcast(out);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class ResourceClient extends  WebViewClient {

        private WebResourceResponse blank;
        private ArrayList<String> ignoreList;
        private ArrayList<String> whiteList;
        private String js;
        private String jsGrep;
        private WebView view;
        private String _name;
        private Context context;
        private boolean _request;
        private boolean _receive;
        private boolean _loaded;
        private boolean _hasLoaded;
        private SearchService __thiz;

        public ResourceClient(WebView v,SearchService th) {
            view = v;
            context = th;
            blank = new WebResourceResponse("text/html", "UTF-8", new ByteArrayInputStream("<div></div>".getBytes(Charsets.UTF_8)));
            ignoreList = new ArrayList<>();
            whiteList = new ArrayList<>();
            ignoreList.add("jpg");
            ignoreList.add("png");
            ignoreList.add("gif");
            ignoreList.add("css");
            ignoreList.add("menu.js");
            ignoreList.add("banner_effect2.js");
            ignoreList.add("GnxFlashActivate.js");
            ignoreList.add("GnxAdBanner.js");
            ignoreList.add("GnxUserMapleMenu.js");
            ignoreList.add("GnxUserGameLaunch.js");
            ignoreList.add("FlashLink.js");

            whiteList.add("http://s.nx.com/s2/game/maplestory/maple2007/image/etc/noimage_96_96.gif");
            js = "";
            jsGrep = "";
            _request = _receive = _loaded = _hasLoaded = false;
            _name = "";
            __thiz = th;
            js = getRaw(R.raw.search);
            jsGrep = getRaw(R.raw.grep);
        }
        public void request(String __name){
            _name = __name;
            _request = true;
            _receive = false;
            webView.loadUrl("http://rank.maplestory.nexon.com/MapleStory/Page/Gnx.aspx?URL=Ranking/TotalRanking", null);
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
            if(_receive && url.startsWith("http://rank.maplestory.nexon.com/MapleStory/Page/Gnx.aspx?URL=Ranking")){
                _loaded = true;
            }
            //Log.d("Char","LoadR : " + url);
        }

        @Override
        public void onPageFinished(final WebView view, String url) {
            super.onPageFinished(view, url);
            Log.d("Char", "Load Finished:" + url);
            if(url.equalsIgnoreCase("file:///android_asset/blank.html")){
                __thiz.running = false;
                _hasLoaded = false;
                return;
            }
            if(url.equalsIgnoreCase("http://rank.maplestory.nexon.com/MapleStory/Page/Gnx.aspx?URL=Ranking/TotalRanking")
                    ||url.equalsIgnoreCase("http://rank.maplestory.nexon.com/MapleStory/Page/Gnx.aspx?URL=Ranking%2fTotalRanking")){
                if(_request){
                    _request = false;
                    _receive = true;
                    view.stopLoading();
                    view.evaluateJavascript((js + "").replace("%nick", _name), null);
                }else if(_receive && _loaded){
                    _loaded = false;
                    _request = false;
                    _receive = false;
                    view.evaluateJavascript((jsGrep + "").replace("%nick", _name), new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
                            Log.d("OUT",g.toJson(value));
                            if(value == null || value.equalsIgnoreCase("null") || value.equalsIgnoreCase("\"null\"")){
                                __thiz.onReceiveID(null);
                            }else{
                                Log.d("OUT",value);
                                value = value.replace("\"","");
                                UserModel out;
                                ArrayList<String> result = Lists.newArrayList(Splitter.on(",").trimResults().omitEmptyStrings().split(value));
                                out = new UserModel(Integer.parseInt(result.get(0)),Integer.parseInt(result.get(1)), result.get(2));
                                out.worldID = Integer.parseInt(result.get(3));
                                __thiz.onReceiveID(out);
                            }
                            view.loadUrl("file:///android_asset/blank.html");
                        }
                    });
                }
            }
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            // Log.d("ShouldInterceptRequest",request.getUrl().toString());
            boolean load = true;
            String uri = request.getUrl().toString();
            if(_receive){
                if(uri.startsWith("http://rank.maplestory.nexon.com/MapleStory/Page/Gnx.aspx?URL=Ranking") && !_hasLoaded){
                    load = true;
                    _hasLoaded = true;
                }else{
                    load = false;
                }
            }else if(!uri.startsWith("http://rank.maplestory.nexon.com")){
                load = false;
            }
            for(String black : ignoreList){
                if(uri.endsWith(black)){
                    load = false;
                    break;
                }
            }
            for(String white : whiteList){
                if(uri.equalsIgnoreCase(white)){
                    load = true;
                    break;
                }
            }
            if(load){
                Log.d("ShouldInterceptRequest","Load URL: " + request.getUrl().toString());
                return super.shouldInterceptRequest(view, request);
            }else{
                return blank;
            }
        }
    }
    private String getRaw(int id){
        try {
            Resources res = getResources();
            InputStream in_s = res.openRawResource(id);

            byte[] b = new byte[in_s.available()];
            in_s.read(b);
            return new String(b);
        } catch (Exception e) {
            // e.printStackTrace();
            return null;
        }
    }
}
