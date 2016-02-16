package com.craftingmod.maplebot.heavy.core;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.craftingmod.maplebot.model.SimpleUserModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by superuser on 16/2/15.
 */
public class CustomDB {

    private static final String dbName = "custom.db";
    public static final int dbVersion = 1;

    protected final String sqlDefault = "SELECT * FROM 'character' WHERE $search;";

    // DB관련 객체 선언
    private OpenHelper opener; // DB opener
    private SQLiteDatabase db; // DB controller

    // 부가적인 객체들
    private Context context;

    public CustomDB(Context ct){
        context = ct;
        this.opener = new OpenHelper(context, dbName, null, dbVersion);
        db = opener.getWritableDatabase();
        try{
            db.execSQL("CREATE TABLE 'character' ('characterID' INT NOT NULL,'accountID' INT NOT NULL,'username' VARCHAR(25),'worldID' INT,'id' LONG,PRIMARY KEY('id'));");
        }catch (Exception e){}
    }

    public void execSQL(String sql){
        db.execSQL(sql);
    }
    public Cursor rawSQL(String sql){
        return db.rawQuery(sql, null);
    }

    private HashMap<String,String> getSearch(int account,int user){
        HashMap<String,String> hmap = new HashMap<>();
        hmap.put("accountID",account + "");
        hmap.put("characterID",user + "");
        return hmap;
    }
    private HashMap<String,String> getSearch(String userName){
        HashMap<String,String> hmap = new HashMap<>();
        hmap.put("username",userName);
        return hmap;
    }
    public void put(SimpleUserModel user){
        try{
            execSQL(genSQL(user));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public ArrayList<SimpleUserModel> search(int account,int user){
        return search(getSearch(account, user));
    }
    public ArrayList<SimpleUserModel> search(String userName){
        return search(getSearch(userName));
    }
    public ArrayList<SimpleUserModel> search(HashMap<String,String> searches){
        StringBuilder sqlBuilder = new StringBuilder();
        int i =0;
        for (Map.Entry<String, String> entry : searches.entrySet()){
            sqlBuilder.append(entry.getKey() + "=\'" + entry.getValue() + "\'");
            if(i < searches.size()-1){
                sqlBuilder.append(" AND ");
            }
            i += 1;
        }
        String sql = (sqlDefault+"").replace("$search", sqlBuilder.toString());
        Cursor cursor = db.rawQuery(sql, null);

        ArrayList<SimpleUserModel> out = new ArrayList<>();
        if(cursor.moveToFirst()){
            do {
                out.add(parse(cursor));
            } while (cursor.moveToNext());
        }
        return out;
    }
    public static SimpleUserModel parse(Cursor oCursor){
        SimpleUserModel uModel;
        uModel = new SimpleUserModel(oCursor.getInt(1),oCursor.getInt(0),oCursor.getString(2));
        uModel.worldID = oCursor.getInt(3);
        return uModel;
    }
    private String genSQL(SimpleUserModel model){
        StringBuilder build = new StringBuilder();
        build.append("INSERT INTO 'character' VALUES (");
        build.append(model.characterID); // cid
        build.append(" , ");
        build.append(model.accountID); //aid
        build.append(" , ");
        build.append(getString(model.userName));
        build.append(" , ");
        build.append(model.worldID); //wid
        build.append(" , ");
        long num = (long) (model.worldID * Math.pow(10,10) + model.characterID);
        build.append(num); //wid
        build.append(");");
        return build.toString();
    }
    public static long genKey(int worldID,int characterID){
        return (long) (worldID * Math.pow(10,10) + characterID);
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
