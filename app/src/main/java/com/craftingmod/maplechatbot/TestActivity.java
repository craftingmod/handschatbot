package com.craftingmod.maplechatbot;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebResourceResponse;

import com.google.common.base.Charsets;
import com.google.common.io.ByteSource;
import com.google.common.io.CharStreams;

import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkView;
import org.xwalk.core.XWalkWebResourceRequest;
import org.xwalk.core.XWalkWebResourceResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;

/**
 * Created by superuser on 16/2/13.
 */
public class TestActivity extends AppCompatActivity {

    private static final String TAG = "MapleTest";

    private XWalkView webview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);
        webview = (XWalkView) findViewById(R.id.webview);
        webview.setResourceClient(new ResourceClient(webview));
        XWalkPreferences.setValue(XWalkPreferences.REMOTE_DEBUGGING,true);
        webview.load("http://rank.maplestory.nexon.com/MapleStory/Page/Gnx.aspx?URL=Ranking/TotalRanking",null);
    }
    private class ResourceClient extends XWalkResourceClient {

        private XWalkWebResourceResponse blank;
        private ArrayList<String> ignoreList;
        private ArrayList<String> whiteList;

        public ResourceClient(XWalkView view) {
            super(view);
            blank = createXWalkWebResourceResponse("text/html", "UTF-8", new ByteArrayInputStream("<div></div>".getBytes(Charsets.UTF_8)));
            ignoreList = new ArrayList<>();
            whiteList = new ArrayList<>();
            ignoreList.add("*.jpg");
            ignoreList.add("*.png");
            ignoreList.add("*.gif");
            ignoreList.add("*.css");
            whiteList.add("http://s.nx.com/s2/game/maplestory/maple2007/image/etc/noimage_96_96.gif");
        }

        public void onLoadStarted(XWalkView view, String url) {
            super.onLoadStarted(view, url);
            Log.d(TAG, "Load Started:" + url);
        }

        public void onLoadFinished(XWalkView view, String url) {
            super.onLoadFinished(view, url);
            Log.d(TAG, "Load Finished:" + url);
        }

        public void onProgressChanged(XWalkView view, int progressInPercent) {
            super.onProgressChanged(view, progressInPercent);
            Log.d(TAG, "Loading Progress:" + progressInPercent);
        }

        @Override
        public XWalkWebResourceResponse shouldInterceptLoadRequest(XWalkView view, XWalkWebResourceRequest request) {
            boolean load = true;
            String uri = request.getUrl().toString();
            for(String white : whiteList){
                if(uri.equalsIgnoreCase(white)){
                    load = true;
                }
            }
            for(String black : ignoreList){
                if(!uri.contains("*") && uri.equalsIgnoreCase(black)){
                    load = false;
                }else if(uri.contains("*") && uri.endsWith(black.substring(black.lastIndexOf("*")+1))){
                    load = false;
                }
            }
            if(load){
                return super.shouldInterceptLoadRequest(view,request);
            }else{
                return blank;
            }
        }
    }
}
