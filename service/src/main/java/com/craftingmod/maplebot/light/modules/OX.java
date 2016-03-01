package com.craftingmod.maplebot.light.modules;

import android.content.res.Resources;
import android.text.TextUtils;

import com.craftingmod.maplebot.light.IService;
import com.craftingmod.maplebot.R;
import com.craftingmod.maplebot.model.ChatModel;
import com.craftingmod.maplebot.light.model.OXModel;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by superuser on 16/2/13.
 */
public class OX extends CoreModule {
    private ArrayList<OXModel> ox;

    public OX(IService sender) {
        super(sender);
        Resources res = context.getResources();
        InputStream in_s = res.openRawResource(R.raw.out);
        String json = convertStreamToString(in_s);
        Type type = new TypeToken<ArrayList<OXModel>>(){}.getType();
        ox = g.fromJson(json,type);
    }
    public String convertStreamToString(InputStream is){
        try(java.util.Scanner s = new java.util.Scanner(is)) { return s.useDelimiter("\\A").hasNext() ? s.next() : ""; }
    }

    @Override
    protected String onCommand(ChatModel chat, String cmd, ArrayList<String> args) {
        if(args.size() <= 0){
            return "검색할 것이 없음.";
        }
        ArrayList<String> out = new ArrayList<>();
        String search = grap(args,0);
        for(int i=0;i<ox.size();i+=1){
            if(ox.get(i).name.contains(search)){
                out.add(ox.get(i).name + ": " + ox.get(i).answer +" ");
            }
            if(out.size() >= 5){
                break;
            }
        }
        if(out.size() >= 1){
            return TextUtils.join("   #   ",out);
        }
        return "검색 결과 없음.";
    }

    @Override
    public String getName() {
        return "OXQuiz";
    }

    @Override
    public String[] help() {
        return new String[]{"!ox <검색 내용> : 검색 내용이 들어간 ox퀴즈 답을 5개까지 알려줘요"};
    }

    @Override
    protected String[] filter() {
        return new String[]{"ox","ㅈㅂ"};
    }
}
