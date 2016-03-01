package com.craftingmod.HandsHooker;

import com.craftingmod.HandsHooker.hooker.LogHooker;
import com.craftingmod.HandsHooker.hooker.MessageHooker;
import com.craftingmod.HandsHooker.hooker.SplashHooker;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by superuser on 16/2/2.
 */
public class XMain implements IXposedHookLoadPackage {

    private Class<?> chatModel;
    protected Class<?> FragTalk;
    protected Class<?> global;
    protected Class<?> messageAdapter;
    protected Class<?> chatManager;
    protected Class<?> talkroom;

    private Gson g;
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        final String pname = loadPackageParam.packageName;
        final ClassLoader cl = loadPackageParam.classLoader;
        final String mMaple = "com.Nexon.MonsterLifeChat";

        g = new GsonBuilder().create();

        if(pname.equalsIgnoreCase(mMaple)){
            chatModel = XposedHelpers.findClass(mMaple + ".ChatMessageModel", cl);
            FragTalk = XposedHelpers.findClass(mMaple + ".maplechat.talk.FragmentMapleTalkMessages",cl);
            global = XposedHelpers.findClass(mMaple + ".G",cl);
            messageAdapter = XposedHelpers.findClass(mMaple + ".maplechat.talk.AdapterMapleTalkMessages",cl);
            chatManager = XposedHelpers.findClass(mMaple + ".ChatManager", cl);
            talkroom = XposedHelpers.findClass(mMaple + ".maplechat.talk.ActivityMHTalkRoom",cl);

           /*
            XposedHelpers.findAndHookMethod(talkroom, "sendMessage", String.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Context context = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");

                    Intent intent = new Intent("com.craftingmod.GET_FRD");
                    intent.putExtra("data", (int[]) XposedHelpers.getObjectField(param.thisObject, "friendsAids"));
                    context.sendBroadcast(intent);
                }
            });
            */

            new LogHooker(loadPackageParam).handlePackage();
            new MessageHooker(loadPackageParam).handlePackage();
            new SplashHooker(loadPackageParam).handlePackage();
        }
    }
}
