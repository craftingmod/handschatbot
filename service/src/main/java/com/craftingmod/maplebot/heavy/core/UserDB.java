package com.craftingmod.maplebot.heavy.core;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import com.craftingmod.maplebot.Config;
import com.craftingmod.maplebot.model.UserModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by superuser on 16/2/4.
 */
public class UserDB {

    private static final String dbName = "users.db";
    public static final int dbVersion = 1;

    protected final String sqlDefault = "SELECT * FROM character_table_info WHERE $search;";

    // DB관련 객체 선언
    private OpenHelper opener; // DB opener
    private SQLiteDatabase db; // DB controller

    // 부가적인 객체들
    private Context context;

    public UserDB(Context ct){
        context = ct;
        this.opener = new OpenHelper(context, dbName, null, dbVersion);
        db = opener.getWritableDatabase();
    }

    public void execSQL(String sql){
        db.execSQL(sql);
    }
    public Cursor rawSQL(String sql){
        return db.rawQuery(sql, null);
    }
    public void putCache(UserModel model){
        execSQL(genSQL("users",model));
    }
    public ArrayList<UserModel> searchCache(HashMap<String,String> searches){
        StringBuilder sqlBuilder = new StringBuilder();
        int i =0;
        for (Map.Entry<String, String> entry : searches.entrySet()){
            sqlBuilder.append(entry.getKey() + "=\'" + entry.getValue() + "\'");
            if(i < searches.size()-1){
                sqlBuilder.append(" AND ");
            }
            i += 1;
        }
        String sql = (sqlDefault+"").replace("character_table_info","users").replace("$search", sqlBuilder.toString());
        Cursor cursor = db.rawQuery(sql, null);

        ArrayList<UserModel> out = new ArrayList<>();
        if(cursor.moveToFirst()){
            do {
                out.add(parse(cursor));
            } while (cursor.moveToNext());
        }
        return out;
    }
    public ArrayList<UserModel> searchUser(HashMap<String,String> searches){
        StringBuilder sqlBuilder = new StringBuilder();
        int i =0;
        for (Map.Entry<String, String> entry : searches.entrySet()){
            if(entry.getKey().equalsIgnoreCase("name")){
                sqlBuilder.append(entry.getKey() + "=\'" + entry.getValue().toLowerCase() + "\'");
            }else{
                sqlBuilder.append(entry.getKey() + "=\'" + entry.getValue() + "\'");
            }
            if(i < searches.size()-1){
                sqlBuilder.append(" AND ");
            }
            i += 1;
        }
        String sql = (sqlDefault+"").replace("$search", sqlBuilder.toString());
        Cursor cursor = db.rawQuery(sql, null);

        ArrayList<UserModel> out = new ArrayList<>();
        if(cursor.moveToFirst()){
            do {
                out.add(parse(cursor));
            } while (cursor.moveToNext());
        }
        return out;
    }
    public static UserModel parse(Cursor oCursor){
        UserModel uModel;
        uModel = new UserModel(oCursor.getInt(0),oCursor.getInt(1),oCursor.getString(12));
        uModel.Exp = oCursor.getLong(11);
        uModel.gender = (byte)oCursor.getInt(3);
        uModel.level = (short)oCursor.getInt(4);
        uModel.jobRank = oCursor.getInt(9);
        uModel.pop = oCursor.getInt(6);
        uModel.popRank = oCursor.getInt(10);
        uModel.totalRank = oCursor.getInt(7);
        uModel.worldID = oCursor.getInt(2);
        uModel.worldName = oCursor.getString(14);
        uModel.worldRank = oCursor.getInt(8);
        uModel.userImage = oCursor.getString(19);
        if(uModel.userImage.length() < 1){
            uModel.userImage = null;
        }
        uModel.job = oCursor.getString(15);
        if(oCursor.getString(16).length() < 1){
            if(uModel.job.length() < 1){
                uModel.job = "없음";
            }else{
                uModel.job = oCursor.getString(15);
            }
        }else{
            uModel.job = oCursor.getString(16);
        }
        return uModel;
    }
    private String genSQL(String table,UserModel model){
        StringBuilder build = new StringBuilder();
        build.append("INSERT INTO " + table + " VALUES (");
        build.append(model.accountID); //aid
        build.append(" , ");
        build.append(model.characterID); // cid
        build.append(" , ");
        build.append(model.worldID); //wid
        build.append(" , ");
        build.append(1); //type
        build.append(" , ");
        build.append(model.level);
        build.append(" , ");
        build.append(model.gender);
        build.append(" , ");
        build.append(model.pop);
        build.append(" , ");
        build.append(model.totalRank);
        build.append(" , ");
        build.append(model.worldRank);
        build.append(" , ");
        build.append(model.jobRank);
        build.append(" , ");
        build.append(model.popRank);
        build.append(" , ");
        build.append(model.Exp);
        build.append(" , ");
        build.append(getString(model.userName));
        build.append(" , ");
        build.append(getString(null));
        build.append(" , ");
        build.append(getString("없음 ㅋ"));
        build.append(" , ");
        build.append(getString(model.jobName));
        build.append(" , ");
        build.append(getString(model.jobDetail));
        build.append(" , ");
        build.append(System.currentTimeMillis());
        build.append(" , ");
        build.append(getString("http://s.nx.com/s2/game/maplestory/maple2007/image/icon/ico_world_luna.gif"));
        build.append(" , ");
        build.append(getString(model.userImage));
        build.append(");");
        return build.toString();
    }
    private String getString(String str){
        if(str == null){
            return "\'\'";
        }else{
            return "\'"+str+"\'";
        }
    }

    // Opener of DB and Table
    private class OpenHelper extends SQLiteOpenHelper {

        public OpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                          int version) {
            super(context, name, null, version);
            // TODO Auto-generated constructor stub
        }

        // 생성된 DB가 없을 경우에 한번만 호출됨
        @Override
        public void onCreate(SQLiteDatabase arg0) {
            // String dropSql = "drop table if exists " + tableName;
            // db.execSQL(dropSql);

        }

        @Override
        public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
        }
    }
}
