package com.craftingmod.HandsHooker.hooker;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by superuser on 16/1/18.
 */
public class SplashHooker extends BaseMapleHooker {

    public SplashHooker(XC_LoadPackage.LoadPackageParam pm) {
        super(pm);
    }

    @Override
    public void handlePackage() {
        /*
        XposedBridge.hookAllMethods(splash, "handleMessage", new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam mParam) throws Throwable {
                final Boolean firstExecute = (Boolean) XposedHelpers.callMethod(
                        XposedHelpers.callStaticMethod(getMapleClass(".SharedData"), "getInstance"),
                        "IsFirstExcute");
                if(firstExecute){
                    XposedHelpers.callMethod(
                            XposedHelpers.callStaticMethod(getMapleClass(".UserDatabase"), "getInstance"),
                            "Logout");
                }
                Intent intent = new Intent((Context)mParam.thisObject,getMapleClass(".MainTabsActivity"));
                intent.setFlags(335544320);
                XposedHelpers.callMethod(mParam.thisObject,"startActivity",intent);
                XposedHelpers.callMethod(mParam.thisObject,"finish");
                return null;
            }
        });
        */
        XposedBridge.hookAllMethods(splash, "getAppVersionState", new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                return 1;
            }
        });
        /*
        Class<?> handsHandler = getMapleClass(".MapleHandsSplash.MapleHandsSplashActivityHandler");
        XC_MethodReplacement callMsg = new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(final MethodHookParam mParam) throws Throwable {
                if(mParam.args.length >= 2){
                    if(mParam.args[1] instanceof Long){
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                exec(mParam.thisObject);
                            }
                        },(long)mParam.args[1]);
                    }
                }else{
                    exec(mParam.thisObject);
                }
                return null;
            }
            protected void exec(Object _this){
                XposedHelpers.callMethod(_this,"handleMessage",new Message());
            }
        };
        XposedBridge.hookAllMethods(handsHandler,"sendMessage", callMsg);
        XposedBridge.hookAllMethods(handsHandler,"sendMessageDelayed", callMsg);
        */
    }
}
