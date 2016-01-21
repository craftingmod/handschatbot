package com.craftingmod.maplechatbot.chat;

import android.os.AsyncTask;
import android.util.Log;

import com.anprosit.android.promise.NextTask;
import com.anprosit.android.promise.Task;
import com.craftingmod.maplechatbot.Config;
import com.craftingmod.maplechatbot.model.UserModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.chainfire.libsuperuser.Shell;

/**
 * Created by superuser on 15/12/13.
 */
public class CharacterFinder implements Runnable, Task<HashMap<String,String>, ArrayList<UserModel>>, Shell.OnCommandLineListener {
    protected NextTask<ArrayList<UserModel>> task;
    protected HashMap<String,String> search;
    protected ArrayList<UserModel> out;
    protected Gson g;
    protected final String sql = "SELECT * FROM character_table_info WHERE $search";
    protected final String cmd = "sqlite3 $p \"" + sql + "\"";

    protected Shell.Interactive session;

    public CharacterFinder(){
        session = new Shell.Builder().useSU().open();
        out = new ArrayList<>();
        g = new GsonBuilder().create();
    }

    @Override
    public void run(HashMap<String,String> srch, NextTask<ArrayList<UserModel>> nextTask) {
        task = nextTask;
        search = srch;
        Log.d("MapleInfoSQL", "Search data:" + g.toJson(search));
        this.run();
        //new Thread(this).run();
    }

    @Override
    public void run() {
        StringBuilder sqlBuilder = new StringBuilder();
        int i =0;
        for (Map.Entry<String, String> entry : search.entrySet()){
            sqlBuilder.append(entry.getKey() + "=\'" + entry.getValue() + "\'");
            if(i < search.size()-1){
                sqlBuilder.append(" AND ");
            }
            i += 1;
        }
        String command = (cmd+"").replace("$p",Config.DATABASE_PATH).replace("$search", sqlBuilder.toString());
        Log.d("MapleInfoSQL",command);
        session.addCommand(command, 0, this);
    }

    public static UserModel parse(String oneline){
        String[] piece = oneline.split("\\|");

        UserModel uModel;
        uModel = new UserModel(Integer.parseInt(piece[0]),Integer.parseInt(piece[1]),piece[12]);
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
        if(piece.length >= 20){
            uModel.userImage = piece[19];
        }
        if(piece[16].length() < 1){
            if(piece[15].length() < 1){
                uModel.job = "없음";
            }else{
                uModel.job = piece[15];
            }
        }else{
            uModel.job = piece[16];
        }
        return uModel;
    }
    public static HashMap<String,String> getSearch(int account,int user){
        HashMap<String,String> hmap = new HashMap<>();
        hmap.put("aid",account + "");
        hmap.put("cid",user + "");
        return hmap;
    }

    @Override
    public void onCommandResult(int commandCode, int exitCode) {
        if (exitCode < 0) {
            task.fail(null,new Exception("Shell exited at " + exitCode));
        }else{
            task.run(out);
        }
    }

    @Override
    public void onLine(String line) {
        if(line.contains("Error: database is locked")){
            Log.e("Maple", "Database is locked");
            task.fail(null,new Exception("Database is locked."));
            return;
        }
        UserModel model = parse(line);
        out.add(parse(line));
        try{
            Log.d("MapleDB",new GsonBuilder().create().toJson(model));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
