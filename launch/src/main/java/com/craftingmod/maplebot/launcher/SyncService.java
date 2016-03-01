package com.craftingmod.maplebot.launcher;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.craftingmod.maplebot.Config;
import com.craftingmod.maplebot.MapleUtil;
import com.craftingmod.maplebot.Save;
import com.craftingmod.maplebot.model.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import eu.chainfire.libsuperuser.Shell;

/**
 * Created by superuser on 16/2/1.
 */
public class SyncService extends Service implements Shell.OnCommandLineListener {
    private Context context;
    private Gson g;
    private Shell.Interactive shell;
    private ListFriend handlerFriend;
    private WriteCharacter handlerUser;
    private HandlerThread thread;
    private DBManager mManager;
    private final String database_path = "/data/mapleHands/maple.db";

    private int world_ID;
    private String world_String;

    @Override
    public void onCreate() {
        super.onCreate();
    }
    private void init(){
        context = this.getBaseContext();
        g =  new GsonBuilder().create();
        shell = new Shell.Builder().useSU()
                .setOnSTDOUTLineListener(this)
                .setOnSTDERRLineListener(this)
                .setWantSTDERR(true)
                .open();
        thread = new HandlerThread("Syncronize_friend_List",HandlerThread.MAX_PRIORITY);
        thread.start();
        handlerFriend = new ListFriend(thread.getLooper());
        handlerUser = new WriteCharacter(thread.getLooper());
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        init();
        doForeground();
        this.start();
        Log.d("Maple","Start");
        return Service.START_STICKY;
    }
    public void writeDB(){
        ArrayList<String> commands = new ArrayList<>();
    }
    public void doForeground(){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic)
                        .setContentTitle("동기화 중")
                        .setContentText("DB 동기화 중");
        startForeground(5573, mBuilder.build());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCommandResult(int commandCode, int exitCode) {}

    @Override
    public void onLine(String line) {
        Log.d("Console", line);
    }

