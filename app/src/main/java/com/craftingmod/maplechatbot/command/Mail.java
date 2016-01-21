package com.craftingmod.maplechatbot.command;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.anprosit.android.promise.Callback;
import com.anprosit.android.promise.NextTask;
import com.anprosit.android.promise.Promise;
import com.anprosit.android.promise.Task;
import com.craftingmod.maplechatbot.chat.CharacterFinder;
import com.craftingmod.maplechatbot.chat.ISender;
import com.craftingmod.maplechatbot.model.ChatModel;
import com.craftingmod.maplechatbot.model.MailModel;
import com.craftingmod.maplechatbot.model.UserModel;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by superuser on 15/12/13.
 */
public class Mail extends BaseCommand {

    private HashMap<Integer,ArrayList<MailModel>> mails;

    public Mail(ISender sd) {
        super(sd);
        Type typeM = new TypeToken<ArrayList<MailModel>>(){}.getType();
        Type typeS = new TypeToken<HashMap<Integer,String>>(){}.getType();
        String data = this.getDataStr("mails");
        mails = new HashMap<>();
        if(data != null){
            HashMap<Integer,String> mapSave = g.fromJson(data,typeS);
            for (Map.Entry<Integer, String> entry : mapSave.entrySet()){
                mails.put(entry.getKey(), (ArrayList<MailModel>) g.fromJson(entry.getValue(),typeM));
                //System.out.println(entry.getKey() + "/" + entry.getValue());
            }
        }
    }
    @Override
    public void onSave() {
        HashMap<Integer,String> mailSave = new HashMap<>();
        Type typeM = new TypeToken<ArrayList<MailModel>>(){}.getType();
        Type typeS = new TypeToken<HashMap<Integer,String>>(){}.getType();
        for (Map.Entry<Integer, ArrayList<MailModel>> entry : mails.entrySet()){
            mailSave.put(entry.getKey(), g.toJson(entry.getValue(), typeM));
        }
        this.saveData("mails", g.toJson(mailSave,typeS));
    }
    @Override
    protected String[] filter(){
        return new String[]{"mail","메일"};
    }
    @Override
    protected void onEvent(ChatModel chat,UserModel user,String msg){
        if(mails.containsKey(user.accountID) && mails.get(user.accountID).size() >= 1){
            final ArrayList<MailModel> mMail = mails.get(user.accountID);
            for(int i=0;i<mMail.size();i+=1){
                final MailModel mail = mMail.get(i);
                sendMessage(mail.sendNick + " > " + mail.message ,user.accountID);
            }
            mails.remove(user.accountID);
        }
    }

    @Override
    protected void onCommand(final ChatModel chat,final UserModel user, final String cmdName, @Nullable final ArrayList<String> args) {
        if(args.size() >= 2) {
            final String sender = args.get(0);
            Promise.with(this, String.class)
                    .then(new Task<String, HashMap<String,String>>() {
                        @Override
                        public void run(String pm, NextTask<HashMap<String,String>> task) {
                            HashMap<String,String> map = new HashMap<>();
                            map.put("name",pm);
                            task.run(map);
                        }
                    }).then(new CharacterFinder()).then(new Task<ArrayList<UserModel>, Object>() {
                @Override
                public void run(ArrayList<UserModel> userModels, NextTask<Object> nextTask) {
                    addMail(userModels.get(0).accountID, user, grap(args, 1));
                    sendMessage(sender + "(" + userModels.get(0).accountID + ") 에게 메일을 보냈습니다.", chat.SenderAID);
                }
            }).setCallback(new Callback<Object>() {
                @Override
                public void onSuccess(Object o) {
                }
                @Override
                public void onFailure(Bundle bundle, Exception e) {
                    sendMessage("메일을 보내는데 실패하였습니다.",chat.SenderAID);
                }
            }).create().execute(sender);
        }
    }
    private void addMail(int aid,UserModel user,String msg){
        if(!mails.containsKey(aid)){
            mails.put(aid,new ArrayList<MailModel>());
        }
        mails.get(aid).add(new MailModel(user.userName, aid, msg));
    }
}
