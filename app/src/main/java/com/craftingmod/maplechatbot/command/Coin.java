package com.craftingmod.maplechatbot.command;

import android.content.Context;
import android.support.annotation.Nullable;

import com.craftingmod.maplechatbot.chat.ISender;
import com.craftingmod.maplechatbot.model.ChatModel;
import com.craftingmod.maplechatbot.model.UserModel;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by superuser on 16/1/21.
 */
public class Coin extends BaseCommand {
    public Coin(ISender sd) {
        super(sd);
    }
    @Override
    protected String[] filter(){
        return new String[]{"coin","동전"};
    }
    @Override
    protected void onCommand(ChatModel chat, UserModel user, String cmdName, @Nullable ArrayList<String> args) {
        String hashMSG = toSHA256(this.grap(args,0));
        Calendar rightNow = Calendar.getInstance();
        int[] field = new int[]{Calendar.YEAR,Calendar.DAY_OF_YEAR,Calendar.DAY_OF_MONTH,Calendar.DAY_OF_WEEK_IN_MONTH,Calendar.DAY_OF_WEEK};
        StringBuilder time = new StringBuilder();
        for(int i=0;i<field.length;i+=1){
            time.append(rightNow.get(field[i]));
        }
        String hashTime = toSHA256(time.toString());
        Long rnd = 0L;
        for(int i=0;i<hashMSG.length();i+=1){
            rnd += Integer.parseInt(hashMSG.substring(i,i+1).toUpperCase(),16);
        }
        for(int i=0;i<hashTime.length();i+=1){
            rnd += Integer.parseInt(hashTime.substring(i,i+1).toUpperCase(),16);
        }
        String out = this.grap(args,0) + " : ";
        if(rnd%20 == 6 || rnd%20 == 7) {
            this.sendMessageAll(out+"동전이 세로로 섰다!");
        }else if(rnd % 2 == 1){
            this.sendMessageAll(out+"네");
        }else{
            this.sendMessageAll(out+"아니오");
        }

    }
    public String toSHA256(String str){
        String SHA;
        try{
            MessageDigest sh = MessageDigest.getInstance("SHA-256");
            sh.update(str.getBytes());
            byte byteData[] = sh.digest();
            StringBuffer sb = new StringBuffer();
            for(int i = 0 ; i < byteData.length ; i++){
                sb.append(Integer.toString((byteData[i]&0xff) + 0x100, 16).substring(1));
            }
            SHA = sb.toString();
        }catch(NoSuchAlgorithmException e){
            e.printStackTrace();
            SHA = null;
        }
        return SHA;
    }
}
