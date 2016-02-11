package com.craftingmod.maplechatbot.command;

import android.content.res.Resources;
import android.support.annotation.Nullable;

import com.craftingmod.maplechatbot.R;
import com.craftingmod.maplechatbot.chat.ISender;
import com.craftingmod.maplechatbot.model.ChatModel;
import com.craftingmod.maplechatbot.model.MailModel;
import com.craftingmod.maplechatbot.model.OXModel;
import com.craftingmod.maplechatbot.model.UserModel;
import com.google.common.base.Joiner;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by superuser on 16/2/9.
 */
public class OXQuiz extends BaseCommand {
    private ArrayList<OXModel> ox;

    public OXQuiz(ISender sender) {
        super(sender);
        try {
            Resources res = context.getResources();
            InputStream in_s = res.openRawResource(R.raw.out);
            String json = IOUtils.toString(in_s);
            Type type = new TypeToken<ArrayList<OXModel>>(){}.getType();
            ox = g.fromJson(json,type);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String[] filter() {
        return new String[]{"OX"};
    }

    @Override
    protected void onCommand(ChatModel chat, UserModel user, String cmdName, @Nullable ArrayList<String> args) {
        if(args.size() <= 0){
            return;
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
            sendMessage(Joiner.on("   #   ").join(out),user.accountID);
        }else{
            sendMessage("검색 결과 없음.",user.accountID);
        }
    }
}
