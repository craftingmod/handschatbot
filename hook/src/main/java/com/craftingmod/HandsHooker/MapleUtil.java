package com.craftingmod.HandsHooker;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import com.anprosit.android.promise.NextTask;
import com.craftingmod.HandsHooker.model.FriendModel;
import com.craftingmod.HandsHooker.model.SimpleUserModel;
import com.craftingmod.HandsHooker.model.UserModel;
import com.google.common.base.Splitter;

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
public final class MapleUtil {

    private HandlerThread thread;
    private Handler handler;
    private static MapleUtil instance;
    public static MapleUtil getInstance(){
        if(instance == null){
            instance = new MapleUtil();
        }
        return instance;
    }
    public MapleUtil(){
        thread = new HandlerThread("MapleUtil");
        thread.start();
        handler = new Handler(thread.getLooper());
    }
    public static UserModel getCharacterInfo(final int worldID, final int characterID){
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
                try {
                    task.run(getCharacterInfo(worldID, characterID));
                } catch (Exception e) {
                    task.fail(null, e);
                }
            }
        });
    }
    public static ArrayList<FriendModel> getAccountFriendList(int accountID){
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
    private static String callSOAP(String methodName, HashMap<String,Integer> params,int timeout){

        Object obj = callSOAPtoOBJ(methodName, params, timeout);
        if(obj != null){
            return ((SoapObject) obj).toString();
        }else{
            return null;
        }
    }
    private static Object callSOAPtoOBJ(String methodName, HashMap<String,Integer> params,int timeout){
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
