package com.craftingmod.maplechatbot.command;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;

import com.craftingmod.maplechatbot.model.ChatModel;
import com.craftingmod.maplechatbot.model.UserModel;

import java.util.ArrayList;

/**
 * Created by superuser on 15/12/13.
 */
public class Lotto extends BaseCommand {
    public Lotto(Context ct) {
        super(ct);
    }

    @Override
    protected String[] filter(){
        return new String[]{"스타포스","starEnchant","주사위","dice"};
    }

    @Override
    protected void onCommand(ChatModel chat,UserModel user, String cmdName, @Nullable ArrayList<String> args) {
        if(cmdName.equals("스타포스") || cmdName.equals("starEnchant")){
            if(args.size() >= 2 && isInteger(args.get(0)) && isInteger(args.get(1))){
                int start = Integer.parseInt(args.get(0));
                int end = Integer.parseInt(args.get(1));
                new StarForce().execute(start,end,user.accountID);
            }
        }
        if(cmdName.equals("주사위") || cmdName.equals("dice")){
            sendMessage("" + (1 + Math.floor(Math.random() * 6)),user.accountID);
        }
    }

    private class StarForce extends AsyncTask<Integer,Void,Long[]> {
        private int nowStar;
        private long nowMoney;
        private int destStar;
        private int boomed;
        private int falled;
        private int chance_fall;

        private int accID;

        private int start;

        private byte[] percent_sucess = {100, 95,90,85,85,80, 75,70,65,60,55, 45,35,30,30,30, 30,30,30,30,30, 30,30,3,2,1};
        private byte[] percent_fall = {0, 5,10,15,15,20, 25,30,35,40,45, 55,65,69,69,68, 68,68,68,67,67, 63,63,78,69,59};
        private byte[] percent_doom = {0, 0,0,0,0,0, 0,0,0,0,0, 0,0,1,1,2, 2,2,2,3,3, 7,7,19,29,40};

        private int[] money_150 = {0,
                136000,271000,406000,541000,676000,
                811000,946000,1081000,1216000,1351000,
                5470350,6918850,8587900,10490100,12637950,
                30087200,35437900,41351400,47850600,54952200,
                62696400,71087200,80152000,89912300,100389000};

        // start,end
        @Override
        protected Long[] doInBackground(Integer... params) {
            destStar = Math.min(25, params[1]);
            start = Math.min(destStar,Math.max(0, params[0]));

            nowStar = start;
            nowMoney = 0;
            boomed = 0;
            falled = 0;
            chance_fall = 0;

            accID = params[2];

            while(enchant());
            return new Long[]{nowMoney,(long)falled,(long)boomed};
        }
        @Override
        protected void onPostExecute(Long[] result){
            StringBuilder money = new StringBuilder();
            if(nowMoney >= 100000000){
                money.append(Math.floor(nowMoney/100000000)).append("억 ");
                nowMoney = nowMoney % 100000000;
            }
            if(nowMoney >= 10000){
                money.append(Math.floor(nowMoney/10000)).append("만 ");
                nowMoney = nowMoney % 10000;
            }
            money.append(nowMoney).append("메소");

            sendMessage("스타포스 " + start + "강부터 " + destStar + "강까지 " + money.toString()
                    +"가 들었고, 실패횟수는 " + falled + "번이며 터진횟수는 " + boomed + "번입니다.",accID);
            super.onPostExecute(result);
        }
        private boolean enchant(){
            nowMoney += money_150[nowStar+1];
            double rand = Math.random();
            if(rand*100 >= percent_sucess[nowStar+1]+4.5 && chance_fall < 2){
                if(100 - rand*100 <= percent_fall[nowStar+1]){
                    falled += 1;
                    if(nowStar > 5 && nowStar%5 != 0){
                        chance_fall += 1;
                        nowStar -= 1;
                    }
                }else{
                    boomed += 1;
                }
            }else{
                chance_fall = 0;
                nowStar += 1;
            }
            if(nowStar >= destStar){
                return false;
            }else{
                return true;
            }
        }
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
