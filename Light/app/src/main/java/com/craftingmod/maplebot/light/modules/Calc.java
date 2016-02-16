package com.craftingmod.maplebot.light.modules;

import com.craftingmod.maplebot.light.IService;
import com.craftingmod.maplebot.model.ChatModel;
import com.udojava.evalex.Expression;

import java.util.ArrayList;

/**
 * Created by superuser on 16/2/13.
 */
public class Calc extends CoreModule {
    public Calc(IService sender) {
        super(sender);
    }

    @Override
    protected String onCommand(ChatModel chat, String cmd, ArrayList<String> args) {
        String out = "문법 오류에요";
        try{
            Expression exp = new Expression(this.grap(args,0));
            out = exp.eval().toPlainString();
        }catch (Exception ignored){}
        return out;
    }

    @Override
    public String getName() {
        return "Calculator";
    }

    @Override
    public String[] help() {
        return new String[]{"!calc <수식> : 간단한 수식을 계산합니다. 잘 안되지만."};
    }

    @Override
    protected String[] filter() {
        return new String[]{"calc"};
    }
}