    private void start(){
        /*
        Save config = Config.read();
        if(config.masterAID >= 0){
            world_ID = Config.WORLD_ID;
            world_String = Config.WORLDS[world_ID];
            handlerFriend.sendMessage(handlerFriend.createMsg(Config.ACCOUNT_ID, 0));
        }
        */
        world_ID = Config.WORLD_ID;
        world_String = Config.WORLDS[world_ID];
        handlerFriend.sendMessage(handlerFriend.createMsg(Config.ACCOUNT_ID,0));
    }
    private void initDB(){
        mManager = new DBManager(this);
        try{
            mManager.execSQL("DROP TABLE 'character_table_info';");
        }catch (SQLiteException e){
            e.printStackTrace();
        }
        try{
            mManager.execSQL("DROP TABLE 'masterCharacter';");
        }catch (SQLiteException e){
            e.printStackTrace();
        }
        String createSql = "CREATE TABLE `character_table_info` (   `aid` INT NOT NULL, `cid` INT NOT NULL, `wid` INT NOT NULL, `type` INT NOT NULL,    `level` INT NOT NULL,   `gender` INT NOT NULL,  `pop` INT NOT NULL, `totalrank` INT NOT NULL,   `worldrank` INT NOT NULL,   `jobrank` INT NOT NULL, `poprank` INT NOT NULL,   `exp` BIGINT(20) NOT NULL,    `name` VARCHAR(25) NOT NULL,    `nick_name`   VARCHAR(25) NOT NULL,     `wname` VARCHAR(25) NOT NULL,   `jobname` VARCHAR(25) NOT NULL,     `jobdetail` VARCHAR(25) NOT NULL,   `update_date` BIGINT(20) NOT NULL,  `wimg_url` VARCHAR(512) NOT NULL,   `img_url` VARCHAR(512) NOT NULL);";
        mManager.execSQL(createSql);
        mManager.execSQL("CREATE TABLE 'masterCharacter' ('accountID' INT NOT NULL,'charID' INT NOT NULL,'nickname' VARCHAR(25));");
        mManager.execSQL((createSql+"").replace("character_table_info","users"));
        mManager.db.beginTransaction();
        /*
        shell.addCommand("rm -rf /data/mapleHands");
        shell.addCommand("mkdir /data/mapleHands");
        ArrayList<String> createTable = new ArrayList<>();
        createTable.add("sqlite3 " + database_path);
        createTable.add("CREATE TABLE `character_table_info` (   `aid` INT NOT NULL, `cid` INT NOT NULL, `wid` INT NOT NULL, `type` INT NOT NULL,    `level` INT NOT NULL,   `gender` INT NOT NULL,  `pop` INT NOT NULL, `totalrank` INT NOT NULL,   `worldrank` INT NOT NULL,   `jobrank` INT NOT NULL, `poprank` INT NOT NULL,   `exp` BIGINT(20) NOT NULL,    `name` VARCHAR(25) NOT NULL,    `nick_name`   VARCHAR(25) NOT NULL,     `wname` VARCHAR(25) NOT NULL,   `jobname` VARCHAR(25) NOT NULL,     `jobdetail` VARCHAR(25) NOT NULL,   `update_date` BIGINT(20) NOT NULL,  `wimg_url` VARCHAR(512) NOT NULL,   `img_url` VARCHAR(512) NOT NULL);");
        createTable.add(".exit");
        shell.addCommand(createTable);
        */
    }
    private void writeUser(ListFriend friends){
        initDB();
        final int size = friends.accounts.size();
        for(int i=0;i<size;i+=1){
            handlerUser.sendEmptyMessage(friends.accounts.get(i).aID);
        }
        handlerUser.sendEmptyMessage(-7);
        handlerUser.sizeOfAcc = size;
        friends.accounts.clear();
    }
    private String genSQL(UserModel model){
        StringBuilder build = new StringBuilder();
        build.append("INSERT INTO character_table_info VALUES (");
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
        build.append(getString(model.userName.toLowerCase()));
        build.append(" , ");
        build.append(getString(null));
        build.append(" , ");
        build.append(getString(world_String));
        build.append(" , ");
        build.append(getString(model.jobName));
        build.append(" , ");
        build.append(getString(model.jobDetail));
        build.append(" , ");
        build.append(System.currentTimeMillis());
        build.append(" , ");
        build.append(getString("i"));
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
    public class WriteCharacter extends Handler {
        private int accounts;
        public int sizeOfAcc;

        protected WriteCharacter(Looper looper){
            super(looper);
            this.accounts = 0;
            this.sizeOfAcc = 0;
        }
        @Override
        public void handleMessage(Message msg) {
            int aID = msg.what;
            if(aID == -7){
                mManager.db.setTransactionSuccessful();
                mManager.db.endTransaction();
                Log.d("SyncUtil", "Syncing Finished.");
                context.sendBroadcast(new Intent("broadcast.maple.shutdown"));
                handlerUser.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<String> cmds = new ArrayList<>();
                        long ms = System.currentTimeMillis();
                        cmds.add("rm -rf /data/data/com.craftingmod.maplebot/databases/users.db");
                        cmds.add("cp -R /data/data/com.craftingmod.maplebot.launcher/databases/users.db /data/data/com.craftingmod.maplebot/databases/users.db");
                        cmds.add("cp -R /data/data/com.craftingmod.maplebot.launcher/databases/users.db-journal /data/data/com.craftingmod.maplebot/databases/users.db-journal");
                        cmds.add("chmod 664 /data/data/com.craftingmod.maplebot/databases/*");
                        cmds.add("chown " + Config.ID_SERVICE + ":" + Config.ID_SERVICE + " /data/data/com.craftingmod.maplebot/databases/*");
                        Shell.SU.run(cmds);
                        Intent light = new Intent();
                        Intent heavy = new Intent();
                        light.setComponent(new ComponentName("com.craftingmod.maplebot","com.craftingmod.maplebot.light.LightService"));
                        heavy.setComponent(new ComponentName("com.craftingmod.maplebot","com.craftingmod.maplebot.heavy.HeavyService"));
                        startService(light);
                        startService(heavy);
                        stopSelf();
                    }
                },5000);
                return;
            }
            if(aID <= 0){
                return;
            }
            Log.d("SyncUtil", "Syncing Character " + aID + " - " + accounts + "/" + sizeOfAcc);
            try{
                ArrayList<SimpleUserModel> simpleList = MapleUtil.getCharacterList(aID, world_ID);
                UserModel masterChar = null;
                for(int i=0;i<simpleList.size();i+=1){
                    final SimpleUserModel sModel = simpleList.get(i);
                    UserModel model = MapleUtil.getInstance().getCharacterInfo(sModel.worldID,sModel.characterID);
                    if(model.level <= 0){
                        continue;
                    }
                    model.accountID = aID;
                    if(masterChar == null || masterChar.level < model.level){
                        masterChar = model;
                    }
                    mManager.execSQL(genSQL(model));
                    //this.users.add(model);
                }
                if(masterChar != null){
                    mManager.execSQL("INSERT INTO masterCharacter VALUES(" + masterChar.accountID + "," + masterChar.characterID + ",\'"+masterChar.userName + "\');");
                }
                this.accounts = this.accounts + 1;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public class ListFriend extends Handler {

        public static final int MAX_CHON = 2;
        public ArrayList<SyncAIDModel> accounts; // Accounts
        public ArrayList<Integer> searched; // Searched AID;

        protected ListFriend(Looper looper){
            super(looper);
            searched = new ArrayList<>();
            accounts = new ArrayList<>();
            //this.aID = aid;
        }
        @Override
        public void handleMessage(Message msg) {
            if(msg.what != 1){
                return;
            }
            int aID = msg.arg1;
            int bridge = msg.arg2;
            if(bridge > MAX_CHON){
                this.searched.add(aID);
                nextSearch();
                return;
            }
            Log.d("SyncUtil", "Syncing AID " + aID + " / " + accounts.size() + " ~ " + searched.size() + " ~ " + bridge);
            try{
                ArrayList<FriendModel> split = MapleUtil.getInstance().getAccountFriendList(aID);
                for(int k=0;k<split.size();k+=1){
                    FriendModel model = split.get(k);
                    SyncAIDModel syncModel = new SyncAIDModel(model.accountID,bridge+1);
                    if(model.worldID != world_ID || accounts.contains(syncModel) || syncModel.aID == aID){
                        // not equals world ID OR already exists
                        continue;
                    }
                    this.accounts.add(syncModel);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            // add searched AID
            this.searched.add(aID);
            nextSearch();
        }
        private void nextSearch(){
            Boolean left = false;
            for(int i=0;i<this.accounts.size();i+=1){
                SyncAIDModel model = this.accounts.get(i);
                if(model.bridge > MAX_CHON){
                    continue;
                } else if(this.searched.contains(model.aID)){
                    continue;
                }else{
                    sendMessage(createMsg(model.aID,model.bridge));
                    left = true;
                    break;
                }
            }
            if(!left){
                Log.d("SyncUtil", "Finished.");
                searched.clear();
                writeUser(this);
            }
        }
        private Message createMsg(int aid,int bridge){
            Message msg = new Message();
            msg.what = 1;
            msg.arg1 = aid;
            msg.arg2 = bridge;
            return msg;
        }
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


    //DB를 총괄관리
    public class DBManager {

        // DB관련 상수 선언
        private static final String dbName = "users.db";
        private static final String tableName = "APinfo";
        public static final int dbVersion = 1;

        // DB관련 객체 선언
        private OpenHelper opener; // DB opener
        private SQLiteDatabase db; // DB controller

        // 부가적인 객체들
        private Context context;

        // 생성자
        public DBManager(Context context) {
            this.context = context;
            this.opener = new OpenHelper(context, dbName, null, dbVersion);
            db = opener.getWritableDatabase();
        }

        // Opener of DB and Table
        private class OpenHelper extends SQLiteOpenHelper {

            public OpenHelper(Context context, String name, CursorFactory factory,
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
        public void execSQL(String sql){
            db.execSQL(sql);
        }
        public Cursor selectSQL(String sql){
            return db.rawQuery(sql, null);
        }


        // 데이터 추가
        /*
        public void insertData(APinfo info) {
            String sql = "insert into " + tableName + " values(NULL, '"
                    + info.getSSID() + "', " + info.getCapabilities() + ", '"
                    + info.getPasswd() + "');";
            db.execSQL(sql);
        }

        // 데이터 갱신
        public void updateData(APinfo info, int index) {
            String sql = "update " + tableName + " set SSID = '" + info.getSSID()
                    + "', capabilities = " + info.getCapabilities()
                    + ", passwd = '" + info.getPasswd() + "' where id = " + index
                    + ";";
            db.execSQL(sql);
        }

        // 데이터 삭제
        public void removeData(int index) {
            String sql = "delete from " + tableName + " where id = " + index + ";";
            db.execSQL(sql);
        }

        // 데이터 검색
        public APinfo selectData(int index) {
            String sql = "select * from " + tableName + " where id = " + index
                    + ";";
            Cursor result = db.rawQuery(sql, null);

            // result(Cursor 객체)가 비어 있으면 false 리턴
            if (result.moveToFirst()) {
                APinfo info = new APinfo(result.getInt(0), result.getString(1),
                        result.getInt(2), result.getString(3));
                result.close();
                return info;
            }
            result.close();
            return null;
        }

        // 데이터 전체 검색
        public ArrayList<apinfo> selectAll() {
            String sql = "select * from " + tableName + ";";
            Cursor results = db.rawQuery(sql, null);

            results.moveToFirst();
            ArrayList<apinfo> infos = new ArrayList<apinfo>();

            while (!results.isAfterLast()) {
                APinfo info = new APinfo(results.getInt(0), results.getString(1),
                        results.getInt(2), results.getString(3));
                infos.add(info);
                results.moveToNext();
            }
            results.close();
            return infos;
        }
        */
    }
}
