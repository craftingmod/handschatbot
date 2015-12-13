package com.craftingmod.maplechatbot.hooker;

import android.content.Context;

import de.robv.android.xposed.XC_MethodHook;
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
