package com.craftingmod.maplechatbot.command;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.anprosit.android.promise.Callback;
import com.anprosit.android.promise.NextTask;
import com.anprosit.android.promise.Promise;
import com.anprosit.android.promise.Task;
import com.craftingmod.maplechatbot.chat.CharacterFinder;
import com.craftingmod.maplechatbot.model.MailModel;
import com.craftingmod.maplechatbot.model.UserModel;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by superuser on 15/12/13.
 */
public class Mail extends BaseCommand {

    private HashMap<Integer,Boolean> hasMail;
    private ArrayList<MailModel> mails;

    public Mail(Context ct) {
        super(ct);
        mails = new ArrayList<>();
        hasMail = new HashMap<>();
    }

    @Override
    protected void onCommand(final UserModel user, final String cmdName, @Nullable final ArrayList<String> args) {
        if(cmdName.equalsIgnoreCase("mail") || cmdName.equalsIgnoreCase("메일")){
            if(args.size() >= 2){
                final String sender = args.get(0);
                final long aid = user.accountID;
                Promise.with(this,Boolean.class)
                        .then(new Task<Boolean, ArrayList<UserModel>>() {
                            @Override
                            public void run(Boolean aBoolean, NextTask<ArrayList<UserModel>> task) {
                                new CharacterFinder(task).execute("name=\'" + sender + "\'");
                            }
                        }).then(new Task<ArrayList<UserModel>, Object>() {
                    @Override
                    public void run(ArrayList<UserModel> userModels, NextTask<Object> nextTask) {
                        addMail(userModels.get(0).accountID,user,grap(args, 1));
                    }
                }).setCallback(new Callback<Object>() {
                    @Override
                    public void onSuccess(Object o) {}
                    @Override
                    public void onFailure(Bundle bundle, Exception e) {
                        sendMessage("메일을 보내는데 실패하였습니다.");
                    }
                }).create().execute(true);
            /*
            if(!mails.containsKey(sender)){
                HashMap<String,String> hash = new HashMap<>();
                hash.put(user.userName,this.grap(args,1));
                mails.put(sender,hash);
            }else{
                mails.get(sender).put(user.userName, this.grap(args, 1));
            }
            this.sendMessage("메일 저장 완료.");
            */
            }
        }
    }
    private void addMail(int aid,UserModel user,String msg){
        hasMail.put(aid,true);
        mails.add(new MailModel(user.userName,aid,msg));
        sendMessage("AID " + aid + "로 메일을 보냈습니다.");
    }
    @Override
    public void onText(UserModel user, String msg){
        if(hasMail.get(user.accountID)){
            hasMail.put(user.accountID,false);
            StringBuilder sb = new StringBuilder();
            sb.append("Mail => ");
            for(i=0;i<mails.size();i+=1){
                if(mails.get(i).receiveAccountID == user.accountID){
                    sb.append(mails.get(i).sendNick).append(" : ").append(mails.get(i).message).append(" /  ");
                    mails.remove(i);
                }
            }
            sendMessage(sb.toString());
        }
    }

    @Override
    protected void onExit() {
        this.saveData("mails",g.toJson(mails));
    }
}
