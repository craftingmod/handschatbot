package com.craftingmod.maplechatbot.chat;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.*;
import android.util.Log;
import android.widget.TextView;

import com.anprosit.android.promise.NextTask;
import com.craftingmod.maplechatbot.MyApplication;
import com.craftingmod.maplechatbot.model.ChatModel;
import com.craftingmod.maplechatbot.model.UserModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import de.robv.android.xposed.XposedBridge;
import eu.chainfire.libsuperuser.Shell;

/**
 * Created by superuser on 15/12/12.
 */
public class CharacterLoader extends AsyncTask<UserModel,String,UserModel> {
    public final int CHARACTER_INFO = 3;
    protected NextTask<UserModel> task;
    protected DBManager db;
    public CharacterLoader(NextTask<UserModel> tsk){
        super();
        task = tsk;
        /*
        Shell.SU.run(new String[]{"mkdir /data/data/com.craftingmod.maplechatbot/databases",
                "cp -R /data/data/com.Nexon.MonsterLifeChat/databases/maplehands.db /data/data/com.craftingmod.maplechatbot/databases/maple.db",
                "chmod 777 /data/data/com.craftingmod.maplechatbot/databases/maple.db"}); */
    }
    /*
    private void log(String str){
        if(info != null){
            info.append(str + "\n");
        }
    }
    public void handle(Message msg){
        Log.v("Welcome,Handle","Message!");
        if(msg.what == CHARACTER_INFO){
            int accountID = msg.arg1;
            int characterID = msg.arg2;
            Cursor data = new sub(accountID,characterID).getData();
            UserModel uModel;
            if(data.moveToFirst()){
                try{
                    do {
                        uModel = new UserModel(accountID,characterID,data.getString(data.getColumnIndex("name")));
                        uModel.Exp = data.getLong(data.getColumnIndex("exp"));
                        uModel.gender = (byte) data.getInt(data.getColumnIndex("gender"));
                        uModel.level = (byte) data.getInt(data.getColumnIndex("level"));
                        uModel.jobRank = data.getInt(data.getColumnIndex("jobrank"));
                        uModel.pop = data.getInt(data.getColumnIndex("pop"));
                        uModel.popRank = data.getInt(data.getColumnIndex("poprank"));
                        uModel.totalRank = data.getInt(data.getColumnIndex("totalrank"));
                        uModel.worldID = data.getInt(data.getColumnIndex("wid"));
                        uModel.worldName = data.getString(data.getColumnIndex("wname"));
                        uModel.worldRank = data.getInt(data.getColumnIndex("worldrank"));
                        if(data.isNull(data.getColumnIndex("jobdetail"))){
                            if(data.isNull(data.getColumnIndex("jobname"))){
                                uModel.job = "없음";
                            }else{
                                uModel.job = data.getString(data.getColumnIndex("jobname"));
                            }
                        }else{
                            uModel.job = data.getString(data.getColumnIndex("jobdetail"));
                        }
                        Log.d("MapleUsermodel", new GsonBuilder().create().toJson(uModel));
                    }while(data.moveToNext());
                    task.run(uModel);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
    public Cursor execSQL(String sql) {
        return db.rawQuery(sql, null);
        //result.
    }
    */

    @Override
    protected UserModel doInBackground(UserModel... params) {
        String cmd = "sqlite3 /data/data/com.Nexon.MonsterLifeChat/databases/maplehands.db \"SELECT * FROM character_table_info WHERE aid=\'"
                + params[0].accountID + "\' AND cid=\'" + params[0].characterID + "\'\"";
        List<String> suList = Shell.SU.run(cmd);
        String suResult;
        if(suList == null || suList.size() < 1){
            task.fail(null,new IndexOutOfBoundsException("Caused by: java.lang.IndexOutOfBoundsException: Invalid index 0, size is 0"));
            return null;
        }else{
            suResult = suList.get(0);
        }
        String[] piece = suResult.split("\\|");

        UserModel uModel;
        uModel = new UserModel(params[0].accountID,params[0].characterID,piece[12]);
        uModel.Exp = Long.parseLong(piece[11]);
        uModel.gender = Byte.parseByte(piece[3]);
        uModel.level = Short.parseShort(piece[4]);
        uModel.jobRank = Integer.parseInt(piece[9]);
        uModel.pop = Integer.parseInt(piece[6]);
        uModel.popRank = Integer.parseInt(piece[10]);
        uModel.totalRank = Integer.parseInt(piece[7]);
        uModel.worldID = Integer.parseInt(piece[2]);
        uModel.worldName = piece[14];
        uModel.worldRank = Integer.parseInt(piece[8]);
        uModel.userImage = piece[19];
        if(piece[16].length() < 1){
            if(piece[15].length() < 1){
                uModel.job = "없음";
            }else{
                uModel.job = piece[15];
            }
        }else{
            uModel.job = piece[16];
        }
        Log.d("MapleUsermodel", new GsonBuilder().create().toJson(uModel));
        if(task != null) task.run(uModel);
        return uModel;
    }
    @Override
    protected void onPreExecute() {

    }
    @Override
    protected void onProgressUpdate(String... values) {

    }

    private class sub {
        private int accountID;
        private int characterID;
        private Cursor cs;
        public sub(int acid,int chid){
            accountID = acid;
            characterID = chid;
        }
        private boolean hasMultiple(String type){
           // cs = execSQL(getSQLSelect(type));
            return cs.getCount() >= 2;
        }
        private String getSQLSelect(String selectN){
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT ");
            sb.append(selectN);
            sb.append(" FROM character_table_info WHERE aid=\'");
            sb.append(accountID);
            sb.append("\' AND cid=\'");
            sb.append(characterID);
            sb.append("\'");
            return sb.toString();
        }
        public Cursor getData(){
            /*
            if(hasMultiple("*")){
                XposedBridge.log("Multiple Accounts!");
            }else{
                cs = execSQL(getSQLSelect("*"));
            }
            */
            return null;
           // return execSQL(getSQLSelect("*"));
        }
    }
}
