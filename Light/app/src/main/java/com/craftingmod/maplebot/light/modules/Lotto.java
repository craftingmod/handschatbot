package com.craftingmod.maplebot.light.modules;

import com.craftingmod.maplebot.light.IService;
import com.craftingmod.maplebot.model.ChatModel;

import java.util.ArrayList;

/**
 * Created by superuser on 16/2/14.
 */
public class Lotto extends CoreModule {
    public Lotto(IService sender) {
        super(sender);
    }

    @Override
    protected String onCommand(ChatModel chat, String cmd, ArrayList<String> args) {
        int percent = 10;
        int tri = 5;
        if(cmd.equalsIgnoreCase("주흔") || cmd.equalsIgnoreCase("주흔피버") || cmd.equalsIgnoreCase("완작") || cmd.equalsIgnoreCase("완작피버")){
            if(args.size() != 2 || !isInteger(args.get(0)) || !isInteger(args.get(1))){
                return cmd + " <15,30,70,100%> <갯수>";
            }
            percent  = Integer.parseInt(args.get(0));
            tri = Math.max(1, Math.min(Integer.parseInt(args.get(1)), 30000));
            if(percent != 30 && percent != 70 && percent != 15 && percent != 100){
                return "잘못된 %";
            }
            if(cmd.equalsIgnoreCase("주흔피버") || cmd.equalsIgnoreCase("완작피버")){
                if(percent == 15) percent = 25;
                if(percent == 30) percent = 45;
                if(percent == 70) percent = 95;
            }
            percent += 10; // 4 + 6
        }else if(cmd.equalsIgnoreCase("순백")){
            if(args.size() != 1 || !isInteger(args.get(0))){
                return cmd + " <살릴 업글횟수>";
            }
            tri = Math.max(1,Math.min(Integer.parseInt(args.get(0)),11));
            percent = 10;
        }else{
            return "알수없는 오류";
        }
        int success = 0;
        int fail = 0;
        int i = 0;
        if(cmd.equalsIgnoreCase("주흔") || cmd.equalsIgnoreCase("주흔피버")){
            while(i < tri){
                if(Math.random()*100 < percent){
                    success += 1;
                }else{
                    fail += 1;
                }
                i += 1;
            }
            return args.get(0) + "% 주문서가 " + success + "번 성공하고 " + fail + "번 사르르~";
        }
        if(cmd.equalsIgnoreCase("순백")){
            while(i < tri){
                if(Math.random()*100 < percent){
                    success += 1;
                    i += 1;
                }else{
                    fail += 1;
                }
            }
            return "업그레이드 횟수 " + tri + "번 살리는데 순백이" + fail + "개 사라졌어요.";
        }
        if(cmd.equalsIgnoreCase("완작") || cmd.equalsIgnoreCase("완작피버")){
            while(i < tri){
                if(Math.random()*100 < percent){
                    success += 1;
                }else{
                    while(Math.random()*100 > 10){
                        fail += 1;
                    }
                    i -= 1;
                }
                i += 1;
            }
            return args.get(0) + "% 주문서 " + success + "작에 순백이 " + fail + "개 사르르~";
        }
        return "알수없는 오류";
    }

    @Override
    public String getName() {
        return "Lotto";
    }

    @Override
    public String[] help() {
        return new String[]{"!완작[피버] <15,30,70,100> <횟수> : 횟수만큼 주흔을 바르면 얼마나 순백이 들까요",
                "!주흔[피버] <15,30,70,100> <횟수> : 뽑기의 재미를 모의로 느껴봐요!",
                "!순백 <살려야 하는 업그레이드 횟수> : 순백의 재미를 느껴봐요!"};
    }

    @Override
    protected String[] filter() {
        return new String[]{"완작","주흔","주흔피버","순백","완작피버"};
    }

    public static boolean isInteger(String str) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        if (length == 0) {
            return false;
        }
        int i = 0;
        if (str.charAt(0) == '-') {
            if (length == 1) {
                return false;
            }
            i = 1;
        }
        for (; i < length; i++) {
            char c = str.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}
