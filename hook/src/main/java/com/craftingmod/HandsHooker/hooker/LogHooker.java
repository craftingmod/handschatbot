package com.craftingmod.HandsHooker.hooker;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by superuser on 15/12/12.
 */
public class LogHooker extends BaseMapleHooker {

    public LogHooker(XC_LoadPackage.LoadPackageParam pm) {
        super(pm);
    }

    @Override
    public void handlePackage() {
        XposedBridge.hookAllMethods(global, "DLOG", this);
        XposedBridge.hookAllMethods(global, "GetDeviceID", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                final String uuid = "00000000-0000-0000-0000-000000000000";
                param.setResult(uuid);
                super.afterHookedMethod(param);
            }
        });
        XposedBridge.hookAllMethods(global, "GetPhoneNumber", new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                return null;
            }
        });


    }
    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        super.beforeHookedMethod(param);
        Object[] Arr = param.args;
        if (Arr.length >= 1) {
            for (int i = 0; i < Arr.length; i += 1) {
                XposedBridge.log((String) Arr[i]);
            }
        }
    }
}
