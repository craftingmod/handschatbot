package com.craftingmod.maplechatbot.command;

import android.support.annotation.Nullable;

import com.craftingmod.maplechatbot.chat.ISender;
import com.craftingmod.maplechatbot.model.ChatModel;
import com.craftingmod.maplechatbot.model.UserModel;
import com.udojava.evalex.Expression;

import java.util.ArrayList;


/**
 * Created by superuser on 16/1/21.
 */
public class Cmath extends BaseCommand {

    public Cmath(ISender sender) {
        super(sender);
    }
    protected String[] filter(){
        return new String[]{"math"};
    }
    @Override
    protected void onCommand(ChatModel chat, UserModel user, String cmdName, @Nullable ArrayList<String> args) {
        // x=3;y=2 x^2+2x+3=0
        try{
            Expression exp = new Expression(this.grap(args,0));
            this.sendMessage("> " + exp.eval(),user.accountID);
        }catch (Exception e){
            this.sendMessage("문법 오류에영",user.accountID);
        }
    }
}
