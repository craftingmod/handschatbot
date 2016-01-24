package com.craftingmod.maplechatbot.chat;

import android.os.AsyncTask;
import android.os.HandlerThread;
import android.util.Log;

import com.anprosit.android.promise.NextTask;
import com.anprosit.android.promise.Promise;
import com.anprosit.android.promise.Task;
import com.craftingmod.maplechatbot.Config;
import com.craftingmod.maplechatbot.model.UserModel;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.os.Handler;

import eu.chainfire.libsuperuser.Shell;

/**
 * Created by superuser on 15/12/13.
 */
public final class CharacterFinder implements Task<HashMap<Integer,String>, ArrayList<UserModel>> {

    public static final Integer ACCOUNT_ID = 1;
    public static final Integer CHARACTER_ID = 2;
    public static final Integer NICKNAME = 3;
    public static final Integer ALL = 4;

    protected final String sql = "SELECT * FROM character_table_info WHERE $search";
    protected final String cmd = "sqlite3 $p \"" + sql + "\"";

    private ListMultimap<Integer,UserModel> users;
    private ArrayList<UserModel> row;
    protected Gson g;
    protected Long lastSync;
    private HandlerThread thread;
    private Handler handler;

    private static CharacterFinder instance;

    public static CharacterFinder getInstance(){
        if(instance == null){
            instance = new CharacterFinder();
        }
        return instance;
    }

    private CharacterFinder(){
        thread = new HandlerThread("CharacterFinder");
        thread.start();
        handler = new Handler(thread.getLooper());
        g = new GsonBuilder().create();
        lastSync = 0L;
    }
    @Override
    public void run(final HashMap<Integer,String> param, final NextTask<ArrayList<UserModel>> task) {
        if(lastSync < System.currentTimeMillis()-7200 * 1000){
            sync();
        }
        genSQL(param);
        Log.d("MapleInfoSQL", "Search data:" + g.toJson(param));
        handler.post(new Runnable() {
            @Override
            public void run() {
                ArrayList<UserModel> output = new ArrayList<>();
                if (param.containsKey(ACCOUNT_ID)) {
                    List<UserModel> user = users.get(Integer.parseInt(param.get(ACCOUNT_ID)));
                    if(user != null){
                        for (int i = 0; i < user.size(); i += 1) {
                            final UserModel loopModel = user.get(i);
                            if (param.containsKey(CHARACTER_ID)) {
                                if (loopModel.characterID == Integer.parseInt(param.get(CHARACTER_ID))) {
                                    output.add(loopModel);
                                }
                            } else {
                                output.add(loopModel);
                            }
                        }
                    }
                    if (user != null && user.size() >= 1 && output.size() == 0) {
                        // New character
                        user.get(0).userName = "$" + user.get(0).userName;
                        output.add(user.get(0));
                    }
                } else {
                    if (param.containsKey(NICKNAME)) {
                        for (int i = 0; i < row.size(); i += 1) {
                            if (row.get(i).userName.equalsIgnoreCase(param.get(NICKNAME))) {
                                output.add(row.get(i));
                                break;
                            }
                        }
                    }
                    if(param.containsKey(ALL)){
                        task.run(row);
                        return;
                    }
                }
                task.run(output);
            }
        });
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
    public static HashMap<Integer,String> getSearch(int account,int user){
        HashMap<Integer,String> hmap = new HashMap<>();
        hmap.put(ACCOUNT_ID,account + "");
        hmap.put(CHARACTER_ID, user + "");
        return hmap;
    }
    public static HashMap<Integer,String> getSearch(String username){
        HashMap<Integer,String> hmap = new HashMap<>();
        hmap.put(NICKNAME,username);
        return hmap;
    }
    private String genSQL(HashMap<Integer,String> search){
        HashMap<String,String> map = new HashMap<>();
        if(search.containsKey(ACCOUNT_ID)){
            map.put("aid",search.get(ACCOUNT_ID));
        }
        if(search.containsKey(CHARACTER_ID)){
            map.put("cid",search.get(CHARACTER_ID));
        }
        if(search.containsKey(NICKNAME)){
            map.put("name",search.get(NICKNAME));
        }
        StringBuilder sqlBuilder = new StringBuilder();
        int i =0;
        for (Map.Entry<String, String> entry : map.entrySet()){
            sqlBuilder.append(entry.getKey() + "=\'" + entry.getValue() + "\'");
            if(i < search.size()-1){
                sqlBuilder.append(" AND ");
            }
            i += 1;
        }
        String command = (cmd+"").replace("$p",Config.DATABASE_PATH).replace("$search", sqlBuilder.toString());
        Log.d("MapleInfoSQL",command);
        return command;
    }

    public void sync(){
        users = ArrayListMultimap.create();
        row = new ArrayList<>();
        lastSync = System.currentTimeMillis();
        handler.post(new Runnable() {
            @Override
            public void run() {
                final String command = (cmd + "").replace("$p", Config.DATABASE_PATH).replace("$search", "wid=\'" + Config.WORLD_BOT_ID + "\'");
                Log.d("MapleInfoSQL", command);
                List<String> result = Shell.SU.run(command);
                if (result.size() < 1) {
                    Log.e("CharacterFinder", "No result.");
                } else if (result.size() == 1 && result.get(0).contains("Error: database is locked")) {
                    Log.e("CharacterFinder", "DB locked.");
                } else {
                    for (int i = 0; i < result.size(); i += 1) {
                        final UserModel model = parse(result.get(i));
                        users.put(model.accountID, model);
                        row.add(model);
                    }
                }
            }
        });
    }
}
