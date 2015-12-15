package com.craftingmod.maplechatbot.chat;

import android.os.AsyncTask;
import android.util.Log;

import com.anprosit.android.promise.NextTask;
import com.anprosit.android.promise.Task;
import com.craftingmod.maplechatbot.model.UserModel;

import java.util.ArrayList;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

/**
 * Created by superuser on 15/12/13.
 */
public class CharacterFinder extends AsyncTask<String,Void,ArrayList<UserModel>> {
    private NextTask<ArrayList<UserModel>> task;
    public CharacterFinder(NextTask<ArrayList<UserModel>> tsk){
        task = tsk;
    }
    @Override
    protected ArrayList<UserModel> doInBackground(String... params) {
        String cmd = "sqlite3 /data/data/com.Nexon.MonsterLifeChat/databases/maplehands.db \"SELECT * FROM character_table_info WHERE " + params[0] + "\"";
        List<String> suList = Shell.SU.run(cmd);
        if(suList == null || suList.size() < 1){
            return null;
        }
        ArrayList<UserModel> users = new ArrayList<>();
        for(int i=0;i<suList.size();i+=1){
            users.add(CharacterLoader.parse(suList.get(i)));
            Log.d("WBUS",suList.get(i));
            Log.d("WBUS","--");
        }
        return users;
    }
    @Override
    protected void onPostExecute(ArrayList<UserModel> result){
        if(result != null && result.size() >= 1){
            task.run(result);
        }else{
            task.fail(null,null);
        }
    }
}
