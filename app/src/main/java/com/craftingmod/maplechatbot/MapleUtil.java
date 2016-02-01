package com.craftingmod.maplechatbot;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.anprosit.android.promise.NextTask;
import com.craftingmod.maplechatbot.model.FriendModel;
import com.craftingmod.maplechatbot.model.SimpleUserModel;
import com.craftingmod.maplechatbot.model.UserModel;
import com.google.common.base.Splitter;
import com.google.common.collect.HashBiMap;

import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by superuser on 16/1/25.
 */
@SuppressWarnings("unchecked")
public class MapleUtil {

    private Context context;
    private static MapleUtil instance;
    public static MapleUtil getInstance(Context context){
        if(instance == null){
            instance = new MapleUtil(context);
        }
        return instance;
    }
    public static MapleUtil getInstance(){
        if(instance != null){
            return instance;
        }else{
            return new MapleUtil();
        }
    }
    public MapleUtil(Context ct){
        context = ct;
    }
    public MapleUtil(){
    }
    public void getAccountFriendList(int accountID, final NextTask<ArrayList<FriendModel>> task){
        final String splitString = "AccountFriendList=anyType";
        HashMap<String,Integer> map = new HashMap<>();
        map.put("AccountID", accountID);
        //noinspection unchecked
        new AsyncTask<HashMap<String, Integer>, Void, String>() {
            @Override
            protected String doInBackground(HashMap<String, Integer>... params) {
                return callSOAP("GetAccountFriendList", params[0], 10);
            }
            @Override
            protected void onPostExecute(String string) {
                super.onPostExecute(string);
                final ArrayList<FriendModel> out = new ArrayList<>();
                Log.d("MapleUtil",string);
                List<String> split = Splitter.on(splitString).trimResults().omitEmptyStrings().splitToList(string);
                for(int k=0;k<split.size();k+=1){
                    out.add(new FriendModel(split.get(k)));
                }
                task.run(out);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, map);
    }
    public void getCharacterList(final int accountID,int worldID,final NextTask<ArrayList<SimpleUserModel>> task){
        HashMap<String,Integer> map = new HashMap<>();
        final String splitString = "Web_CharacterList=anyType";
        map.put("AccountID",accountID);
        map.put("WorldID", worldID);
        new AsyncTask<HashMap<String,Integer>,Void,String>(){
            @Override
            protected String doInBackground(HashMap<String, Integer>... params) {
                return callSOAP("GetCharacterList",params[0],10);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                Log.d("MapleUtil", s);
                final ArrayList<SimpleUserModel> out = new ArrayList<>();
                List<String> split = Splitter.on(splitString).trimResults().omitEmptyStrings().splitToList(s);
                for(int k=1;k<split.size();k+=1){
                    SimpleUserModel model = new SimpleUserModel(accountID,-1,"");
                    String parseStr = split.get(k);
                    parseStr = parseStr.replaceAll("anyType\\{\\}", "null");
                    parseStr = parseStr.substring(parseStr.indexOf("{")+1,parseStr.indexOf("};"));
                    List<String> maps = Splitter.on(";").trimResults().omitEmptyStrings().splitToList(parseStr);
                    for(int j=0;j<maps.size();j+=1){
                        String keyStr = maps.get(j);
                        String key = keyStr.split("=")[0];
                        String value = keyStr.split("=")[1];
                        if(key.equalsIgnoreCase("GameWorldID")){
                            model.worldID = Integer.parseInt(value);
                        }
                        if(key.equalsIgnoreCase("CharacterID")){
                            model.characterID = Integer.parseInt(value);
                        }
                        if(key.equalsIgnoreCase("CharacterName")){
                            model.userName = value;
                        }
                        if(key.equalsIgnoreCase("AccountID")){
                            model.accountID = Integer.parseInt(value);
                        }
                        if(key.equalsIgnoreCase("RegisterDate")){
                            model.registerDate = value;
                        }
                    }
                    if(model.characterID > 0){
                        out.add(model);
                    }
                }
                task.run(out);
                // Web_CharacterList=anyType
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,map);
    }
    public void isGameLogin(int accountID,final NextTask<Boolean> task){
        HashMap<String,Integer> map = new HashMap<>();
        map.put("n4AccountID",accountID);
        new AsyncTask<HashMap<String,Integer>,Void,SoapPrimitive>(){
            @Override
            protected SoapPrimitive doInBackground(HashMap<String, Integer>... params) {
                return (SoapPrimitive)callSOAPtoOBJ("IsGameLogin",params[0],20);
            }

            @Override
            protected void onPostExecute(SoapPrimitive ob) {
                super.onPostExecute(ob);
                task.run(Boolean.parseBoolean(ob.toString()));
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, map);
    }
    public void getCharacterInfo(final int worldID, final int characterID, final NextTask<UserModel> task){
        HashMap<String,Integer> map = new HashMap<>();
        map.put("CharacterID", characterID);
        map.put("WorldID", worldID);
        new AsyncTask<HashMap<String,Integer>,Void,String>(){
            @Override
            protected String doInBackground(HashMap<String, Integer>... params) {
                return callSOAP("GetCharacterInfo",params[0],50);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                Log.d("MapleUtil", s);
                UserModel out = new UserModel(0,characterID,"");
                out.worldID = worldID;
                out.parse(s);
                task.run(out);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, map);
    }
    private String callSOAP(String methodName, HashMap<String,Integer> params,int timeout){

        Object obj = callSOAPtoOBJ(methodName, params, timeout);
        if(obj != null){
            return ((SoapObject) obj).toString();
        }else{
            return null;
        }
    }
    private Object callSOAPtoOBJ(String methodName, HashMap<String,Integer> params,int timeout){
        SoapObject soapO = new SoapObject("http://api.maplestory.nexon.com/soap/",methodName);
        for (Map.Entry<String, Integer> entry : params.entrySet()){
            soapO.addPropertyIfValue(entry.getKey(), entry.getValue());
        }
        SoapSerializationEnvelope scope = new SoapSerializationEnvelope(120);
        scope.setOutputSoapObject(soapO);
        scope.dotNet = true;
        HttpTransportSE pipe = new HttpTransportSE("http://api.maplestory.nexon.com/soap/MobileApp.asmx",timeout*1000);
        pipe.debug = false;
        try {
            pipe.call("http://api.maplestory.nexon.com/soap/"+methodName, scope);
            return scope.getResponse();
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
