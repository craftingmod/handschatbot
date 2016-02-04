package com.craftingmod.maplechatbot;

import android.content.Context;
import android.os.AsyncTask;
import android.os.HandlerThread;
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
import android.os.Handler;

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
    private HandlerThread thread;
    private Handler handler;
    private static MapleUtil instance;
    public static MapleUtil getInstance(Context context){
        if(instance == null){
            instance = new MapleUtil(context);
        }
        return instance;
    }
    public static MapleUtil getInstance(){
        if(instance == null){
            instance = new MapleUtil();
        }
        return instance;
    }
    public MapleUtil(Context ct){
        this();
        context = ct;
    }
    public MapleUtil(){
        thread = new HandlerThread("MapleUtil");
        thread.start();
        handler = new Handler(thread.getLooper());
    }
    public void getAccountFriendList(final int accountID, final NextTask<ArrayList<FriendModel>> task){
        handler.post(new Runnable() {
            @Override
            public void run() {
                try{
                    task.run(getAccountFriendList(accountID));
                }catch (Exception e){
                    task.fail(null,e);
                }
            }
        });
    }
    public ArrayList<FriendModel> getAccountFriendList(int accountID){
        final String splitString = "AccountFriendList=anyType";
        final HashMap<String,Integer> map = new HashMap<>();
        map.put("AccountID", accountID);

        String string = callSOAP("GetAccountFriendList", map, 10);
        final ArrayList<FriendModel> out = new ArrayList<>();
       // Log.d("MapleUtil",string);
        List<String> split = Splitter.on(splitString).trimResults().omitEmptyStrings().splitToList(string);
        for(int k=0;k<split.size();k+=1){
            out.add(new FriendModel(split.get(k)));
        }
        return out;
    }
    public ArrayList<SimpleUserModel> getCharacterList(int accountID,int worldID){
        final HashMap<String,Integer> map = new HashMap<>();
        final String splitString = "Web_CharacterList=anyType";
        map.put("AccountID",accountID);
        map.put("WorldID", worldID);

        String s = callSOAP("GetCharacterList",map,10);
        //Log.d("MapleUtil", s);
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
        return out;
    }
    public void getCharacterList(final int accountID,final int worldID,final NextTask<ArrayList<SimpleUserModel>> task){
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    task.run(getCharacterList(accountID, worldID));
                } catch (Exception e) {
                    task.fail(null, e);
                }
            }
        });
    }
    public boolean isGameLogin(int accountID){
        final HashMap<String,Integer> map = new HashMap<>();
        map.put("n4AccountID",accountID);
        SoapPrimitive ob = (SoapPrimitive) callSOAPtoOBJ("IsGameLogin", map, 20);
        return Boolean.parseBoolean(ob.toString());
    }
    public void isGameLogin(final int accountID,final NextTask<Boolean> task){
        handler.post(new Runnable() {
            @Override
            public void run() {
                try{
                    task.run(isGameLogin(accountID));
                }catch (Exception e){
                    task.fail(null,e);
                }
            }
        });
    }
    public UserModel getCharacterInfo(final int worldID, final int characterID){
        final HashMap<String,Integer> map = new HashMap<>();
        map.put("CharacterID", characterID);
        map.put("WorldID", worldID);

        String result = callSOAP("GetCharacterInfo",map,50);
        // Log.d("MapleUtil", result);
        UserModel out = new UserModel(0,characterID,"");
        out.worldID = worldID;
        out.parse(result);
        return out;
    }
    public void getCharacterInfo(final int worldID, final int characterID, final NextTask<UserModel> task){
        handler.post(new Runnable() {
            @Override
            public void run() {
                try{
                    task.run(getCharacterInfo(worldID, characterID));
                }catch (Exception e){
                    task.fail(null,e);
                }
            }
        });
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
